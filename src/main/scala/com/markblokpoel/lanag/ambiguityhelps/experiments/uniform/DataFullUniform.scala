package com.markblokpoel.lanag.ambiguityhelps.experiments.uniform

import com.markblokpoel.lanag.ambiguityhelps.datastructures.TurnData
import com.markblokpoel.lanag.core.Data

case class DataFullUniform(pairId: Long,
                           agent1Order: Int,
                           agent2Order: Int,
                           agent1AmbiguityMean: Double,
                           agent1AmbiguityVar: Double,
                           agent2AmbiguityMean: Double,
                           agent2AmbiguityVar: Double,
                           asymmetry: Double,
                           changeMethod: String,
                           changeRate: Double,
                           interaction: Seq[TurnData])
  extends Data
