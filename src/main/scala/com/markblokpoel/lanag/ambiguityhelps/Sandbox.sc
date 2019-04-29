import com.markblokpoel.lanag.ambiguityhelps.{RSA1ShotAgent, RSA1ShotInteraction}
import com.markblokpoel.lanag.rsa.Lexicon

val map = Vector[Vector[Double]](
  Vector(1,1,0),
  Vector(0,1,0),
  Vector(0,1,1)
)
val matrix = Lexicon(map)
val order = 2
val smith = new RSA1ShotAgent(matrix, order)

smith.asSpeaker.inferredLexicon

val neo = new RSA1ShotAgent(matrix, order)
neo.asListener.inferredLexicon

val interaction = RSA1ShotInteraction(smith, neo)

(for(_ <- 0 until interaction.maxTurns) yield interaction.turn).foreach(d => {
  println(d.turn.toString + ":\t" + d.success
    + "\t ent_s=" + d.speakerData.speakerEntropy.getOrElse("n.a.")
    + "\t ent_l=" + d.listenerData.listenerEntropy.getOrElse("n.a."))
})