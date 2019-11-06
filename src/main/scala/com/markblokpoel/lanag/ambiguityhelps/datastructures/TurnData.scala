package com.markblokpoel.lanag.ambiguityhelps.datastructures

import com.markblokpoel.lanag.core.Data

case class TurnData(turn: Int,
                    success: Boolean,
                    speakerData: SpeakerData,
                    listenerData: ListenerData)
    extends Data
