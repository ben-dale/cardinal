scalaVersion := "2.12.4"
organization := "uk.co.ridentbyte"
name := "cardinal"
version := "0.1.4"

libraryDependencies += "com.google.code.gson" % "gson" % "2.8.5"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test"
libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.6.0-M2"
libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.5.6"
libraryDependencies += "org.junit.jupiter" % "junit-jupiter-api" % "5.3.1" % Test