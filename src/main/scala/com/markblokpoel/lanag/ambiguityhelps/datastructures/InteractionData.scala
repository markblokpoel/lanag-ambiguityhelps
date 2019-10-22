package com.markblokpoel.lanag.ambiguityhelps.datastructures

import com.markblokpoel.lanag.core.Data

case class InteractionData(pairId: Long,
                           agent1Order: Int,
                           agent2Order: Int,
                           agent1AmbiguityMean: Double,
                           agent1AmbiguityVar: Double,
                           agent2AmbiguityMean: Double,
                           agent2AmbiguityVar: Double,
                           asymmetry: Double,
                           originData: OriginData,
                           interaction: Seq[TurnData])
  extends Data
