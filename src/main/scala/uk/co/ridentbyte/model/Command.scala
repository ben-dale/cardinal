package uk.co.ridentbyte.model

import java.util.Random

/**
  * Handles commands in the format: #{(.*)}
  *
  * @param command - Raw command
  */
case class Command(private val command: String) {

  /**
    * Extract contents from command
    */
  def contents: String = {
    "#\\{(.*)\\}".r
      .findFirstMatchIn(Option(command).getOrElse(""))
      .map(_.group(1))
      .getOrElse(command)
  }

  def rawCommand = command

  def processWith(variables: Map[String, () => String], prev: Option[String] = None, r: Random = new Random()): String = {
    val rawContents = prev.getOrElse(contents)
    val optDirectlyMatchedVar = variables.find(_._1 == rawContents)
    if (optDirectlyMatchedVar.isDefined) {
      optDirectlyMatchedVar.get._2()
    } else {
      // Not a variable, process functions

      val lowerFunction = "(?:lower\\((.*)\\))".r
      val upperFunction = "(?:upper\\((.*)\\))".r
      val capitaliseFunction = "(?:capitalise\\((.*)\\))".r
      val randomFunction = "(?:random\\((.*)\\))".r

      val rawString = "\"[^\"]+\""

      rawContents match {
        case lowerFunction(v) if v.matches(rawString) => v.replace("\"", "").toLowerCase()
        case lowerFunction(v) if !v.matches(rawString) => processWith(variables, Some(v)).toLowerCase
        case upperFunction(v) if v.matches(rawString) => v.replace("\"", "").toUpperCase()
        case upperFunction(v) if !v.matches(rawString) => processWith(variables, Some(v)).toUpperCase
        case capitaliseFunction(v) if v.matches(rawString) => v.replace("\"", "").capitalize
        case capitaliseFunction(v) if !v.matches(rawString) => processWith(variables, Some(v)).capitalize
        case randomFunction(v) =>
          val splitValues = v.split(",")
          val processedValues = splitValues.map { v => processWith(variables, Some(v.trim)) }
          processedValues(r.nextInt(processedValues.length))
        case v if v.matches(rawString) => v.replace("\"", "")
      }
    }
  }

}
