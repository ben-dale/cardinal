package uk.co.ridentbyte.util

import java.io.{File, PrintWriter}
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

  def writeToFile(dir: String, filename: String, data: String): Unit = {
    val d = new File(dir)
    if (!d.exists || !d.isDirectory) {
      Files.createDirectories(Paths.get(dir))
    }
    val pw = new PrintWriter(dir + "/" + filename)
    pw.write(data)
    pw.close()
  }


}
