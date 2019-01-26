package uk.co.ridentbyte.model

import org.scalatest.FlatSpec
import org.scalatest.Matchers._

import scala.collection.JavaConverters._

class CommandUnitSpec extends FlatSpec {

  val vocab = new Vocabulary(
    new Words(List().asJava, new java.util.Random()),
    new Words(List().asJava, new java.util.Random()),
    new Words(List().asJava, new java.util.Random()),
    new Words(List().asJava, new java.util.Random()),
    new Words(List().asJava, new java.util.Random()),
    new Words(List().asJava, new java.util.Random()),
    new Words(List().asJava, new java.util.Random()),
    new Words(List().asJava, new java.util.Random()),
    new Words(List("Lorem", "ipsum").asJava, new java.util.Random()),
    new Words(List().asJava, new java.util.Random())
  )

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
    val result = testSubject.processWith(Map("hello" -> (() => "123")), vocab)

    // Then
    result shouldBe "123"
  }

  it should "process lower(\"HELLO\") and return value: hello" in {
    // Given
    val testSubject = Command("#{lower(\"HELLO\")}")

    // When
    val result = testSubject.processWith(Map.empty[String, () => String], vocab)

    // Then
    result shouldBe "hello"
  }

  it should "process upper(\"hello\") and return value: HELLO" in {
    // Given
    val testSubject = Command("#{upper(\"hello\")}")

    // When
    val result = testSubject.processWith(Map.empty[String, () => String], vocab)

    // Then
    result shouldBe "HELLO"
  }

  it should "process capitalise(\"hello\") and return value: Hello" in {
    // Given
    val testSubject = Command("#{capitalise(\"hello\")}")

    // When
    val result = testSubject.processWith(Map.empty[String, () => String], vocab)

    // Then
    result shouldBe "Hello"
  }

  it should "process upper(firstName) and return value: JON" in {
    // Given
    val testSubject = Command("#{upper(firstName)}")

    // When
    val result = testSubject.processWith(Map("firstName" -> (() => "Jon")), vocab)

    // Then
    result shouldBe "JON"
  }

  it should "process capitalise(lower(firstName)) and return value Jon" in {
    // Given
    val testSubject = Command("#{capitalise(lower(firstName))}")

    // When
    val result = testSubject.processWith(Map("firstName" -> (() => "JON")), vocab)

    // Then
    result shouldBe "Jon"
  }

  it should "process random(firstName, lastName) and return either value Jon or Bob" in {
    // Given
    val testSubject = Command("#{random(firstName, lastName)}")

    // When
    val result = testSubject.processWith(Map("firstName" -> (() => "Jon"), "lastName" -> (() => "Bob")), vocab)

    // Then
    result should fullyMatch regex "Jon|Bob"
  }

  it should "process random(\"hello\", lastName) and return either value Jon or Bob" in {
    // Given
    val testSubject = Command("#{random(\"hello\", lastName)}")

    // When
    val result = testSubject.processWith(Map("firstName" -> (() => "Jon"), "lastName" -> (() => "Bob")), vocab)

    // Then
    result should fullyMatch regex "hello|Bob"
  }

  it should "process random(upper(\"hello\")) and return either value Jon or Bob" in {
    // Given
    val testSubject = Command("#{random(upper(\"hello\"))}")

    // When
    val result = testSubject.processWith(Map.empty[String, () => String], vocab)

    // Then
    result should fullyMatch regex "HELLO"
  }

  it should "process random(upper(firstName)) and return either value Jon or Bob" in {
    // Given
    val testSubject = Command("#{random(upper(firstName))}")

    // When
    val result = testSubject.processWith(Map("firstName" -> (() => "Jon")), vocab)

    // Then
    result shouldBe "JON"
  }

  it should "process #{random(ben, bob)} and return random(ben, bob)" in {
    // Given
    val testSubject = Command("#{random(ben, bob)}")

    // When
    val result = testSubject.processWith(Map(), vocab)

    // Then
    result shouldBe "random(ben, bob)"
  }

  it should "process #{random(ben, \"bob\")} and return random(ben, bob)" in {
    // Given
    val testSubject = Command("#{random(ben, \"bob\")}")

    // When
    val result = testSubject.processWith(Map(), vocab)

    // Then
    result shouldBe "random(ben, \"bob\")"
  }

  it should "process #{lorem(\"20\")} and return lorem(\"20\")" in {
    // Given
    val testSubject = Command("#{lorem(\"20\")}")

    // When
    val result = testSubject.processWith(Map(), vocab)

    // Then
    result shouldBe "lorem(\"20\")"
  }

  it should "process #{lorem(1)} and return Lorem" in {
    // Given
    val testSubject = Command("#{lorem(1)}")

    // When
    val result = testSubject.processWith(Map(), vocab)

    // Then
    result shouldBe "Lorem"
  }

  it should "process #{random(1)} and return 1" in {
    // Given
    val testSubject = Command("#{random(1)}")

    // When
    val result = testSubject.processWith(Map(), vocab)

    // Then
    result shouldBe "1"
  }

  it should "process #{lorem(random(1))} and return Lorem" in {
    // Given
    val testSubject = Command("#{lorem(random(\"1\"))}")

    // When
    val result = testSubject.processWith(Map(), vocab)

    // Then
    result shouldBe "Lorem"
  }


}
