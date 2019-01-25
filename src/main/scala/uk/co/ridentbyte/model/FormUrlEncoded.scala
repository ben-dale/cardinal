package uk.co.ridentbyte.model


case class FormUrlEncoded(private val parameters: List[String]) {
  override def toString: String = parameters.mkString("&")
  def lines: String = parameters.mkString("\n")
  def header: String = "Content-Type: application/x-www-form-urlencoded"
}

object FormUrlEncoded {
  def apply(value: String): FormUrlEncoded = {
    FormUrlEncoded(value.split("&").toList)
  }

  def apply(value: Array[String]): FormUrlEncoded = {
    FormUrlEncoded(value.toList)
  }
}
