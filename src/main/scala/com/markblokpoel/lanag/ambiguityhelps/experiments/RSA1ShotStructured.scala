package com.markblokpoel.lanag.ambiguityhelps.experiments

import java.time.Instant

import com.markblokpoel.lanag.ambiguityhelps.RSA1ShotDataStructures.RSA1TurnData
import com.markblokpoel.lanag.ambiguityhelps.RSA1ShotInteraction
import com.markblokpoel.lanag.core.Data
import com.markblokpoel.lanag.rsa.HAMMING_DISTANCE
import com.markblokpoel.lanag.util.{ConfigWrapper, RNG, SparkSimulation}
import com.typesafe.config.ConfigFactory

// Simple case class to collect simulation data.
case class StructuredFlatData(pairId: Long,
                              agent1Order: Int,
                              agent2Order: Int,
                              agent1AmbiguityMean: Double,
                              agent1AmbiguityVar: Double,
                              agent2AmbiguityMean: Double,
                              agent2AmbiguityVar: Double,
                              asymmetry: Double,
                              threshold: Double,
                              representationalChangeRate: Double,
                              interaction: Seq[RSA1TurnData])
    extends Data

// Simple case class to collect summarized data, removing individual turns.
case class StructuredSummaryData(pairId: Long,
                                 agent1Order: Int,
                                 agent2Order: Int,
                                 agent1AmbiguityMean: Double,
                                 agent1AmbiguityVar: Double,
                                 agent2AmbiguityMean: Double,
                                 agent2AmbiguityVar: Double,
                                 asymmetry: Double,
                                 threshold: Double,
                                 representationalChangeRate: Double,
                                 averageSuccess: Double,
                                 averageEntropyAsSpeaker: Double,
                                 averageEntropyAsListener: Double)
    extends Data

/** Sets up a local spark simulation for a 1-shot Rational Speech Act communication simulation
  * using structured agents. It writes the results to JSON and CSV files and plots some summary statistics.
  *
  * @author Mark Blokpoel
  */
@SerialVersionUID(100L)
object RSA1ShotStructured extends Serializable {

  def main(args: Array[String]): Unit = {
    /*
      Generic setup, loading configuration file if present.
     */
    val timestamp = Instant.now.toEpochMilli
    val folderName = "output/rsa1shot_structured-" + timestamp

    val conf = ConfigWrapper(ConfigFactory.load())

    val vocabularySize = conf.getOrElse[Int]("core.vocabulary-size", 8)
    val contextSize = conf.getOrElse[Int]("core.context-size", 4)
    val sampleSize = conf.getOrElse[Int]("core.sample-size", 25)
    val beta = conf.getOrElse[Double]("core.beta", Double.PositiveInfinity)
    val sparkLocalMode = conf.getOrElse[Boolean]("core.spark-local-mode", false)
    val randomSeed = conf.getOrElse[Long]("core.random-seed", 0)
    val representationLength = conf.getOrElse[Int](
      "structured.representation-length",
      math.max(vocabularySize, contextSize))
    val thresholdResolution =
      conf.getOrElse[Double]("structured.threshold-resolution", 0.1)
    val thresholdLowerBound =
      conf.getOrElse[Double]("structured.threshold-lowerbound", 0)
    val thresholdUpperBound =
      conf.getOrElse[Double]("structured.threshold-upperbound", 1)
    val changeResolution =
      conf.getOrElse[Double]("structured.change-resolution", 0.2)
    val changeLowerBound =
      conf.getOrElse[Double]("structured.change-lowerbound", 0)
    val changeUpperBound =
      conf.getOrElse[Double]("structured.change-upperbound", 1)

    RNG.setSeed(randomSeed)

    val sparkSim = SparkSimulation(sparkLocalMode)
    import sparkSim.spark.implicits._

    println("Running RSA 1-shot structured simulation with:")
    println(
      s"vocabulary-size = $vocabularySize\n"
        + s"context-size = $contextSize\n"
        + s"representation-length = $representationLength\n"
        + s"threshold-resolution = $thresholdResolution\n"
        + s"threshold-lower-bound = $thresholdLowerBound\n"
        + s"threshold-upper-bound = $thresholdUpperBound\n"
        + s"change-resolution = $changeResolution\n"
        + s"change-upper-bound = $changeLowerBound\n"
        + s"change-upper-bound = $changeUpperBound\n"
        + s"sample-size = $sampleSize\n"
        + s"beta = $beta\n")

    /*
    Setting up the simulation.
     */
    val spg = new StructuredPairGenerator(
      vocabularySize,
      contextSize,
      representationLength,
      HAMMING_DISTANCE,
      thresholdResolution,
      changeResolution,
      sampleSize,
      thresholdLowerBound,
      thresholdUpperBound,
      changeLowerBound,
      changeUpperBound,
      beta
    )
    val parameterSpace = spg.generateParameterSpace

    // Setting up the simulation at the LocalSparkSimulation.
    val sparkSimulation = sparkSim
      .parallelize(parameterSpace)
      .map(parameters => spg.sampleGenerator(parameters))
      .map(generator =>
        generator.flatMap(pair => {
          val interaction0 =
            RSA1ShotInteraction(pair.agent1, pair.agent2, pair.originData)
          Seq(interaction0.atOrder(0),
              interaction0.atOrder(1),
              interaction0.atOrder(2))
        }))
      .flatMap(interactions => interactions.map(_.runAndCollectData))
      .map(i =>
        StructuredFlatData(
          i.pairId,
          i.agent1Order,
          i.agent2Order,
          i.agent1AmbiguityMean,
          i.agent1AmbiguityVar,
          i.agent2AmbiguityMean,
          i.agent2AmbiguityVar,
          i.asymmetry,
          i.originData.parameter1,
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
        StructuredSummaryData(
          i.pairId,
          i.agent1Order,
          i.agent2Order,
          i.agent1AmbiguityMean,
          i.agent1AmbiguityVar,
          i.agent2AmbiguityMean,
          i.agent2AmbiguityVar,
          i.asymmetry,
          i.threshold,
          i.representationalChangeRate,
          i.interaction.count(d => d.success) / i.interaction.length.toDouble,
          i.interaction.foldLeft(0.0)((acc, e) =>
            acc + e.speakerData.speakerEntropy
              .getOrElse(0.0)) / i.interaction.length.toDouble,
          i.interaction.foldLeft(0.0)((acc, e) =>
            acc + e.listenerData.listenerEntropy
              .getOrElse(0.0)) / i.interaction.length.toDouble
      ))
    summary.write.option("header", value = true).csv(folderName + "/csv")
    summary.show()

    // Close the LocalSparkSimulation, ensuring all data is written to disk.
    sparkSim.shutdown()
  }
}
