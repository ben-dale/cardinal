package uk.co.ridentbyte.model

import java.util.UUID
import java.util.regex.Pattern

import uk.co.ridentbyte.util.{LastNames, Names}

import scala.util.Random

case class RequestString(content: String, environmentVars: Map[String, String]) {

  def process: String = {
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
    val randomIntRangeMatcher = "#\\{random\\([\\s]*([0-9]+)[\\s]*\\.\\.([0-9]+)[\\s]*\\)\\}".r
    0.until(randomIntRangeMatcher.findAllMatchIn(contentCopy).length).foreach { _ =>
      val matchedValue = randomIntRangeMatcher.findFirstMatchIn(contentCopy).get
      val int1 = matchedValue.group(1).toInt
      val int2 = matchedValue.group(2).toInt
      contentCopy = contentCopy.replaceFirst(Pattern.quote(matchedValue.toString()), (int1 + Random.nextInt((int2 + 1) - int1)).toString)
    }

    environmentVars.foreach { case (k, v) => contentCopy = contentCopy.replaceAll("#\\{" + k + "\\}", v) }

    contentCopy
  }

  private def replaceEachIn(value: String, replace: String, valueGenerator: () => String): String = {
    var bodyCopy = value
    0.until(replace.r.findAllMatchIn(value).length).foreach { _ =>
      bodyCopy = bodyCopy.replaceFirst(replace, valueGenerator())
    }
    bodyCopy
  }

}
