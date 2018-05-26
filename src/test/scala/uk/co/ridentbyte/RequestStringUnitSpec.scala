package uk.co.ridentbyte

import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import uk.co.ridentbyte.model.{Names, RequestString}

import scala.util.Random

class RequestStringUnitSpec extends FlatSpec {

  "RequestString" should "process and replace #{guid}" in {
    // Given
    val testSubject = RequestString("#{guid}", Map.empty[String, String], Names(List(), Random), Names(List(), Random))

    // When
    val result = testSubject.process

    // Then
    result should not include "#{guid}"
  }

}