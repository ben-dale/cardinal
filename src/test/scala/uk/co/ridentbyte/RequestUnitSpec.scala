package uk.co.ridentbyte

import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import uk.co.ridentbyte.model.{Config, Request}

class RequestUnitSpec extends FlatSpec {

  "Request" should "serialise to JSON" in {
    // Given
    val request = Request(
      "https://google.com",
      "POST",
      List("Content-Type: application/json"),
      Some("""{"hello": "world"}""")
    )

    // When
    val result = request.toJson

    // Then
    result shouldBe
      """{
        |  "uri" : "https://google.com",
        |  "verb" : "POST",
        |  "headers" : [ "Content-Type: application/json" ],
        |  "body" : "{\"hello\": \"world\"}"
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
    val result = Request(json)

    // Then
    result.uri shouldBe "https://google.com"
    result.verb shouldBe "POST"
    result.headers shouldBe List("Content-Type: application/json")
    result.body shouldBe Some("""{"hello": "world"}""")
  }

  it should "return Response with #{id} constants replaced" in {
    // Given
    val request = Request(
      "https://google.com/#{id}",
      "POST",
      List("X-Thing: #{id}"),
      Some("""{"hello": "#{id}"}""")
    )

    // When
    val result = request.withId("4")

    // Then
    result.uri shouldBe "https://google.com/4"
    result.verb shouldBe "POST"
    result.headers shouldBe List("X-Thing: 4")
    result.body shouldBe Some("""{"hello": "4"}""")
  }

  it should "return Response with #{guid} placeholders replaced" in {
    // Given
    val request = Request(
      "https://google.com/#{guid}/search",
      "POST",
      List("X-Thing: #{guid}", "X-Another: hello/world/#{guid}"),
      Some("#{guid}")
    )

    // When
    val result = request.processConstants(Config(List.empty[String]))

    // Then
    verifyPlaceholdersReplaced("#{guid}", request, result)
  }

  private def verifyPlaceholdersReplaced(placeholder: String, request: Request, result: Request): Unit = {
    val value = result.body.get
    value shouldNot be (placeholder)
    result.headers.filter(_.contains(placeholder)) shouldBe empty
    result.uri shouldNot contain (placeholder)
    result.verb shouldBe request.verb
  }

}