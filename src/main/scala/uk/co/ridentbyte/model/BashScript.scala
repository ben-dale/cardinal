package uk.co.ridentbyte.model

import java.util.Date

case class BashScript(requests: List[CardinalRequest], throttle: Option[Long], config: Config) {

  override def toString: String = {
    val header = "#!/bin/bash"
    val date = "# Auto-generated " + new Date().toString
    val delay = if (throttle.isDefined) "sleep " + throttle.get / 1000.0 + "\n" else ""
    val content = requests.map(_.toCurl(config)).mkString("\necho\n" + delay) + "\necho"
    header + "\n\n" + date + "\n" + content
  }

}
