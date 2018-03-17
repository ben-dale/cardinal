package uk.co.ridentbyte.util

import java.io.{File, FileWriter}
import java.nio.file.{FileSystems, Files, Paths}

import scala.io.Source
import scala.collection.JavaConverters._
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
    Files
      .walk(FileSystems.getDefault.getPath(path))
      .iterator()
      .asScala
      .filter(f => Files.isRegularFile(f) && f.getFileName.toFile.getName.endsWith(".json"))
      .map(_.toString.replace(path + "/", "")).toList
  }

  def writeToFile(filename: String, data: String): Unit = {
    val f = new File(filename)
    f.getParentFile.mkdirs()

    val fileWriter = new FileWriter(f)
    fileWriter.write(data)
    fileWriter.close()
  }


}
