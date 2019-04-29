package com.markblokpoel.lanag.ambiguityhelps

import com.markblokpoel.lanag.ambiguityhelps.RSA1ShotDataStructures.{
  ListenerData,
  SpeakerData
}
import com.markblokpoel.lanag.core.{
  Agent,
  ContentSignal,
  Listener,
  ReferentialIntention,
  Speaker
}
import com.markblokpoel.lanag.math.Probability
import com.markblokpoel.lanag.rsa.Lexicon

import scala.util.Random

/** The base agent type for 1-shot Rational Speech Act agents (Frank & Goodman, 2012).
  *
  * @param originalLexicon The original (initial) lexicon of the agent.
  * @param order           The default order of the agent (default to 0).
  * @param beta            The decision noise parameter. Used in the signal and referent inference, which is
  *                        based on the softargmax function. Defaults to <code>PositiveInfinity</code>, making
  *                        softargmax equivalent to argmax (i.e., purely rational agents).
  * @author Mark Blokpoel
  */
@SerialVersionUID(100L)
class RSA1ShotAgent(val originalLexicon: Lexicon,
                    val order: Int = 0,
                    val beta: Double = Double.PositiveInfinity)
    extends Agent[ReferentialIntention, ContentSignal]
    with Serializable {

  /** The vocabulary size of the agent's lexicon. */
  def vocabularySize: Int = originalLexicon.vocabularySize

  /** The context size of the agent's lexicon. */
  def contextSize: Int = originalLexicon.contextSize

  /** Returns an immutable copy of this agent with a different order.
    *
    * @param n The order of pragmatic inference used by the agent.
    */
  def withOrder(n: Int): RSA1ShotAgent = new RSA1ShotAgent(originalLexicon, n)

  override def asSpeaker: RSA1ShotSpeaker =
    RSA1ShotSpeaker(originalLexicon, order)

  override def asListener: RSA1ShotListener =
    RSA1ShotListener(originalLexicon, order)

  override def toString: String =
    s"Agent with order $order\n" + originalLexicon.toString
}

/** The speaker type for 1-shot Rational Speech Act agents.
  *
  * @param originalLexicon The original (initial) lexicon of the agent.
  * @param order           The order of the agent as speaker.
  * @author Mark Blokpoel
  */
case class RSA1ShotSpeaker(override val originalLexicon: Lexicon,
                           override val order: Int)
    extends RSA1ShotAgent(originalLexicon, order)
    with Speaker[ReferentialIntention, ContentSignal] {

  /** The lexicon after pragmatic reasoning at the level of the speaker's <code>order</code>. */
  lazy val inferredLexicon: Lexicon = originalLexicon.setOrderAsSpeaker(order)

  /** Returns a random referential intention. */
  def selectIntention: ReferentialIntention =
    ReferentialIntention(Some(Random.nextInt(originalLexicon.contextSize)))

  /** Computes a posterior distribution over signals, given a referent index.
    *
    * @param referentIndex An index pointing to the referent in the lexicon context.
    */
  private def posteriorSignalDistribution(referentIndex: Int) = {
    require(referentIndex >= 0 && referentIndex < inferredLexicon.contextSize,
            "Referent index out of bounds.")
    val intentionDistribution =
      Vector.tabulate(vocabularySize)(n => if (n == referentIndex) 1.0 else 0.0)
    inferredLexicon dotT intentionDistribution
  }

  /** Returns a signal for a given intention, based on the Rational Speech Act theory and the agent's
    * order and beta parameter.
    *
    * @param intention The [[com.markblokpoel.lanag.core.ReferentialIntention]] to be communicated.
    * @return A tuple that consisting of the [[com.markblokpoel.lanag.core.ContentSignal]] selected by
    *         the communicator and [[RSA1ShotDataStructures.SpeakerData]] that contains
    *         the posterior distribution's entropy.
    */
  override def produceSignal(
      intention: ReferentialIntention): (ContentSignal, SpeakerData) = {
    if (intention.isDefined) {
      val posterior = posteriorSignalDistribution(intention.content.get)
      val signalIndex = Probability.softArgMax(posterior, beta)
      val signal = ContentSignal(signalIndex)
      return (signal, SpeakerData(Some(Probability.entropy(posterior))))
    }
    (ContentSignal(None), SpeakerData(None))
  }
}

/** The listener type for 1-shot Rational Speech Act agents.
  *
  * @param originalLexicon The original (initial) lexicon of the agent.
  * @param order           The order of the agent as listener.
  * @author Mark Blokpoel
  */
case class RSA1ShotListener(override val originalLexicon: Lexicon,
                            override val order: Int)
    extends RSA1ShotAgent(originalLexicon, order)
    with Listener[ReferentialIntention, ContentSignal] {

  /** The lexicon after pragmatic reasoning at the level of the listener's <code>order</code>. */
  lazy val inferredLexicon: Lexicon = originalLexicon.setOrderAsListener(order)

  /** Computes a posterior distribution over referents, given a signal index.
    *
    * @param signalIndex An index pointing to the referent in the lexicon context.
    */
  private def posteriorReferentDistribution(
      signalIndex: Int): Vector[Double] = {
    require(signalIndex >= 0 && signalIndex <= inferredLexicon.vocabularySize,
            "Signal index out of bounds")
    val signalDistribution =
      Vector.tabulate(contextSize)(n => if (n == signalIndex) 1.0 else 0.0)
    inferredLexicon dot signalDistribution
  }

  /** Returns an intention for a given signal, based on the Rational Speech Act theory and the agent's
    * order and beta parameter.
    *
    * @param signal The [[com.markblokpoel.lanag.core.ContentSignal]] to be communicated.
    * @return A tuple that consisting of the [[com.markblokpoel.lanag.core.ReferentialIntention]] selected by
    *         the communicator and [[RSA1ShotDataStructures.ListenerData]] that contains
    *         the posterior distribution's entropy.
    */
  override def interpretSignal(
      signal: ContentSignal): (ReferentialIntention, ListenerData) = {
    if (signal.isDefined) {
      val posterior = posteriorReferentDistribution(signal.content.get)
      val referentIndex = Probability.softArgMax(posterior, beta)
      val referent = ReferentialIntention(referentIndex)
      return (referent, ListenerData(Some(Probability.entropy(posterior))))
    }
    (ReferentialIntention(None), ListenerData(None))
  }
}
