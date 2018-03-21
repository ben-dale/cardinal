package uk.co.ridentbyte.model

import java.util.UUID

import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization.writePretty
import uk.co.ridentbyte.util.{LastNames, Names}

import scala.util.Random

case class Request(uri: String, verb: String, headers: List[String], body: Option[String]) {
  private implicit val formats: DefaultFormats = DefaultFormats

  def toJson: String = writePretty(this)

  def withId(id: String): Request = {
    Request(
      uri.replaceAll("#\\{id\\}", id),
      verb,
      headers,
      if (body.isDefined) Some(body.get.replaceAll("#\\{id\\}", id)) else None
    )
  }

  def processConstants(): Request = {
    val newUri = parseAndReplaceConstants(uri)
    val newBody = body match {
      case Some(b) => Some(parseAndReplaceConstants(b))
      case _  => None
    }
    Request(newUri, verb, headers, newBody)
  }

  private def parseAndReplaceConstants(content: String): String = {
    var contentCopy = content

    val guid = UUID.randomUUID.toString.split("-")(0)
    val int = Math.abs(Random.nextInt).toString
    val float = Math.abs(Random.nextFloat).toString
    val firstName = Names.getRandom
    val lastName = LastNames.getRandom

    // Constants
    contentCopy = contentCopy.replaceAll("#\\{guid\\}", guid)
    contentCopy = contentCopy.replaceAll("#\\{int\\}", int)
    contentCopy = contentCopy.replaceAll("#\\{float\\}", float)
    contentCopy = contentCopy.replaceAll("#\\{firstName\\}", firstName)
    contentCopy = contentCopy.replaceAll("#\\{firstNameLower\\}", firstName.toLowerCase)
    contentCopy = contentCopy.replaceAll("#\\{lastName\\}", lastName)
    contentCopy = contentCopy.replaceAll("#\\{lastNameLower\\}", lastName.toLowerCase)

    // Random values
    contentCopy = replaceEachIn(contentCopy, "#\\{randomGuid\\}", () => UUID.randomUUID.toString.split("-")(0))
    contentCopy = replaceEachIn(contentCopy, "#\\{randomInt\\}", () => Math.abs(Random.nextInt).toString)
    contentCopy = replaceEachIn(contentCopy, "#\\{randomFloat\\}", () => Math.abs(Random.nextFloat).toString)
    contentCopy = replaceEachIn(contentCopy, "#\\{randomFirstName\\}", () => Names.getRandom)
    contentCopy = replaceEachIn(contentCopy, "#\\{randomLastName\\}", () => LastNames.getRandom)

    // Functions
    val randomIntRangeMatcher = "#\\{random\\([\\s]*([0-9]+)[\\s]*..([0-9]+)[\\s]*\\)\\}".r
    0.until(randomIntRangeMatcher.findAllMatchIn(contentCopy).length).foreach { _ =>
      val matchedValue = randomIntRangeMatcher.findFirstMatchIn(contentCopy).get
      val int1 = matchedValue.group(1).toInt
      val int2 = matchedValue.group(2).toInt
      contentCopy = contentCopy.replace(matchedValue.toString(), (int1 + Random.nextInt((int2 + 1) - int1)).toString)
    }

    contentCopy
  }

  private def replaceEachIn(body: String, replace: String, valueGenerator: () => String): String = {
    var bodyCopy = body
    0.until(replace.r.findAllMatchIn(body).length).foreach { _ =>
      bodyCopy = bodyCopy.replaceFirst(replace, valueGenerator())
    }
    bodyCopy
  }
}

object Request {
  private implicit val formats: DefaultFormats = DefaultFormats
  def apply(json: String): Request = parse(json).extract[Request]
}