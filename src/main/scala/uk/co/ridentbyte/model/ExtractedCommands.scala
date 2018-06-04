package uk.co.ridentbyte.model

case class ExtractedCommands(private val data: String) {

  def all: List[Command] = {
    "(#\\{[^\\{|\\}]+\\})".r
      .findAllMatchIn(Option(data).getOrElse(""))
      .map(c => Command(c.group(0)))
      .toList
  }

}
