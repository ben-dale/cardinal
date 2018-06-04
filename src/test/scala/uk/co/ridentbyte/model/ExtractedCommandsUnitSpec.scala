package uk.co.ridentbyte.model

import org.scalatest.FlatSpec
import org.scalatest.Matchers._


class ExtractedCommandsUnitSpec extends FlatSpec {

  "ExtractedCommands" should "return a single command" in {
    // Given
    val testSubject = ExtractedCommands("hello #{world}")

    // When
    val result = testSubject.all

    // Then
    result shouldBe List(Command("#{world}"))
  }

  it should "return empty list" in {
    // Given
    val testSubject = ExtractedCommands("this shouldn't match anything")

    // When
    val result = testSubject.all

    // Then
    result shouldBe empty
  }

  it should "return two commands" in {
    // Given
    val testSubject = ExtractedCommands("#{hello} world #{sailor}")

    // When
    val result = testSubject.all

    // Then
    result shouldBe List(Command("#{hello}"), Command("#{sailor}"))
  }

  it should "handle null content" in {
    // Given
    val testSubject = ExtractedCommands(null)

    // When
    val result = testSubject.all

    // Then
    result shouldBe empty
  }

}
