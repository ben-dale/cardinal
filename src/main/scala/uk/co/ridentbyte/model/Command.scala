package uk.co.ridentbyte.model

import java.util.Random

import scala.util.matching.Regex
import scala.collection.JavaConverters._

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
        "(?:lorem\\((.*)\\))".r,
        "(?:randomBetween\\((.*)\\))".r
      )

      val lowerFunction = "(?:lower\\((.*)\\))".r
      val upperFunction = "(?:upper\\((.*)\\))".r
      val capitaliseFunction = "(?:capitalise\\((.*)\\))".r
      val randomFunction = "(?:random\\((.*)\\))".r
      val loremFunction = "(?:lorem\\((.*)\\))".r
      val randomBetween = "(?:randomBetween\\((.*)\\))".r

      val rawString = "\"[^\"]*\""

      rawContents match {
        case lowerFunction(v) if v.matches(rawString) => v.replace("\"", "").toLowerCase()
        case lowerFunction(v) if !v.matches(rawString) => processWith(constants, vocabulary, Some(v)).toLowerCase
        case upperFunction(v) if v.matches(rawString) => v.replace("\"", "").toUpperCase()
        case upperFunction(v) if !v.matches(rawString) => processWith(constants, vocabulary, Some(v)).toUpperCase
        case capitaliseFunction(v) if v.matches(rawString) => v.replace("\"", "").capitalize
        case capitaliseFunction(v) if !v.matches(rawString) => processWith(constants, vocabulary, Some(v)).capitalize
        case loremFunction(n) =>
          val argumentType = getArgumentTypes(List(n), constants, functions).head
          if (argumentType.isInstanceOf[IntLiteral]) {
            vocabulary.loremIpsum.first(n.toInt).asScala.mkString(" ")
          } else if (argumentType.isInstanceOf[Function]) {
            val processedValue = processWith(constants, vocabulary, Some(n))
            val processedValueArgumentType = getArgumentTypes(List(processedValue), constants, functions)
            if (processedValueArgumentType.head.isInstanceOf[IntLiteral]) {
              vocabulary.loremIpsum.first(processedValue.toInt).asScala.mkString(" ")
            } else {
              rawContents
            }
          } else {
            rawContents
          }

        case randomFunction(v) =>
          // TODO - need to improve text splitting here
          // For example we want #{random(random(1, 2), random(3, 4))} to be valid
          val splitValues = v.split(",").map(_.trim).toList
          val argumentTypes = getArgumentTypes(splitValues, constants, functions)
          val containsInvalidTypes = argumentTypes.count(n => n.isInstanceOf[InvalidLiteral]) > 0
          if (!containsInvalidTypes) {
            val processedValues = splitValues.map { v => processWith(constants, vocabulary, Some(v)) }
            processedValues(r.nextInt(processedValues.length))
          } else {
            rawContents
          }

        case randomBetween(v) =>
          val splitValues = v.split(",").map(_.trim).toList
          val argumentTypes = getArgumentTypes(splitValues, constants, functions)
          val containsInvalidTypes = argumentTypes.count(n => n.isInstanceOf[InvalidLiteral]) > 0
          if (!containsInvalidTypes && splitValues.length == 2) {
            try {
              val processedValues = splitValues.map { v => processWith(constants, vocabulary, Some(v)) }
              val allPossibleValues = processedValues.head.toInt.to(processedValues.last.toInt)
              allPossibleValues(r.nextInt(allPossibleValues.length)).toString
            } catch {
              case _: Exception => rawContents
            }
          } else {
            rawContents
          }

        case v if v.matches(rawString) => v.replace("\"", "")
        case v => v
      }
    }
  }

  def getArgumentTypes(args: List[String], constants: Map[String, () => String], functions: List[Regex]): List[ArgumentType] = {
    args.map {
      case a if a.matches("\"[^\"]*\"") => StringLiteral(a)
      case i if i.matches("[\\-]?[0-9]+") => IntLiteral(i)
      case c if constants.contains(c) => Constant(c)
      case f if functions.exists(f2 => f.matches(f2.regex)) => Function(f)
      case n => InvalidLiteral(n)
    }
  }

  def isRawInt(s: String): Boolean = s.matches("[0-9]+")

}

class ArgumentType(value: String)
case class Function(value: String) extends ArgumentType(value)
case class Constant(value: String) extends ArgumentType(value)
case class StringLiteral(value: String) extends ArgumentType(value)
case class IntLiteral(value: String) extends ArgumentType(value)
case class InvalidLiteral(value: String) extends ArgumentType(value)