package uk.co.ridentbyte.model

case class FormUrlEncodedParameters(parameters: List[String]) {
  def toBodyString: String = parameters.mkString("&")
  def toBodyEditString: String = parameters.mkString("\n")
}

object FormUrlEncodedParameters {
  def apply(value: String): FormUrlEncodedParameters = {
    FormUrlEncodedParameters(value.split("&").toList)
  }
}
