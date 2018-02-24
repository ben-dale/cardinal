package uk.co.ridentbyte.model

case class Request(uri: String, verb: String, headers: List[String], body: Option[String])
