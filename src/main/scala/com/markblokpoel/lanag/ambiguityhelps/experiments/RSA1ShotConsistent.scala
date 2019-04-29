package com.markblokpoel.lanag.ambiguityhelps.experiments

import java.time.Instant

import com.markblokpoel.lanag.ambiguityhelps.RSA1ShotDataStructures.RSA1TurnData
import com.markblokpoel.lanag.ambiguityhelps.RSA1ShotInteraction
import com.markblokpoel.lanag.core.Data
import com.markblokpoel.lanag.util.{ConfigWrapper, RNG, SparkSimulation}
import com.typesafe.config.ConfigFactory

// Simple case class to collect simulation data.
case class RSA1ShotFlatData(pairId: Long,
                            agent1Order: Int,
                            agent2Order: Int,
                            agent1AmbiguityMean: Double,
                            agent1AmbiguityVar: Double,
                            agent2AmbiguityMean: Double,
                            agent2AmbiguityVar: Double,
                            asymmetry: Double,
                            changeMethod: String,
                            changeRate: Double,
                            interaction: Seq[RSA1TurnData])
    extends Data

// Simple case class to collect summarized data, removing individual turns.
case class RSA1ShotSummaryData(pairId: Long,
                               agent1Order: Int,
                               agent2Order: Int,
                               agent1AmbiguityMean: Double,
                               agent1AmbiguityVar: Double,
                               agent2AmbiguityMean: Double,
                               agent2AmbiguityVar: Double,
                               asymmetry: Double,
                               changeMethod: String,
                               changeRate: Double,
                               success: Double)
    extends Data

/** Sets up a local spark simulation for a 1-shot Rational Speech Act communication simulation
  * using consistent agents. It writes the results to JSON and CSV files and plots some summary statistics.
  *
  * @author Mark Blokpoel
  */
@SerialVersionUID(100L)
object RSA1ShotConsistent extends Serializable {
  def main(args: Array[String]): Unit = {
    /*
      Generic setup, loading configuration file if present.
     */
    val timestamp = Instant.now.toEpochMilli
    val folderName = "output/rsa1shot_consistent-" + timestamp

    val conf = ConfigWrapper(ConfigFactory.load())

    val vocabularySize = conf.getOrElse[Int]("core.vocabulary-size", 8)
    val contextSize = conf.getOrElse[Int]("core.context-size", 4)
    val sampleSize = conf.getOrElse[Int]("core.sample-size", 25)
    val beta = conf.getOrElse[Double]("core.beta", Double.PositiveInfinity)
    val sparkLocalMode = conf.getOrElse[Boolean]("core.spark-local-mode", false)
    val randomSeed = conf.getOrElse[Long]("core.random-seed", 0)
    val changeResolution =
      conf.getOrElse[Double]("consistent.change-resolution", 0.2)

    RNG.setSeed(randomSeed)

    val sparkSim = SparkSimulation(sparkLocalMode)
    import sparkSim.spark.implicits._

    println("Running RSA 1-shot structured simulation with:")
    println(
      s"vocabulary-size = $vocabularySize\n"
        + s"context-size = $contextSize\n"
        + s"change-resolution = $changeResolution\n"
        + s"sample-size = $sampleSize\n"
        + s"beta = $beta\n")

    /*
      Setting up the simulation.
     */
    val gen = new ConsistentPairGenerator(
      vocabularySize,
      contextSize,
      changeResolution,
      sampleSize,
      beta
    )
    val parameterSpace = gen.generateParameterSpace

    // Setting up the simulation at the LocalSparkSimulation.
    val sparkSimulation = sparkSim
      .parallelize(parameterSpace)
      .map(parameters => gen.sampleGenerator(parameters))
      .map(generator =>
        generator.flatMap(pair => {
          Seq(
            RSA1ShotInteraction(pair.agent1.withOrder(0),
                                pair.agent2.withOrder(0),
                                pair.originData),
            RSA1ShotInteraction(pair.agent1.withOrder(1),
                                pair.agent2.withOrder(1),
                                pair.originData),
            RSA1ShotInteraction(pair.agent1.withOrder(2),
                                pair.agent2.withOrder(2),
                                pair.originData)
          )
        }))
      .flatMap(interactions => interactions.map(_.runAndCollectData))
      .map(i =>
        RSA1ShotFlatData(
          i.pairId,
          i.agent1Order,
          i.agent2Order,
          i.agent1AmbiguityMean,
          i.agent1AmbiguityVar,
          i.agent2AmbiguityMean,
          i.agent2AmbiguityVar,
          i.asymmetry,
          gen.decodeChangeMethod(i.originData.parameter1),
          i.originData.parameter2,
          i.interaction
      ))

    // Cache the results so Spark doesn't recompute simulation each time you use the Dataset.
    val results = sparkSimulation.toDS().cache()
    // Write the results to JSON file.
    results.write.json(folderName + "/json")
    results.show()

    // Summarize the individual turns, and write summarized data to CSV file.
    val summary = results.map(
      i =>
        RSA1ShotSummaryData(
          i.pairId,
          i.agent1Order,
          i.agent2Order,
          i.agent1AmbiguityMean,
          i.agent1AmbiguityVar,
          i.agent2AmbiguityMean,
          i.agent2AmbiguityVar,
          i.asymmetry,
          i.changeMethod,
          i.changeRate,
          i.interaction.count(d => d.success) / i.interaction.length.toDouble
      ))
    summary.write.option("header", value = true).csv(folderName + "/csv")
    summary.show()

    // Close the LocalSparkSimulation, ensuring all data is written to disk.
    sparkSim.shutdown()
  }
}
