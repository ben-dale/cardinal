package uk.co.ridentbyte.model

import scala.collection.JavaConverters._

case class ExtractedCommands(private val data: String) {

  def all: java.util.List[Command] = {
    "(#\\{[^\\{|\\}]+\\})".r
      .findAllMatchIn(Option(data).getOrElse(""))
      .map(c => Command(c.group(0)))
      .toList
      .asJava
  }

}
