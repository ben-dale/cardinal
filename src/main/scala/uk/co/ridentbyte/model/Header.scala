package uk.co.ridentbyte.model

case class Header(key: String, value: String) {
  override def toString: String = s"""$key: $value"""
}
