package uk.co.ridentbyte.model

import java.util.UUID
import java.util.regex.Pattern

import scala.util.Random

case class RequestString(content: String, environmentVars: Map[String, String], vocabulary: Vocabulary) {

  def process: String = {
    var contentCopy = content
    val extractedCommands = ExtractedCommands(content).all

    val guid = UUID.randomUUID.toString.split("-")(0)
    val int = Math.abs(Random.nextInt).toString
    val float = Math.abs(Random.nextFloat).toString
    val firstName = vocabulary.firstNames.random()
    val lastName = vocabulary.lastNames.random()
    val action = vocabulary.actions.random()
    val businessEntity = vocabulary.businessEntities.random()
    val communication = vocabulary.communications.random()
    val country = vocabulary.countries.random()
    val obj = vocabulary.objects.random()
    val place = vocabulary.places.random()
    val emoji = vocabulary.emoji.random()

    val variables = Map[String, () => String](
      "guid" -> (() => guid),
      "int" -> (() => int),
      "float" -> (() => float),
      "firstName" -> (() => firstName),
      "lastName" -> (() => lastName),
      "action" -> (() => action),
      "businessEntity" -> (() => businessEntity),
      "communication" -> (() => communication),
      "country" -> (() => country),
      "object" -> (() => obj),
      "place" -> (() => place),
      "emoji" -> (() => emoji),
      "randomGuid()" -> (() => UUID.randomUUID.toString.split("-")(0)),
      "randomInt()" -> Math.abs(Random.nextInt).toString,
      "randomFloat()" -> Math.abs(Random.nextFloat).toString,
      "randomFirstName()" -> vocabulary.firstNames.random,
      "randomLastName()" -> vocabulary.lastNames.random,
      "randomAction()" -> vocabulary.places.random,
      "randomBusinessEntity()" -> vocabulary.businessEntities.random,
      "randomCommunication()" -> vocabulary.communications.random,
      "randomCountry()" -> vocabulary.countries.random,
      "randomObject()" -> vocabulary.objects.random,
      "randomPlace()" -> vocabulary.places.random,
      "randomEmoji()" -> vocabulary.emoji.random
    ) ++ environmentVars.map { case (k, v) => (k, () => v) }

    extractedCommands.foreach { command =>
      contentCopy = contentCopy.replaceFirst(
        Pattern.quote(command.rawCommand),
        command.processWith(variables, vocabulary)
      )
    }

    contentCopy
  }

}
