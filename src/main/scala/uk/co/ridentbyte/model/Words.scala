package uk.co.ridentbyte.model

import scala.util.Random

case class Words(private val values: List[String], private val r: Random) {

  def random(): String = values(r.nextInt(values.length))

  def first(n: Int): List[String] = values.take(n)

}
