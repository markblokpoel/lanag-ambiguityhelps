package com.markblokpoel.lanag.ambiguityhelps.jupyterdisplayers

import com.markblokpoel.lanag.ambiguityhelps._
import com.markblokpoel.lanag.rsa.Lexicon
import jupyter.Displayers
import scala.collection.JavaConverters._

object RSA1ShotDisplayers {
  Displayers.register(
    classOf[RSA1ShotAgent],
    (agent: RSA1ShotAgent) => {
      import scalatags.Text.all._
      Map(
        "text/html" -> {
          table(cls := "table")(
            tr(td(strong("RSA 1-shot Agent")),
               td(colspan := agent.originalLexicon.contextSize)),
            tr(td(strong("Order")),
               td(agent.order),
               td(colspan := agent.originalLexicon.contextSize - 1)),
            tr(td(strong("Beta")),
               td(agent.beta),
               td(colspan := agent.originalLexicon.contextSize - 1)),
            agent.originalLexicon match {
              case Lexicon(vocabularySize, contextSize, data) =>
                Vector(
                  tr(td(), for (i <- 1 to contextSize) yield th("R", sub(i))))
                  .union(for (i <- 0 until vocabularySize) yield {
                    tr(
                      td(strong("V", sub(i + 1))),
                      for (value <- data.slice(i * contextSize,
                                               (1 + i) * contextSize))
                        yield td(value)
                    )
                  })
              case _ => tr(td("Error: Value is not of type Lexicon."))
            }
          ).render
        }
      ).asJava
    }
  )

  Displayers.register(
    classOf[RSA1ShotListener],
    (agent: RSA1ShotListener) => {
      import scalatags.Text.all._
      Map(
        "text/html" -> {
          table(cls := "table")(
            tr(td(strong("RSA 1-shot Listener")),
               td(colspan := agent.originalLexicon.contextSize)),
            tr(td(strong("Order")),
               td(agent.order),
               td(colspan := agent.originalLexicon.contextSize - 1)),
            tr(td(strong("Beta")),
               td(agent.beta),
               td(colspan := agent.originalLexicon.contextSize - 1)),
            agent.inferredLexicon match {
              case Lexicon(vocabularySize, contextSize, data) =>
                Vector(
                  tr(td(), for (i <- 1 to contextSize) yield th("R", sub(i))))
                  .union(for (i <- 0 until vocabularySize) yield {
                    tr(
                      td(strong("V", sub(i + 1))),
                      for (value <- data.slice(i * contextSize,
                                               (1 + i) * contextSize))
                        yield td(value)
                    )
                  })
              case _ => tr(td("Error: Value is not of type Lexicon."))
            }
          ).render
        }
      ).asJava
    }
  )

  Displayers.register(
    classOf[RSA1ShotSpeaker],
    (agent: RSA1ShotSpeaker) => {
      import scalatags.Text.all._
      Map(
        "text/html" -> {
          table(cls := "table")(
            tr(td(strong("RSA 1-shot Speaker")),
               td(colspan := agent.originalLexicon.contextSize)),
            tr(td(strong("Order")),
               td(agent.order),
               td(colspan := agent.originalLexicon.contextSize - 1)),
            tr(td(strong("Beta")),
               td(agent.beta),
               td(colspan := agent.originalLexicon.contextSize - 1)),
            agent.inferredLexicon match {
              case Lexicon(vocabularySize, contextSize, data) =>
                Vector(
                  tr(td(), for (i <- 1 to contextSize) yield th("R", sub(i))))
                  .union(for (i <- 0 until vocabularySize) yield {
                    tr(
                      td(strong("V", sub(i + 1))),
                      for (value <- data.slice(i * contextSize,
                                               (1 + i) * contextSize))
                        yield td(value)
                    )
                  })
              case _ => tr(td("Error: Value is not of type Lexicon."))
            }
          ).render
        }
      ).asJava
    }
  )
}
