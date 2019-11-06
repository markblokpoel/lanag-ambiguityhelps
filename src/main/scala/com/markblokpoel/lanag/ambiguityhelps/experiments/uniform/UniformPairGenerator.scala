package com.markblokpoel.lanag.ambiguityhelps.experiments.uniform

import com.markblokpoel.lanag.ambiguityhelps.RSA1ShotAgent
import com.markblokpoel.lanag.ambiguityhelps.datastructures.OriginData
import com.markblokpoel.lanag.core.{
  AgentPair,
  ContentSignal,
  PairGenerator,
  ReferentialIntention
}
import com.markblokpoel.lanag.math.Ranges
import com.markblokpoel.lanag.rsa.Lexicon
import com.markblokpoel.lanag.util.RNG

/** Use to generate pairs of agents who's lexicons are binary, consistent (each referent has at least 1 associated
  * word) and have a fixed ambiguity (viz., each word is associated with ambiguity referents). The range of ambiguity
  * goes from no ambiguity to full ambiguity. One agent's lexicon is generated (within set constraints) at random,
  * the other is a randomized derivative adhering to the same consistency constraints.
  *
  * @param vocabularySize   The size of the agents' vocabularies.
  * @param contextSize      The size of the agents' contexts.
  * @param changeResolution Specifies how fine-grained the parameter space is. I.e., derived lexicons will be
  *                         generated for mutations at change = 0 to 1 with step size <code>changeResolution</code>.
  * @param sampleSize       The number of agent pairs that can be sampled from each point in the parameter space.
  * @param beta             The decision noise parameter used in the softargmax inference of the agents.
  */
@SerialVersionUID(100L)
class UniformPairGenerator(vocabularySize: Int,
                           contextSize: Int,
                           changeResolution: Double,
                           sampleSize: Int,
                           beta: Double = Double.PositiveInfinity)
    extends PairGenerator[ParametersUniform,
                          ReferentialIntention,
                          ContentSignal,
                          RSA1ShotAgent,
                          OriginData](sampleSize)
    with Serializable {

  /** Generates the parameter space, specifying the full domain of parameters used to randomly generate consistent
    * agent pairs.
    *
    * @return A sequence of parameters.
    */
  override def generateParameterSpace: Seq[ParametersUniform] = {
    val ambiguityRange = 1 to contextSize
    val changeRange = Ranges.range(changeResolution)

    for {
      amb1 <- ambiguityRange
      amb2 <- ambiguityRange
      cha <- changeRange
    } yield ParametersUniform(amb1, amb2, cha)
  }

  /** Helper function to translate the lexicon change method to a string representation.
    *
    * @param enc The double value encoding the lexicon change method.
    * @return A string with the change method name.
    */
  def decodeChangeMethod(enc: Double): String = {
    if (enc == 0.0) "subtraction"
    else if (enc == 1.0) "addition"
    else if (enc == 2.0) "swap"
    else if (enc == 3.0) "random"
    else "unknown"
  }

  /** Generates, for specific parameters, a pair of agents with consistent lexicons randomly, related through
    * <code>changeRate</code>.
    *
    * @param parameters Parameters from [[generateParameterSpace]].
    * @return An [[com.markblokpoel.lanag.core.AgentPair]], containing a pair of agents of the specified type
    *         and [[com.markblokpoel.lanag.ambiguityhelps.datastructures.OriginData]] reflecting the pair's
    *         origin parameters, i.e., change rate and change method.
    */
  override def generatePair(
      parameters: ParametersUniform): AgentPair[ReferentialIntention,
                                                ContentSignal,
                                                RSA1ShotAgent,
                                                OriginData] = {
    val lexicon1 = Lexicon.generateConsistentAmbiguityMapping(
      parameters.agent1Ambiguity,
      vocabularySize,
      contextSize)
    val changeMethodId = RNG.nextInt(4)
    val lexicon2 = changeMethodId match {
      case 0 => lexicon1.removalBinaryMutation(parameters.changeRate)
      case 1 => lexicon1.additiveBinaryMutation(parameters.changeRate)
      case 2 => lexicon1.mixReferents(parameters.changeRate)
      case 3 =>
        Lexicon.generateConsistentAmbiguityMapping(parameters.agent2Ambiguity,
                                                   vocabularySize,
                                                   contextSize)
    }

    val agent1 = new RSA1ShotAgent(lexicon1, beta = beta)
    val agent2 = new RSA1ShotAgent(lexicon2, beta = beta)
    val originData =
      OriginData(changeMethodId.toDouble, parameters.changeRate)

    RNG.nextInt(2) match {
      case 0 =>
        AgentPair[ReferentialIntention,
                  ContentSignal,
                  RSA1ShotAgent,
                  OriginData](agent1, agent2, originData)
      case 1 =>
        AgentPair[ReferentialIntention,
                  ContentSignal,
                  RSA1ShotAgent,
                  OriginData](agent2, agent1, originData)
    }
  }
}
