package uk.co.ridentbyte.model

import java.nio.charset.StandardCharsets
import java.util.Base64

case class BasicAuth(private val username: String, private val password: String) {
  def asAuthHeader: String = {
    "Authorization: Basic " +
      Base64.getEncoder.encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8))
  }
}
