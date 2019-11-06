package com.markblokpoel.lanag.ambiguityhelps.experiments.random

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

/** Generates, for specific parameters, a pair of agents with pure random binary lexicons.
  *
  * @param vocabularySize     The size of the agents' vocabularies.
  * @param contextSize        The size of the agents' contexts.
  * @param densityResolution  Specifies how fine-grained the parameter space is on the dimension of agent 1's lexicon
  *                           density. I.e., each relation in that lexicon has probability p = 0 to 1 with step size
  *                           <code>changeResolution</code> to be 1.0, and is 0.0 otherwise.
  * @param mutationResolution Specifies how fine-grained the parameter space is on the dimension of agent 2's lexicon
  *                           mutation rate. I.e., that lexicon has probability p = 0 to 1 with step size
  *                           <code>mutationResolution</code> to be different from agent 1's lexicon.
  * @param sampleSize         The number of agent pairs that can be sampled from each point in the parameter space.
  * @param beta               The decision noise parameter used in the softargmax inference of the agents.
  */
@SerialVersionUID(100L)
class RandomPairGenerator(vocabularySize: Int,
                          contextSize: Int,
                          densityResolution: Double,
                          mutationResolution: Double,
                          sampleSize: Int,
                          beta: Double = Double.PositiveInfinity)
    extends PairGenerator[ParametersRandom,
                          ReferentialIntention,
                          ContentSignal,
                          RSA1ShotAgent,
                          OriginData](sampleSize)
    with Serializable {

  /** Generates the parameter space, specifying the full domain of parameters used to randomly generate
    * agent pairs.
    *
    * @return A sequence of parameters.
    */
  override def generateParameterSpace: Seq[ParametersRandom] = {
    val densityRange = Ranges.range(densityResolution)
    val mutationRange = Ranges.range(mutationResolution)

    for {
      dens <- densityRange
      mut <- mutationRange
    } yield ParametersRandom(dens, mut)
  }

  /** Generates, for specific parameters, a pair of agents with random lexicons, related by <code>mutationRate</code>.
    *
    * @param parameters Parameters from [[generateParameterSpace]].
    * @return An [[com.markblokpoel.lanag.core.AgentPair]], containing a pair of agents of the specified type
    *         and [[com.markblokpoel.lanag.ambiguityhelps.datastructures.OriginData]] reflecting the
    *         pair's origin parameters.
    */
  override def generatePair(
      parameters: ParametersRandom): AgentPair[ReferentialIntention,
                                               ContentSignal,
                                               RSA1ShotAgent,
                                               OriginData] = {
    val lexicon1 = Lexicon.generateRandomBinaryLexicon(parameters.density,
                                                       vocabularySize,
                                                       contextSize)
    val lexicon2 = lexicon1.mutate(parameters.mutationRate)
    AgentPair[ReferentialIntention, ContentSignal, RSA1ShotAgent, OriginData](
      new RSA1ShotAgent(lexicon1, beta = beta),
      new RSA1ShotAgent(lexicon2, beta = beta),
      OriginData(parameters.density, parameters.mutationRate)
    )
  }
}
