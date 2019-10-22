package com.markblokpoel.lanag.ambiguityhelps.experiments.structured

import com.markblokpoel.lanag.core.Data

case class DataFlatStructured(pairId: Long,
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