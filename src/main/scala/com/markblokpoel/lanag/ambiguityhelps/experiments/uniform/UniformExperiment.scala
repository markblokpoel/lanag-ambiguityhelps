package com.markblokpoel.lanag.ambiguityhelps.experiments.uniform

import java.time.Instant

import com.markblokpoel.lanag.ambiguityhelps.RSA1ShotInteraction
import com.markblokpoel.lanag.util.{ConfigWrapper, RNG, SparkSimulation}
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.{Dataset, SparkSession}

/** Sets up a spark simulation for a 1-shot Rational Speech Act communication simulation
  * using uniform agents. It writes the results to JSON and CSV files and plots some summary statistics.
  *
  * @author Mark Blokpoel
  */
@SerialVersionUID(100L)
object UniformExperiment extends Serializable with App {
  val timestamp = Instant.now.toEpochMilli
  val outputFolder = "output/rsa1shot_uniform-" + timestamp

  val conf = ConfigWrapper(ConfigFactory.load())

  val vocabularySize = conf.getOrElse[Int]("core.vocabulary-size", 8)
  val contextSize = conf.getOrElse[Int]("core.context-size", 4)
  val sampleSize = conf.getOrElse[Int]("core.sample-size", 25)
  val interactionLength = conf.getOrElse[Int]("core.interaction-length", 20)
  val beta = conf.getOrElse[Double]("core.beta", Double.PositiveInfinity)
  val sparkLocalMode = conf.getOrElse[Boolean]("core.spark-local-mode", false)
  val randomSeed = conf.getOrElse[Long]("core.random-seed", 0)
  val writeJSON = conf.getOrElse[Boolean]("core.write-json", false)
  val changeResolution =
    conf.getOrElse[Double]("consistent.change-resolution", 0.2)

  val sparkSimulation = SparkSimulation(sparkLocalMode)

  val dataSet: Dataset[DataFullUniform] =
    run(sparkSimulation.spark,
        vocabularySize,
        contextSize,
        changeResolution,
        sampleSize,
        interactionLength,
        beta,
        randomSeed)
  dataSet.show()

  if (writeJSON) dataSet.write.json(outputFolder + "/json")
  val summary = flattenData(sparkSimulation.spark, dataSet)
  summary.write.option("header", value = true).csv(outputFolder + "/csv")
  summary.show()

  // Close the spark session, ensuring all data is written to disk.
  sparkSimulation.shutdown()

  def flattenData(sparkSession: SparkSession,
                  fullData: Dataset[DataFullUniform]) = {
    import sparkSession.implicits._

    fullData.map(
      dataRow =>
        DataFlatUniform(
          dataRow.pairId,
          dataRow.agent1Order,
          dataRow.agent2Order,
          dataRow.agent1AmbiguityMean,
          dataRow.agent1AmbiguityVar,
          dataRow.agent2AmbiguityMean,
          dataRow.agent2AmbiguityVar,
          dataRow.asymmetry,
          dataRow.changeMethod,
          dataRow.changeRate,
          averageSuccess = dataRow.interaction
            .count(d => d.success) / dataRow.interaction.length.toDouble,
          averageEntropyAsSpeaker = dataRow.interaction
            .foldLeft(0.0)((acc, e) =>
              acc + e.speakerData.speakerEntropy
                .getOrElse(0.0)) / dataRow.interaction.length.toDouble,
          averageEntropyAsListener = dataRow.interaction
            .foldLeft(0.0)((acc, e) =>
              acc + e.listenerData.listenerEntropy
                .getOrElse(0.0)) / dataRow.interaction.length.toDouble
      ))
  }

  def run(sparkSession: SparkSession,
          vocabularySize: Int,
          contextSize: Int,
          changeResolution: Double,
          sampleSize: Int,
          interactionLength: Int,
          beta: Double,
          randomSeed: Long): Dataset[DataFullUniform] = {

    RNG.setSeed(randomSeed)
    import sparkSession.implicits._

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
    val gen = new UniformPairGenerator(vocabularySize,
                                       contextSize,
                                       changeResolution,
                                       sampleSize,
                                       beta)
    val parameterSpace = gen.generateParameterSpace

    // Setting up the simulation at the LocalSparkSimulation.
    val sparkSimulation = sparkSession.sparkContext
      .parallelize(parameterSpace)
      .map(parameters => gen.sampleGenerator(parameters))
      .map(generator =>
        generator.flatMap(pair => {
          val interaction = RSA1ShotInteraction(pair.agent1,
                                                pair.agent2,
                                                pair.originData,
                                                maxTurns = interactionLength)
          Seq(interaction.atOrder(0),
              interaction.atOrder(1),
              interaction.atOrder(2))
        }))
      .flatMap(interactions => interactions.map(_.runAndCollectData))
      .map(dataRow =>
        DataFullUniform(
          dataRow.pairId,
          dataRow.agent1Order,
          dataRow.agent2Order,
          dataRow.agent1AmbiguityMean,
          dataRow.agent1AmbiguityVar,
          dataRow.agent2AmbiguityMean,
          dataRow.agent2AmbiguityVar,
          dataRow.asymmetry,
          changeMethod = gen.decodeChangeMethod(dataRow.originData.parameter1),
          changeRate = dataRow.originData.parameter2,
          dataRow.interaction
      ))

    // Cache the results so Spark doesn't recompute simulation each time you use the Dataset.
    sparkSimulation.toDS().cache()
  }
}
