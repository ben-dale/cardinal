package uk.co.ridentbyte.model

import uk.co.ridentbyte.Cardinal

case class Curl(uri: String, verb: String, body: Option[String], headers: List[String], envVars: Map[String, String]) {

  def toCommand: String = {
    val sb = new StringBuilder
    sb.append("curl ")

    headers.foreach { header =>
      sb.append(s"""-H '${RequestString(header, envVars, Cardinal.firstNames, Cardinal.lastNames).process}' """)
    }

    body.foreach { b =>
      sb.append(s"""-d '${RequestString(b, envVars, Cardinal.firstNames, Cardinal.lastNames).process}' """)
    }

    sb.append(s"""-X $verb ${RequestString(uri, envVars, Cardinal.firstNames, Cardinal.lastNames).process}""")

    sb.toString()
  }

}
