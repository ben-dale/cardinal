package uk.co.ridentbyte.model

import scala.util.Random

case class Names(private val names: List[String], private val r: Random) {

  def random(): String = names(r.nextInt(names.length))

  def randomLower(): String = names(r.nextInt(names.length)).toLowerCase

}
