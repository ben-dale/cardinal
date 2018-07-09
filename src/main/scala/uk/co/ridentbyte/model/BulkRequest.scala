package uk.co.ridentbyte.model

case class BulkRequest(throttle: Option[Long] = None, count: Option[Int] = None, ids: Option[List[String]] = None, asBash: Boolean) {

  def getIdsAsString: String = if (ids.isDefined) ids.get.mkString(", ") else ""

  def getThrottleAsString: String = if (throttle.isDefined) throttle.get.toString else ""

  def getCountAsString: String = if (count.isDefined) count.get.toString else ""

}
