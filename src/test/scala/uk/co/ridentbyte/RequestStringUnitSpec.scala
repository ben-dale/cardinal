package uk.co.ridentbyte

import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import uk.co.ridentbyte.model.{Names, RequestString}

import scala.util.Random

class RequestStringUnitSpec extends FlatSpec {

  val firstNames: Names = Names(List("Joe"), Random)
  val lastNames: Names = Names(List("Bloggs"), Random)
  val emptyEnvVars: Map[String, String] = Map.empty[String, String]

  "RequestString" should "process and replace #{guid}" in {
    // Given
    val testSubject = RequestString("#{guid}", emptyEnvVars, firstNames, lastNames)

    // When
    val result = testSubject.process

    // Then
    result should not include "#{guid}"
  }

  it should "process and replace #{int}" in {
    // Given
    val testSubject = RequestString("#{int}", emptyEnvVars, firstNames, lastNames)

    // When
    val result = testSubject.process

    // Then
    result should not include "#{int}"
    result should fullyMatch regex "[0-9]+"
  }

  it should "process and replace #{float}" in {
    // Given
    val testSubject = RequestString("#{float}", emptyEnvVars, firstNames, lastNames)

    // When
    val result = testSubject.process

    // Then
    result should not include "#{float}"
    result should fullyMatch regex "[0-9]+\\.[0-9]+"
  }

  it should "process and replace #{firstName}" in {
    // Given
    val testSubject = RequestString("#{firstName}", emptyEnvVars, firstNames, lastNames)

    // When
    val result = testSubject.process

    // Then
    result shouldBe "Joe"
  }

  it should "process and replace #{firstName}-#{firstName}" in {
    // Given
    val testSubject = RequestString("#{firstName}-#{firstName}", emptyEnvVars, firstNames, lastNames)

    // When
    val result = testSubject.process

    // Then
    result shouldBe "Joe-Joe"
  }

  it should "process and replace #{firstNameLower}" in {
    // Given
    val testSubject = RequestString("#{firstNameLower}", emptyEnvVars, firstNames, lastNames)

    // When
    val result = testSubject.process

    // Then
    result shouldBe "joe"
  }

  it should "process and replace #{firstNameLower}-#{firstNameLower}" in {
    // Given
    val testSubject = RequestString("#{firstNameLower}-#{firstNameLower}", emptyEnvVars, firstNames, lastNames)

    // When
    val result = testSubject.process

    // Then
    result shouldBe "joe-joe"
  }

  it should "process and replace #{lastName}" in {
    // Given
    val testSubject = RequestString("#{lastName}", emptyEnvVars, firstNames, lastNames)

    // When
    val result = testSubject.process

    // Then
    result shouldBe "Bloggs"
  }

  it should "process and replace #{lastName}-#{lastName}" in {
    // Given
    val testSubject = RequestString("#{lastName}-#{lastName}", emptyEnvVars, firstNames, lastNames)

    // When
    val result = testSubject.process

    // Then
    result shouldBe "Bloggs-Bloggs"
  }

  it should "process and replace #{lastNameLower}-#{lastNameLower}" in {
    // Given
    val testSubject = RequestString("#{lastNameLower}-#{lastNameLower}", emptyEnvVars, firstNames, lastNames)

    // When
    val result = testSubject.process

    // Then
    result shouldBe "bloggs-bloggs"
  }

  it should "process and replace #{random(1..1)}" in {
    // Given
    val testSubject = RequestString("#{random(1..1)}", emptyEnvVars, firstNames, lastNames)

    // When
    val result = testSubject.process

    // Then
    result shouldBe "1"
  }

}