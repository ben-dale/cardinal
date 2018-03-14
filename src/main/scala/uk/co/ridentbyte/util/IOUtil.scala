package uk.co.ridentbyte.util

import java.io.{File, FileWriter, PrintWriter}
import java.nio.file.{Files, Paths}

import scala.io.Source

object IOUtil {

  def readFileContents(file: File): String = {
    val bufferedSource = Source.fromFile(file)
    val rawRequest = bufferedSource.getLines.mkString
    bufferedSource.close()
    rawRequest
  }

  def loadFile(path: String): File = new File(path)

  def deleteFile(path: String): Unit = {
    Files.deleteIfExists(Paths.get(path))
  }

  def listFileNames(path: String): List[String] = {
    val d = new File(path)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).map(_.getName).toList
    } else {
      List.empty[String]
    }
  }

  def writeToFile(filename: String, data: String): Unit = {
    val f = new File(filename)
    f.getParentFile.mkdirs()

    val fileWriter = new FileWriter(f)
    fileWriter.write(data)
    fileWriter.close()
  }


}
