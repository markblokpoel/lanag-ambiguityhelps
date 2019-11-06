package com.markblokpoel.lanag.ambiguityhelps.experiments.structured

import com.markblokpoel.lanag.ambiguityhelps.RSA1ShotAgent
import com.markblokpoel.lanag.ambiguityhelps.datastructures.OriginData
import com.markblokpoel.lanag.core.{
  AgentPair,
  ContentSignal,
  PairGenerator,
  ReferentialIntention
}
import com.markblokpoel.lanag.math.Ranges
import com.markblokpoel.lanag.rsa.{
  EDIT_DISTANCE,
  HAMMING_DISTANCE,
  StructuredLexicon,
  StructuredMappingFunction
}

/** Generates, for specific parameters, a pair of agents with random binary lexicons based on structured binary
  * string representations of the vocabulary and context..
  *
  * @note It is recommended to set the <code>representationLength</code> larger than <code>vocabularySize</code>
  *       and <code>contextSize</code>.
  * @param vocabularySize       The size of the agents' vocabularies.
  * @param contextSize          The size of the agents' contexts.
  * @param representationLength The length of the binary representations used.
  * @param mappingFunction      A function that takes two binary string representations and computes the
  *                             relationship between them.
  * @param thresholdResolution  Specifies that the range of thresholds within the parameter space, from 0 to 1 by
  *                             <code>thresholdResolution</code>. Threshold is used to determine signal-referent
  *                             relationships, based on <code>mappingFunction</code>. When
  *                             <code>mappingFunction</code> >= mappingThreshold, there is a 1.0-value relationship
  *                             or 0.0 otherwise.
  * @param changeResolution     Specifies how fine-grained the parameter space is. I.e., derived lexicons will be
  *                             generated for mutations at change = 0 to 1 with step size <code>changeResolution</code>.
  * @param sampleSize           The number of agent pairs that can be sampled from each point in the parameter space.
  * @param thresholdLowerBound  The lower bound of the threshold range.
  * @param thresholdUpperBound  The upper bound of the threshold range.
  * @param changeLowerBound     The lower bound of the change range.
  * @param changeUpperBound     The upper bound of the change range.
  * @param beta                 The decision noise parameter used in the softargmax inference of the agents.
  */
@SerialVersionUID(100L)
class StructuredPairGenerator(vocabularySize: Int,
                              contextSize: Int,
                              representationLength: Int,
                              mappingFunction: StructuredMappingFunction,
                              thresholdResolution: Double,
                              changeResolution: Double,
                              sampleSize: Int,
                              thresholdLowerBound: Double = 0,
                              thresholdUpperBound: Double = 1,
                              changeLowerBound: Double = 0,
                              changeUpperBound: Double = 1,
                              beta: Double = Double.PositiveInfinity)
    extends PairGenerator[ParametersStructured,
                          ReferentialIntention,
                          ContentSignal,
                          RSA1ShotAgent,
                          OriginData](sampleSize)
    with Serializable {

  /** Generates the parameter space, specifying the full domain of parameters used to randomly generate
    * agent pairs based on structured binary string representations.
    *
    * @return A sequence of parameters.
    */
  override def generateParameterSpace: Seq[ParametersStructured] = {
    val thresholdRange = Ranges.range(thresholdResolution,
                                      thresholdLowerBound,
                                      thresholdUpperBound)
    val changeRange =
      Ranges.range(changeResolution, changeLowerBound, changeUpperBound)

    for {
      t <- thresholdRange
      cha <- changeRange
    } yield ParametersStructured(t, cha)
  }

  /** Generates, for specific parameters, a pair of agents with structured lexicons randomly, related through
    * <code>changeRate</code> which affects the agent's binary string representations of the context.
    *
    * @param parameters Parameters from [[generateParameterSpace]].
    * @return An [[com.markblokpoel.lanag.core.AgentPair]], containing a pair of agents of the specified type
    *         and [[OriginData]] reflecting the pair's
    *         origin parameters, i.e., threshold and change rate.
    */
  override def generatePair(
      parameters: ParametersStructured): AgentPair[ReferentialIntention,
                                                   ContentSignal,
                                                   RSA1ShotAgent,
                                                   OriginData] = {
    val lexicon1 = StructuredLexicon.generateBinaryStructuredLexicon(
      representationLength,
      mappingFunction,
      parameters.threshold,
      vocabularySize,
      contextSize
    )
    val vocabularyRepresentations2 = lexicon1.vocabularyRepresentations
    val contextRepresentations2 =
      StructuredLexicon.mutateStructuredRepresentations(
        lexicon1.contextRepresentations,
        parameters.changeRate)
    val lexicon2 = StructuredLexicon.generateBinaryStructuredLexicon(
      vocabularyRepresentations2,
      contextRepresentations2,
      mappingFunction,
      parameters.threshold
    )

    val agent1 = new RSA1ShotAgent(lexicon1.getLexicon, beta = beta)
    val agent2 = new RSA1ShotAgent(lexicon2.getLexicon, beta = beta)
    val mappingFunctionId = mappingFunction match {
      case HAMMING_DISTANCE => 0.0
      case EDIT_DISTANCE    => 1.0
      case _                => -1.0
    }
    val originData =
      OriginData(parameters.threshold, parameters.changeRate, mappingFunctionId)

    AgentPair[ReferentialIntention, ContentSignal, RSA1ShotAgent, OriginData](
      agent1,
      agent2,
      originData)
  }

  /** Helper function to translate the similarity measurement method.
    *
    * @param enc The double value encoding the similarity measurement method.
    * @return A string with the similarity measurement method name.
    */
  def decodeMappingFunction(enc: Double): String = {
    if (enc == 0.0) "hamming distance"
    else if (enc == 1.0) "edit distance"
    else "unknown"
  }
}
