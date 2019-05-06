package uk.co.ridentbyte.model

import org.scalatest.FlatSpec
import org.scalatest.Matchers._

import scala.collection.JavaConverters._

class CardinalRequestUnitSpec extends FlatSpec {

  "Request" should "serialise to JSON" in {
    // Given
    val request = new CardinalRequest(
      "https://google.com",
      "POST",
      List("Content-Type: application/json").asJava,
      """{"hello": "world"}"""
    )

    // When
    val result = request.toJson

    // Then
    result shouldBe
      """{
        |  "uri": "https://google.com",
        |  "verb": "POST",
        |  "headers": [
        |    "Content-Type: application/json"
        |  ],
        |  "body": "{\"hello\": \"world\"}"
        |}"""
        .stripMargin
  }

  it should "load from JSON" in {
    // Given
    val json = """{
                 |  "uri" : "https://google.com",
                 |  "verb" : "POST",
                 |  "headers" : [ "Content-Type: application/json" ],
                 |  "body" : "{\"hello\": \"world\"}"
                 |}""".stripMargin

    // When
    val result = CardinalRequest.apply(json)

    // Then
    result.getUri shouldBe "https://google.com"
    result.getVerb shouldBe "POST"
    result.getHeaders.asScala shouldBe List("Content-Type: application/json")
    result.getBody shouldBe """{"hello": "world"}"""
  }

  it should "return Response with #{id} constants replaced" in {
    // Given
    val request = new CardinalRequest(
      "https://google.com/#{id}",
      "POST",
      List("X-Thing: #{id}").asJava,
      """{"hello": "#{id}"}"""
    )

    // When
    val result = request.withId("4")

    // Then
    result.getUri shouldBe "https://google.com/4"
    result.getVerb shouldBe "POST"
    result.getHeaders.asScala shouldBe List("X-Thing: 4")
    result.getBody shouldBe """{"hello": "4"}"""
  }

}
