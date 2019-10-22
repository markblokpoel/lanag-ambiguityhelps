package com.markblokpoel.lanag.ambiguityhelps.experiments.random

import java.time.Instant

import com.markblokpoel.lanag.ambiguityhelps.RSA1ShotInteraction
import com.markblokpoel.lanag.util.{ConfigWrapper, RNG, SparkSimulation}
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.{Dataset, SparkSession}

/** Sets up a local spark simulation for a 1-shot Rational Speech Act communication simulation
  * using purely random agents. It writes the results to JSON and CSV files and plots some summary statistics.
  *
  * @author Mark Blokpoel
  */
@SerialVersionUID(100L)
object RandomExperiment extends Serializable with App {
  /*
    Generic setup, loading configuration file if present.
   */
  val timestamp = Instant.now.toEpochMilli
  val outputFolder = "output/rsa1shot_random-" + timestamp

  val conf = ConfigWrapper(ConfigFactory.load())

  val vocabularySize = conf.getOrElse[Int]("core.vocabulary-size", 8)
  val contextSize = conf.getOrElse[Int]("core.context-size", 4)
  val sampleSize = conf.getOrElse[Int]("core.sample-size", 25)
  val interactionLength = conf.getOrElse[Int]("core.interaction-length", 20)
  val beta = conf.getOrElse[Double]("core.beta", Double.PositiveInfinity)
  val sparkLocalMode = conf.getOrElse[Boolean]("core.spark-local-mode", false)
  val randomSeed = conf.getOrElse[Long]("core.random-seed", 0)
  val writeJSON = conf.getOrElse[Boolean]("core.write-json", false)
  val densityResolution =
    conf.getOrElse[Double]("random.density-resolution", 0.1)
  val mutationResolution =
    conf.getOrElse[Double]("random.mutation-resolution", 0.2)

  val sparkSimulation = SparkSimulation(sparkLocalMode)
  import sparkSimulation.spark.implicits._

  val dataSet: Dataset[DataFullRandom] =
    run(
      sparkSimulation.spark,
      vocabularySize,
      contextSize,
      densityResolution,
      mutationResolution,
      sampleSize,
      interactionLength,
      beta,
      randomSeed)
  dataSet.show()

  // Write the results to JSON file.
  if (writeJSON) dataSet.write.json(outputFolder + "/json")

  // Summarize the individual turns, and write summarized data to CSV file.
  val summary = dataSet.map(dataRow =>
    DataFlatRandom(
      dataRow.pairId,
      dataRow.agent1Order,
      dataRow.agent2Order,
      dataRow.agent1AmbiguityMean,
      dataRow.agent1AmbiguityVar,
      dataRow.agent2AmbiguityMean,
      dataRow.agent2AmbiguityVar,
      dataRow.asymmetry,
      dataRow.densityOrigin,
      dataRow.mutationRate,
      averageSuccess = dataRow.interaction.count(d => d.success) / dataRow.interaction.length.toDouble,
      averageEntropyAsSpeaker = dataRow.interaction.foldLeft(0.0)((acc, e) =>
        acc + e.speakerData.speakerEntropy.getOrElse(0.0)) / dataRow.interaction.length.toDouble,
      averageEntropyAsListener = dataRow.interaction.foldLeft(0.0)((acc, e) =>
        acc + e.listenerData.listenerEntropy.getOrElse(0.0)) / dataRow.interaction.length.toDouble
    ))
  summary.write.option("header", value = true).csv(outputFolder + "/csv")
  summary.show()

  // Close the spark session, ensuring all data is written to disk.
  sparkSimulation.shutdown()

  def run(sparkSession: SparkSession,
          vocabularySize: Int,
          contextSize: Int,
          densityResolution: Double,
          mutationResolution: Double,
          sampleSize: Int,
          interactionLength: Int,
          beta: Double,
          randomSeed: Long): Dataset[DataFullRandom] = {

    RNG.setSeed(randomSeed)
    import sparkSession.implicits._

    println("Running RSA 1-shot structured simulation with:")
    println(
      s"vocabulary-size = $vocabularySize\n"
        + s"context-size = $contextSize\n"
        + s"density-resolution = $densityResolution\n"
        + s"mutation-resolution = $mutationResolution\n"
        + s"sample-size = $sampleSize\n"
        + s"beta = $beta\n")

    /*
      Setting up the simulation.
     */
    val gen = new RandomPairGenerator(
      vocabularySize,
      contextSize,
      densityResolution,
      mutationResolution,
      sampleSize,
      beta
    )
    val parameterSpace = gen.generateParameterSpace

    // Setting up the simulation at the LocalSparkSimulation.
    val sparkSimulation = sparkSession.sparkContext
      .parallelize(parameterSpace)
      .map(parameters => gen.sampleGenerator(parameters))
      .map(generator =>
        generator.flatMap(pair => {
          val interaction = RSA1ShotInteraction(pair.agent1, pair.agent2, pair.originData, maxTurns = interactionLength)
          Seq(interaction.atOrder(0), interaction.atOrder(1), interaction.atOrder(2))}))
      .flatMap(interactions => interactions.map(_.runAndCollectData))
      .map(dataRow =>
        DataFullRandom(
          dataRow.pairId,
          dataRow.agent1Order,
          dataRow.agent2Order,
          dataRow.agent1AmbiguityMean,
          dataRow.agent1AmbiguityVar,
          dataRow.agent2AmbiguityMean,
          dataRow.agent2AmbiguityVar,
          dataRow.asymmetry,
          densityOrigin = dataRow.originData.parameter1,
          mutationRate = dataRow.originData.parameter2,
          dataRow.interaction
        ))

    // Cache the results so Spark doesn't recompute simulation each time you use the Dataset.
    sparkSimulation.toDS().cache()
  }
}
