package uk.co.ridentbyte.model

import java.util.UUID

import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization.writePretty
import uk.co.ridentbyte.util.Names

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

    val chars = UUID.randomUUID.toString.split("-")(0)
    val int = Random.nextInt.toString
    val float = Random.nextFloat.toString
    val firstName = Names.getRandomName
    val lastName = Names.getRandomName

    val newBody = if (body.isDefined) {
      var bodyCopy: String = body.get

      // Random values for each instance
      bodyCopy = replaceEachIn(bodyCopy, "#\\{randomChars\\}", () => UUID.randomUUID.toString.split("-")(0))
      bodyCopy = replaceEachIn(bodyCopy, "#\\{randomInt\\}", () => Random.nextInt.toString)
      bodyCopy = replaceEachIn(bodyCopy, "#\\{randomFloat\\}", () => Random.nextFloat().toString)
      bodyCopy = replaceEachIn(bodyCopy, "#\\{randomName\\}", () => Names.getRandomName)

      val randomIntRangeMatcher = "#\\{random\\([\\s]*([0-9]+)[\\s]*,[\\s]*([0-9]+)[\\s]*\\)\\}".r
      0.until(randomIntRangeMatcher.findAllMatchIn(bodyCopy).length).foreach { _ =>
        val matchedValue = randomIntRangeMatcher.findFirstMatchIn(bodyCopy).get
        val int1 = matchedValue.group(1).toInt
        val int2 = matchedValue.group(2).toInt
        bodyCopy = bodyCopy.replace(matchedValue.toString(), (int1 + Random.nextInt((int2 + 1) - int1)).toString)
      }

      // Constant (random) values for each instance
      bodyCopy = bodyCopy.replaceAll("#\\{chars\\}", chars)
      bodyCopy = bodyCopy.replaceAll("#\\{int\\}", int)
      bodyCopy = bodyCopy.replaceAll("#\\{float\\}", float)
      bodyCopy = bodyCopy.replaceAll("#\\{firstName\\}", firstName)
      bodyCopy = bodyCopy.replaceAll("#\\{firstNameLower\\}", firstName.toLowerCase)

      bodyCopy = bodyCopy.replaceAll("#\\{lastName\\}", lastName)
      bodyCopy = bodyCopy.replaceAll("#\\{lastNameLower\\}", lastName.toLowerCase)

      Some(bodyCopy)
    } else {
      None
    }

    var newUri = uri

    // Random values for each instance
    newUri = replaceEachIn(newUri, "#\\{randomChars\\}", () => UUID.randomUUID.toString.split("-")(0))
    newUri = replaceEachIn(newUri, "#\\{randomInt\\}", () => Random.nextInt.toString)
    newUri = replaceEachIn(newUri, "#\\{randomFloat\\}", () => Random.nextFloat().toString)
    newUri = replaceEachIn(newUri, "#\\{randomName\\}", () => Names.getRandomName)

    // Constant (random) values for each instance
    newUri = newUri.replaceAll("#\\{chars\\}", chars)
    newUri = newUri.replaceAll("#\\{int\\}", int)
    newUri = newUri.replaceAll("#\\{float\\}", float)
    newUri = newUri.replaceAll("#\\{firstName\\}", firstName)
    newUri = newUri.replaceAll("#\\{lastName\\}", lastName)

    Request(newUri, verb, headers, newBody)

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