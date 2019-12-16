package com.markblokpoel.lanag.ambiguityhelps

import com.markblokpoel.lanag.core.{Agent, ContentSignal, ReferentialIntention}
import com.markblokpoel.lanag.rsa.Lexicon

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

  override def toString: String = {
    if (originalLexicon.data.forall(v => v == 0.0 || v == 1.0)) {
      val id =
        Integer.parseInt(originalLexicon.data.map(_.intValue).mkString(""), 2)
      s"[RSA1SA-O$order-ID#$id]"
    } else
      s"Agent with order $order\n" + originalLexicon.toString
  }

  def canEqual(that: Any): Boolean = that.isInstanceOf[RSA1ShotAgent]

  override def equals(that: Any): Boolean = that match {
    case that: RSA1ShotAgent =>
      that.canEqual(this) &&
        that.originalLexicon == this.originalLexicon &&
        that.order == this.order
    case _ => false
  }

  override def hashCode(): Int = this.originalLexicon.hashCode() * 31 + order

}
