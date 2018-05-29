package uk.co.ridentbyte

import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import uk.co.ridentbyte.model.{Words, RequestString}

import scala.util.Random

class RequestStringUnitSpec extends FlatSpec {

  val firstNames: Words = Words(List("Joe"), Random)
  val lastNames: Words = Words(List("Bloggs"), Random)
  val verbs: Words = Words(List("Connect"), Random)
  val nouns: Words = Words(List("Pocket"), Random)
  val envVars: Map[String, String] = Map.empty[String, String]

  "RequestString" should "process and replace #{guid}" in {
    // Given
    val testSubject = RequestString("#{guid}", envVars, firstNames, lastNames, verbs, nouns)

    // When
    val result = testSubject.process

    // Then
    result should not include "#{guid}"
  }

  it should "process and replace #{int}" in {
    // Given
    val testSubject = RequestString("#{int}", envVars, firstNames, lastNames, verbs, nouns)

    // When
    val result = testSubject.process

    // Then
    result should not include "#{int}"
    result should fullyMatch regex "[0-9]+"
  }

  it should "process and replace #{float}" in {
    // Given
    val testSubject = RequestString("#{float}", envVars, firstNames, lastNames, verbs, nouns)

    // When
    val result = testSubject.process

    // Then
    result should not include "#{float}"
    result should fullyMatch regex "[0-9]+\\.[0-9]+"
  }

  it should "process and replace #{firstName}" in {
    // Given
    val testSubject = RequestString("#{firstName}", envVars, firstNames, lastNames, verbs, nouns)

    // When
    val result = testSubject.process

    // Then
    result shouldBe "Joe"
  }

  it should "process and replace #{firstName}-#{firstName}" in {
    // Given
    val testSubject = RequestString("#{firstName}-#{firstName}", envVars, firstNames, lastNames, verbs, nouns)

    // When
    val result = testSubject.process

    // Then
    result shouldBe "Joe-Joe"
  }

  it should "process and replace #{firstNameLower}" in {
    // Given
    val testSubject = RequestString("#{firstNameLower}", envVars, firstNames, lastNames, verbs, nouns)

    // When
    val result = testSubject.process

    // Then
    result shouldBe "joe"
  }

  it should "process and replace #{firstNameLower}-#{firstNameLower}" in {
    // Given
    val testSubject = RequestString("#{firstNameLower}-#{firstNameLower}", envVars, firstNames, lastNames, verbs, nouns)

    // When
    val result = testSubject.process

    // Then
    result shouldBe "joe-joe"
  }

  it should "process and replace #{lastName}" in {
    // Given
    val testSubject = RequestString("#{lastName}", envVars, firstNames, lastNames, verbs, nouns)

    // When
    val result = testSubject.process

    // Then
    result shouldBe "Bloggs"
  }

  it should "process and replace #{lastName}-#{lastName}" in {
    // Given
    val testSubject = RequestString("#{lastName}-#{lastName}", envVars, firstNames, lastNames, verbs, nouns)

    // When
    val result = testSubject.process

    // Then
    result shouldBe "Bloggs-Bloggs"
  }

  it should "process and replace #{lastNameLower}-#{lastNameLower}" in {
    // Given
    val testSubject = RequestString("#{lastNameLower}-#{lastNameLower}", envVars, firstNames, lastNames, verbs, nouns)

    // When
    val result = testSubject.process

    // Then
    result shouldBe "bloggs-bloggs"
  }

  it should "process and replace #{random(1..1)}" in {
    // Given
    val testSubject = RequestString("#{random(1..1)}", envVars, firstNames, lastNames, verbs, nouns)

    // When
    val result = testSubject.process

    // Then
    result shouldBe "1"
  }

  it should "process and replace #{random(2..1)}" ignore {
    // Given
    val testSubject = RequestString("#{random(2..1)}", envVars, firstNames, lastNames, verbs, nouns)

    // When
    val result = testSubject.process

    // Then
    result shouldBe "1"
  }

  it should "process and replace #{random(hello)}" in {
    // Given
    val testSubject = RequestString("#{random(hello)}", envVars, firstNames, lastNames, verbs, nouns)

    // When
    val result = testSubject.process

    // Then
    result shouldBe "hello"
  }

  it should "process and replace #{random(hello, world)}" in {
    // Given
    val testSubject = RequestString("#{random(hello, world)}", envVars, firstNames, lastNames, verbs, nouns)

    // When
    val result = testSubject.process

    // Then
    result should fullyMatch regex "hello|world"
  }

}