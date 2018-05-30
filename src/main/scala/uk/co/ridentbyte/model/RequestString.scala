package uk.co.ridentbyte.model

import java.util.UUID
import java.util.regex.Pattern

import scala.util.Random

case class RequestString(content: String,
                         environmentVars: Map[String, String],
                         vocabulary: Vocabulary) {

  def process: String = {
    var contentCopy = content

    val guid = UUID.randomUUID.toString.split("-")(0)
    val int = Math.abs(Random.nextInt).toString
    val float = Math.abs(Random.nextFloat).toString
    val firstName = vocabulary.firstNames.random()
    val lastName = vocabulary.lastNames.random()

    // Constants
    contentCopy = Replace("#\\{guid\\}").in(contentCopy).withValue(guid)
    contentCopy = Replace("#\\{int\\}").in(contentCopy).withValue(int)
    contentCopy = Replace("#\\{float\\}").in(contentCopy).withValue(float)
    contentCopy = Replace("#\\{firstName\\}").in(contentCopy).withValue(firstName)
    contentCopy = Replace("#\\{firstNameLower\\}").in(contentCopy).withValue(firstName.toLowerCase)
    contentCopy = Replace("#\\{lastName\\}").in(contentCopy).withValue(lastName)
    contentCopy = Replace("#\\{lastNameLower\\}").in(contentCopy).withValue(lastName.toLowerCase)

    // Unique values
    contentCopy = Replace("#\\{uniqueGuid\\}").in(contentCopy).withValueFrom(() => UUID.randomUUID.toString.split("-")(0))
    contentCopy = Replace("#\\{uniqueInt\\}").in(contentCopy).withValueFrom(() => Math.abs(Random.nextInt).toString)
    contentCopy = Replace("#\\{uniqueFloat\\}").in(contentCopy).withValueFrom(() => Math.abs(Random.nextFloat).toString)
    contentCopy = Replace("#\\{uniqueFirstName\\}").in(contentCopy).withValueFrom(vocabulary.firstNames.random)
    contentCopy = Replace("#\\{uniqueFirstNameLower\\}").in(contentCopy).withValueFrom(vocabulary.firstNames.randomLower)
    contentCopy = Replace("#\\{uniqueLastName\\}").in(contentCopy).withValueFrom(vocabulary.lastNames.random)
    contentCopy = Replace("#\\{uniqueLastNameLower\\}").in(contentCopy).withValueFrom(vocabulary.lastNames.randomLower)

    // Randoms
    contentCopy = Replace("#\\{action\\}").in(contentCopy).withValueFrom(vocabulary.actions.random)
    contentCopy = Replace("#\\{businessEntity\\}").in(contentCopy).withValueFrom(vocabulary.businessEntities.random)
    contentCopy = Replace("#\\{communication\\}").in(contentCopy).withValueFrom(vocabulary.communications.random)
    contentCopy = Replace("#\\{country\\}").in(contentCopy).withValueFrom(vocabulary.countries.random)
    contentCopy = Replace("#\\{object\\}").in(contentCopy).withValueFrom(vocabulary.objects.random)
    contentCopy = Replace("#\\{place\\}").in(contentCopy).withValueFrom(vocabulary.places.random)

    // Functions
    val randomIntRangeMatcher = "#\\{random\\([\\s]*([0-9]+)[\\s]*\\.\\.([0-9]+)[\\s]*\\)\\}".r
    0.until(randomIntRangeMatcher.findAllMatchIn(contentCopy).length).foreach { _ =>
      val matchedValue = randomIntRangeMatcher.findFirstMatchIn(contentCopy).get
      val int1 = matchedValue.group(1).toInt
      val int2 = matchedValue.group(2).toInt
      contentCopy = contentCopy.replaceFirst(Pattern.quote(matchedValue.toString()), (int1 + Random.nextInt((int2 + 1) - int1)).toString)
    }

    val randomListMatcher = "#\\{random\\(((?:\\w+(?:(?:\\s+)?,(?:\\s+)?)?)+)\\)\\}".r
    0.until(randomListMatcher.findAllMatchIn(contentCopy).length).foreach { _ =>
      val matchedValue = randomListMatcher.findFirstMatchIn(contentCopy).get
      val items = matchedValue.group(1).split(",").map(_.trim)
      contentCopy = contentCopy.replaceFirst(Pattern.quote(matchedValue.toString()), items(Random.nextInt(items.length)))
    }

    environmentVars.foreach { case (k, v) =>
      contentCopy = Replace("#\\{" + k + "\\}").in(contentCopy).withValue(v)
    }

    contentCopy
  }

  case class Replace(private val token: String) {
    def in(content: String): ContentWithToken = {
      ContentWithToken(content, token)
    }

    case class ContentWithToken(private val content: String, private val token: String) {
      def withValueFrom(f: () => String): String = {
        var bodyCopy = content
        0.until(token.r.findAllMatchIn(content).length).foreach { _ =>
          bodyCopy = bodyCopy.replaceFirst(token, f())
        }
        bodyCopy
      }

      def withValue(value: String): String = {
        content.replaceAll(token, value)
      }
    }
  }

}
