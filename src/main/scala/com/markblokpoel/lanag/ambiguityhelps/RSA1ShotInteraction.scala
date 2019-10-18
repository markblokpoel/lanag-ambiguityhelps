package com.markblokpoel.lanag.ambiguityhelps

import com.markblokpoel.lanag.ambiguityhelps.datastructures.{InteractionData, ListenerData, OriginData, TurnData}
import com.markblokpoel.lanag.core.{ContentSignal, Interaction, ReferentialIntention}
import com.markblokpoel.lanag.util.InteractionIdentifier

/** This implements a 1-shot director matcher simulation between two Rational Speech Act agents. Agents
  * take turns communicating a randomly assigned referent. Entropy of production and interpretation, and
  * successes are measured.
  *
  * @param agent1     One of two agents that is part of this interaction, must be a subtype of [[com.markblokpoel.lanag.core.Agent]].
  * @param agent2     One of two agents that is part of this interaction, must be a subtype of [[com.markblokpoel.lanag.core.Agent]].
  * @param originData The data specifies the agents origin, must be a subtype of [[com.markblokpoel.lanag.core.Data]].
  *                   Default value is [[com.markblokpoel.lanag.core.NoData]].
  * @param maxTurns   The maximum number of turns, i.e., the number of signals produced, in this interaction.
  * @author Mark Blokpoel
  */
case class RSA1ShotInteraction(
    override val agent1: RSA1ShotAgent,
    override val agent2: RSA1ShotAgent,
    override val originData: OriginData = OriginData(),
    override val pairId: Long = InteractionIdentifier.nextId,
    maxTurns: Int = 10)
    extends Interaction[ReferentialIntention, ContentSignal, RSA1ShotAgent](
      agent1,
      agent2,
      originData) {

  override type SpeakerType = RSA1ShotSpeaker
  override type ListenerType = RSA1ShotListener

  override val agent1AsSpeaker: RSA1ShotSpeaker = agent1.asSpeaker
  override val agent2AsSpeaker: RSA1ShotSpeaker = agent2.asSpeaker
  override val agent1AsListener: RSA1ShotListener = agent1.asListener
  override val agent2AsListener: RSA1ShotListener = agent2.asListener
  override protected val currentSpeaker: RSA1ShotSpeaker = agent1AsSpeaker
  override protected val currentListener: RSA1ShotListener = agent2AsListener
  private var turnCount = 0

  def atOrder(order: Int): RSA1ShotInteraction = RSA1ShotInteraction(
    agent1.withOrder(order),
    agent2.withOrder(order),
    originData,
    pairId,
    maxTurns
  )

  override def stoppingCriterion: Boolean = turnCount >= maxTurns

  /** Execures a turn in this 1-shot director matcher task and switches roles.
    *
    * @return Data reflecting the results of this turn, includes success and production and interpretation entropy.
    */
  override def turn: TurnData = {
    turnCount += 1
    val intention = currentSpeaker.selectIntention
    val (signal, speakerData) = currentSpeaker.produceSignal(intention)

    if (signal.isDefined) {
      val (referent, listenerData) = currentListener.interpretSignal(signal)
      val success = intention == referent
      switchRoles()
      TurnData(turnCount - 1, success, speakerData, listenerData)
    } else {
      TurnData(turnCount - 1,
                   success = false,
                   speakerData,
                   ListenerData(None))
    }
  }

  /** Simulates all turns in the interaction and returns a collection of all the data gathered, including:
    * | name | description |
    * | --- | --- |
    * | <code>pairId: Long</code> | a number uniquely identifying this interaction pair |
    * | <code>agent1Order: Int</code> | the order of pragmatic inference of agent 1 |
    * | <code>agent2Order: Int</code> | the order of pragmatic inference of agent 2 |
    * | <code>agent1AmbiguityMean: Double</code> | the mean ambiguity of agent 1's lexicon |
    * | <code>agent1AmbiguityVar: Double</code> | the variance ambiguity of agent 1's lexicon |
    * | <code>agent2AmbiguityMean: Double</code> | the mean ambiguity of agent 2's lexicon |
    * | <code>agent2AmbiguityVar: Double</code> | the variance ambiguity of agent 2's lexicon |
    * | <code>asymmetry: Double</code> | the asymmetry between the two agent's lexicons |
    * | <code>originData: RSA1OriginData</code> | data reflecting the parameters under which the agent's lexicons have been generated |
    * | <code>interaction: Seq[RSA1TurnData]</code> | sequence of data containing the entropy and success measurements from the turns of this interaction |
    */
  override def runAndCollectData: InteractionData = {
    var turnData = Seq.empty[TurnData]
    while (!stoppingCriterion) turnData = turnData :+ turn

    val (agent1AmbiguityMean, agent1AmbiguityVar) =
      agent1.originalLexicon.meanAndVarianceAmbiguity()
    val (agent2AmbiguityMean, agent2AmbiguityVar) =
      agent2.originalLexicon.meanAndVarianceAmbiguity()
    val asymmetry = agent1.originalLexicon.asymmetryWith(agent2.originalLexicon)
    InteractionData(
      pairId,
      agent1.order,
      agent2.order,
      agent1AmbiguityMean,
      agent1AmbiguityVar,
      agent2AmbiguityMean,
      agent2AmbiguityVar,
      asymmetry,
      originData,
      turnData
    )
  }
}
