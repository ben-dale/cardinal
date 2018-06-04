package uk.co.ridentbyte.model

import org.scalatest.FlatSpec
import org.scalatest.Matchers._

class CommandUnitSpec extends FlatSpec {

  "Command" should "return contents of command" in {
    // Given
    val testSubject = Command("#{hello}")

    // When
    val result = testSubject.contents

    // Then
    result shouldBe "hello"
  }

  it should "return command" in {
    // Given
    val testSubject = Command("#[notAValidCommand]")

    // When
    val result = testSubject.contents

    // Then
    result shouldBe "#[notAValidCommand]"
  }

  it should "return null" in {
    // Given
    val testSubject = Command(null)

    // When
    val result = testSubject.contents

    // Then
    result shouldBe null
  }

  it should "replace hello with value 123" in {
    // Given
    val testSubject = Command("#{hello}")

    // When
    val result = testSubject.processWith(Map("hello" -> (() => "123")))

    // Then
    result shouldBe "123"
  }

  it should "process lower(\"HELLO\") and return value: hello" in {
    // Given
    val testSubject = Command("#{lower(\"HELLO\")}")

    // When
    val result = testSubject.processWith(Map.empty[String, () => String])

    // Then
    result shouldBe "hello"
  }

  it should "process upper(\"hello\") and return value: HELLO" in {
    // Given
    val testSubject = Command("#{upper(\"hello\")}")

    // When
    val result = testSubject.processWith(Map.empty[String, () => String])

    // Then
    result shouldBe "HELLO"
  }

  it should "process capitalise(\"hello\") and return value: Hello" in {
    // Given
    val testSubject = Command("#{capitalise(\"hello\")}")

    // When
    val result = testSubject.processWith(Map.empty[String, () => String])

    // Then
    result shouldBe "Hello"
  }

  it should "process upper(firstName) and return value: JON" in {
    // Given
    val testSubject = Command("#{upper(firstName)}")

    // When
    val result = testSubject.processWith(Map("firstName" -> (() => "Jon")))

    // Then
    result shouldBe "JON"
  }

  it should "process capitalise(lower(firstName)) and return value Jon" in {
    // Given
    val testSubject = Command("#{capitalise(lower(firstName))}")

    // When
    val result = testSubject.processWith(Map("firstName" -> (() => "JON")))

    // Then
    result shouldBe "Jon"
  }

  it should "process random(firstName, lastName) and return either value Jon or Bob" in {
    // Given
    val testSubject = Command("#{random(firstName, lastName)}")

    // When
    val result = testSubject.processWith(Map("firstName" -> (() => "Jon"), "lastName" -> (() => "Bob")))

    // Then
    result should fullyMatch regex "Jon|Bob"
  }

  it should "process random(\"hello\", lastName) and return either value Jon or Bob" in {
    // Given
    val testSubject = Command("#{random(\"hello\", lastName)}")

    // When
    val result = testSubject.processWith(Map("firstName" -> (() => "Jon"), "lastName" -> (() => "Bob")))

    // Then
    result should fullyMatch regex "hello|Bob"
  }

  it should "process random(upper(\"hello\")) and return either value Jon or Bob" in {
    // Given
    val testSubject = Command("#{random(upper(\"hello\"))}")

    // When
    val result = testSubject.processWith(Map.empty[String, () => String])

    // Then
    result should fullyMatch regex "HELLO"
  }

  it should "process random(upper(firstName)) and return either value Jon or Bob" in {
    // Given
    val testSubject = Command("#{random(upper(firstName))}")

    // When
    val result = testSubject.processWith(Map("firstName" -> (() => "Jon")))

    // Then
    result should fullyMatch regex "JON"
  }

}
