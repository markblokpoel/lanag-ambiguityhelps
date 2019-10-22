package com.markblokpoel.lanag.ambiguityhelps

import com.markblokpoel.lanag.core.{Data, Parameters}

/** Contains a collection of data structures that are used to collect simulation data and support
  * Apache Spark <code>org.apache.spark.rdd.RDD</code>, <code>org.apache.spark.sql.DataFrame</code>, and
  * <code>org.apache.spark.sql.Dataset</code>.
  */
@SerialVersionUID(100L)
object RSA1ShotDataStructures extends Serializable {
  case class SpeakerData(speakerEntropy: Option[Double]) extends Data

  case class ListenerData(listenerEntropy: Option[Double]) extends Data

  case class RSA1TurnData(turn: Int,
                          success: Boolean,
                          speakerData: SpeakerData,
                          listenerData: ListenerData)
      extends Data

  case class RSA1OriginData(parameter1: Double = Double.NaN,
                            parameter2: Double = Double.NaN,
                            parameter3: Double = Double.NaN,
                            parameter4: Double = Double.NaN)
      extends Data

  case class RSA1ShotRandomizedParameters(density: Double, mutationRate: Double)
      extends Parameters

  case class RSA1ShotConsistentParameters(agent1Ambiguity: Int,
                                          agent2Ambiguity: Int,
                                          changeRate: Double)
      extends Parameters

  case class RSA1ShotStructuredParameters(threshold: Double, changeRate: Double)
      extends Parameters

  case class RSA1InteractionData(pairId: Long,
                                 agent1Order: Int,
                                 agent2Order: Int,
                                 agent1AmbiguityMean: Double,
                                 agent1AmbiguityVar: Double,
                                 agent2AmbiguityMean: Double,
                                 agent2AmbiguityVar: Double,
                                 asymmetry: Double,
                                 originData: RSA1OriginData,
                                 interaction: Seq[RSA1TurnData])
      extends Data
}
