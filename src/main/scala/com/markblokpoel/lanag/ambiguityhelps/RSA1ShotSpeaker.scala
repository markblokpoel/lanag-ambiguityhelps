package com.markblokpoel.lanag.ambiguityhelps

import com.markblokpoel.lanag.ambiguityhelps.datastructures.SpeakerData
import com.markblokpoel.lanag.core.{
  ContentSignal,
  ReferentialIntention,
  Speaker
}
import com.markblokpoel.lanag.math.Probability
import com.markblokpoel.lanag.rsa.Lexicon

import scala.util.Random

/** The speaker type for 1-shot Rational Speech Act agents.
  *
  * @param originalLexicon The original (initial) lexicon of the agent.
  * @param order           The order of the agent as speaker.
  * @author Mark Blokpoel
  */
@SerialVersionUID(100L)
case class RSA1ShotSpeaker(override val originalLexicon: Lexicon,
                           override val order: Int)
    extends RSA1ShotAgent(originalLexicon, order)
    with Speaker[ReferentialIntention, ContentSignal]
    with Serializable {

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
      Vector.tabulate(contextSize)(n => if (n == referentIndex) 1.0 else 0.0)
    inferredLexicon dot intentionDistribution
  }

  /** Returns a signal for a given intention, based on the Rational Speech Act theory and the agent's
    * order and beta parameter.
    *
    * @param intention The [[com.markblokpoel.lanag.core.ReferentialIntention]] to be communicated.
    * @return A tuple that consisting of the [[com.markblokpoel.lanag.core.ContentSignal]] selected by
    *         the communicator and [[com.markblokpoel.lanag.ambiguityhelps.datastructures.SpeakerData]] that contains
    *         the posterior distribution's entropy.
    */
  override def produceSignal(
      intention: ReferentialIntention): (ContentSignal, SpeakerData) = {
    if (intention.isDefined) {
      val posterior = posteriorSignalDistribution(intention.content.get)
      val signalIndex = Probability.softArgMax(posterior, beta)
      val signal = ContentSignal(signalIndex)
      (signal, SpeakerData(Some(Probability.entropy(posterior))))
    } else
      (ContentSignal(None), SpeakerData(None))
  }

  override def canEqual(that: Any): Boolean = that.isInstanceOf[RSA1ShotSpeaker]

  override def equals(that: Any): Boolean = that match {
    case that: RSA1ShotSpeaker =>
      that.canEqual(this) &&
        that.originalLexicon == this.originalLexicon &&
        that.order == this.order
    case _ => false
  }

  override def hashCode(): Int = this.originalLexicon.hashCode() * 31 + order
}
