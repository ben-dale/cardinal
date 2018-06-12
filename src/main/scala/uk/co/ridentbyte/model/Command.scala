package uk.co.ridentbyte.model

import java.util.Random

import scala.util.matching.Regex

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

  def rawCommand: String = command

  def processWith(constants: Map[String, () => String], vocabulary: Vocabulary, prev: Option[String] = None, r: Random = new Random()): String = {
    val rawContents = prev.getOrElse(contents)
    val optDirectlyMatchedVar = constants.find(_._1 == rawContents)
    if (optDirectlyMatchedVar.isDefined) {
      optDirectlyMatchedVar.get._2()
    } else {
      // Not a variable, process functions

      val functions = List[Regex](
        "(?:lower\\((.*)\\))".r,
        "(?:upper\\((.*)\\))".r,
        "(?:capitalise\\((.*)\\))".r,
        "(?:random\\((.*)\\))".r,
        "(?:lorem\\((.*)\\))".r
      )


      val lowerFunction = "(?:lower\\((.*)\\))".r
      val upperFunction = "(?:upper\\((.*)\\))".r
      val capitaliseFunction = "(?:capitalise\\((.*)\\))".r
      val randomFunction = "(?:random\\((.*)\\))".r
      val loremFunction = "(?:lorem\\((.*)\\))".r

      val rawString = "\"[^\"]*\""

      rawContents match {
        case lowerFunction(v) if v.matches(rawString) => v.replace("\"", "").toLowerCase()
        case lowerFunction(v) if !v.matches(rawString) => processWith(constants, vocabulary, Some(v)).toLowerCase
        case upperFunction(v) if v.matches(rawString) => v.replace("\"", "").toUpperCase()
        case upperFunction(v) if !v.matches(rawString) => processWith(constants, vocabulary, Some(v)).toUpperCase
        case capitaliseFunction(v) if v.matches(rawString) => v.replace("\"", "").capitalize
        case capitaliseFunction(v) if !v.matches(rawString) => processWith(constants, vocabulary, Some(v)).capitalize
        case loremFunction(n) if isInt(n) => vocabulary.loremIpsum.first(n.toInt).mkString(" ")
        case loremFunction(n) if !isInt(n) =>
          val processedContents = processWith(constants, vocabulary, Some(n))
          if (isInt(processedContents)) {
            vocabulary.loremIpsum.first(processedContents.toInt).mkString(" ")
          } else {
            processedContents
          }
        case randomFunction(v) =>
          val splitValues = v.split(",").map(_.trim).toList
          val argumentTypes = getArgumentTypes(splitValues, constants, functions)
          val containsInvalidTypes = argumentTypes.count(n => n.isInstanceOf[IntLiteral] || n.isInstanceOf[InvalidLiteral]) > 0
          if (!containsInvalidTypes) {
            val processedValues = splitValues.map { v => processWith(constants, vocabulary, Some(v)) }
            processedValues(r.nextInt(processedValues.length))
          } else {
            command
          }
        case v if v.matches(rawString) => v.replace("\"", "")
        case v => v
      }
    }
  }

  def getArgumentTypes(args: List[String], constants: Map[String, () => String], functions: List[Regex]): List[ArgumentType] = {
    args.map {
      case a if a.matches("\"[^\"]*\"") => StringLiteral(a)
      case i if i.matches("[0-9]+") => IntLiteral(i)
      case c if constants.contains(c) => Constant(c)
      case f if functions.exists(f2 => f.matches(f2.regex)) => Function(f)
      case n => InvalidLiteral(n)
    }
  }

  def isInt(s: String): Boolean = {
    try {
      s.toInt
      true
    } catch {
      case _: Exception => false
    }
  }

}

class ArgumentType(value: String)
case class Function(value: String) extends ArgumentType(value)
case class Constant(value: String) extends ArgumentType(value)
case class StringLiteral(value: String) extends ArgumentType(value)
case class IntLiteral(value: String) extends ArgumentType(value)
case class InvalidLiteral(value: String) extends ArgumentType(value)