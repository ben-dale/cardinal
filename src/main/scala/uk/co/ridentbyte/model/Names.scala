package uk.co.ridentbyte.model

import scala.util.Random

case class Names(private val names: List[String]) {

  def getRandom: String = names(Random.nextInt(names.length))

}
