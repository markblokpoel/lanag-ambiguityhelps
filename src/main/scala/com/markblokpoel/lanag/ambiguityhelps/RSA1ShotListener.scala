package com.markblokpoel.lanag.ambiguityhelps

import com.markblokpoel.lanag.ambiguityhelps.datastructures.ListenerData
import com.markblokpoel.lanag.core.{
  ContentSignal,
  Listener,
  ReferentialIntention
}
import com.markblokpoel.lanag.math.Probability
import com.markblokpoel.lanag.rsa.Lexicon

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
      Vector.tabulate(vocabularySize)(n => if (n == signalIndex) 1.0 else 0.0)
    inferredLexicon dotT signalDistribution
  }

  /** Returns an intention for a given signal, based on the Rational Speech Act theory and the agent's
    * order and beta parameter.
    *
    * @param signal The [[com.markblokpoel.lanag.core.ContentSignal]] to be communicated.
    * @return A tuple that consisting of the [[com.markblokpoel.lanag.core.ReferentialIntention]] selected by
    *         the communicator and [[com.markblokpoel.lanag.ambiguityhelps.datastructures.ListenerData]] that contains
    *         the posterior distribution's entropy.
    */
  override def interpretSignal(
      signal: ContentSignal): (ReferentialIntention, ListenerData) = {
    if (signal.isDefined) {
      val posterior = posteriorReferentDistribution(signal.content.get)
      val referentIndex = Probability.softArgMax(posterior, beta)
      val referent = ReferentialIntention(referentIndex)
      (referent, ListenerData(Some(Probability.entropy(posterior))))
    } else
      (ReferentialIntention(None), ListenerData(None))
  }
}
