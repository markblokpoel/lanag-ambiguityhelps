package com.markblokpoel.lanag.ambiguityhelps.experiments.random

import com.markblokpoel.lanag.ambiguityhelps.datastructures.TurnData
import com.markblokpoel.lanag.core.Data

case class DataFullRandom(pairId: Long,
                          agent1Order: Int,
                          agent2Order: Int,
                          agent1AmbiguityMean: Double,
                          agent1AmbiguityVar: Double,
                          agent2AmbiguityMean: Double,
                          agent2AmbiguityVar: Double,
                          asymmetry: Double,
                          densityOrigin: Double,
                          mutationRate: Double,
                          interaction: Seq[TurnData])
  extends Data
