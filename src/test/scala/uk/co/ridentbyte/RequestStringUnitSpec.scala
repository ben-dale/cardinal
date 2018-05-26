package uk.co.ridentbyte

import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import uk.co.ridentbyte.model.{Names, RequestString}

class RequestStringUnitSpec extends FlatSpec {

  "RequestString" should "process and replace #{guid}" in {
    // Given
    val testSubject = RequestString("X-Another: hello/world/#{guid}", Map.empty[String, String], Names(List()), Names(List()))

    // When
    val result = testSubject.process

    // Then
    result should not include "#{guid}"
  }

}