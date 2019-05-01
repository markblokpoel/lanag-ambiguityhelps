import com.markblokpoel.lanag.ambiguityhelps.{RSA1ShotAgent, RSA1ShotInteraction}
import com.markblokpoel.lanag.rsa.Lexicon

val map1 = Vector[Vector[Double]](
  Vector(1,1,0),
  Vector(0,1,0),
  Vector(0,1,1),
  Vector(1,0,1)
)
val lex1 = Lexicon(map1)
val smith = new RSA1ShotAgent(lex1, 1)
println(smith.asSpeaker.inferredLexicon)
val int = smith.asSpeaker.selectIntention
val intentionDistribution =
  Vector.tabulate(3)(n => if (n == int.content.get) 1.0 else 0.0)
val post = smith.asSpeaker.inferredLexicon dot intentionDistribution


//import com.markblokpoel.lanag.ambiguityhelps.RSA1ShotInteraction
//import com.markblokpoel.lanag.ambiguityhelps.experiments.ConsistentPairGenerator
//import com.markblokpoel.lanag.rsa.Lexicon
//
//def printLex2(l1: Lexicon, l2: Lexicon): Unit = {
//  for(r <- 0 until l1.vocabularySize) {
//    val r1 = l1.getRow(r)
//    val r2 = l2.getRow(r)
//    val l = (r1 zip r2).map(e => if(e._1 == e._2) e._1.toString + " " else e._1 + "!")
//    println(l.mkString("\t"))
//  }
//}


//val gen = new ConsistentPairGenerator(8,4,0.2, 5)
//val space = gen.generateParameterSpace
//space.foreach(s => {
//  val b = gen.generatePair(s)
//  println("Asymmetry: "+b.agent1.originalLexicon.asymmetryWith(b.agent2.originalLexicon))
//  printLex2(b.agent1.originalLexicon, b.agent2.originalLexicon)
//  println("")
//
//  val interaction = RSA1ShotInteraction(b.agent1, b.agent2, b.originData)
//  val interaction0 = interaction.atOrder(0)
//  val interaction1 = interaction.atOrder(1)
//
//  var succ = 0
//  (for(_ <- 0 until interaction0.maxTurns) yield interaction0.turn).foreach(d => {
//    println(d.turn.toString + ":\t" + d.success)
//    if(d.success) succ += 1
//  })
//  println("avg success: " + succ)
//  succ=0
//  (for(_ <- 0 until interaction1.maxTurns) yield interaction1.turn).foreach(d => {
//    println(d.turn.toString + ":\t" + d.success)
//    if(d.success) succ += 1
//  })
//  println("avg success: " + succ)
//})

//val data2 = data.map(i => i.interaction.count(d => d.success).toDouble / i.interaction.length.toDouble)

//println(data.mkString("\n"))


//
//val map2 = Vector[Vector[Double]](
//  Vector(1,1,0),
//  Vector(0,1,0),
//  Vector(0,1,1)
//)
//val lex2 = Lexicon(map2)
//val asymmetry = lex1.asymmetryWith(lex2)
//
//val order = 2
//
//val neo = new RSA1ShotAgent(lex2, 0)
//
//
//val interaction = RSA1ShotInteraction(smith, neo, maxTurns = 50)
//val interaction0 = interaction.atOrder(0)
//val interaction1 = interaction.atOrder(1)
//
//val data0 = interaction0.runAndCollectData
//val data1 = interaction1.runAndCollectData
//
//val success0= data0.interaction.count(d => d.success)
//val success1= data1.interaction.count(d => d.success)
//
////(for(_ <- 0 until interaction0.maxTurns) yield interaction0.turn).foreach(d => {
////  println(d.turn.toString + ":\t" + d.success
////    + "\t ent_s=" + d.speakerData.speakerEntropy.getOrElse("n.a.")
////    + "\t ent_l=" + d.listenerData.listenerEntropy.getOrElse("n.a."))
////})
////
////val interaction1 = interaction0.atOrder(10)
////(for(_ <- 0 until interaction1.maxTurns) yield interaction1.turn).foreach(d => {
////  println(d.turn.toString + ":\t" + d.success
////    + "\t ent_s=" + d.speakerData.speakerEntropy.getOrElse("n.a.")
////    + "\t ent_l=" + d.listenerData.listenerEntropy.getOrElse("n.a."))
////})