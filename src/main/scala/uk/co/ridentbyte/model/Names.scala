package uk.co.ridentbyte.model

import scala.util.Random

case class Names(private val names: List[String], random: Random) {

  def getRandom: String = names(random.nextInt(names.length))

}
