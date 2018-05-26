package uk.co.ridentbyte.util

import java.io.{File, FileWriter}

object IOUtil {

  def writeToFile(file: File, data: String): Unit = {
    val fileWriter = new FileWriter(file)
    fileWriter.write(data)
    fileWriter.close()
  }

  def readFile(fileName: String): List[String] = {
    scala.io.Source.fromFile(fileName).getLines.toList
  }

}
