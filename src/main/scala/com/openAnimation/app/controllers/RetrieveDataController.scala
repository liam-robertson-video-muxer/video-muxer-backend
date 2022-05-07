package com.openAnimation.app.controllers

import akka.http.scaladsl.model.Multipart.FormData
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, MediaTypes, MessageEntity, Multipart, UniversalEntity}
import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import com.openAnimation.app.{PrimaryDao, Snippet, SnippetMetadata, SnippetOut, SnippetPctMetadata, SnippetReponse}
import com.openAnimation.app.tools.CustomClasspath.getResourcePath
import org.apache.commons.io.IOUtils

import java.io.{File, InputStream}
import java.util
import scala.concurrent.Future
import scala.sys.process._

class RetrieveDataController {
  val primaryDao = new PrimaryDao()

  def getTapestry = {
    val tapestryFilePath = new File(getResourcePath("tapestry")).listFiles().last
    val videoStreamInput: InputStream = this.getClass.getClassLoader.getResourceAsStream(s"tapestry/${tapestryFilePath.getName}")
    val videoByteArr: Array[Byte] = IOUtils.toByteArray(videoStreamInput)
    HttpEntity(ContentTypes.`application/octet-stream`, videoByteArr)
  }

  def getAllSnippetsMetadata = {
    val tapestryDuration = this.getDuration(getResourcePath("tapestry/tapestry.mp4"))
    val snippetMetadataList: List[SnippetMetadata] = primaryDao.getAllSnippetsWithConn
    val snippetOutMetadataList = snippetMetadataList.map(snippetMetadata => {
      SnippetMetadata(
        snippetMetadata.id,
        snippetMetadata.user,
        (snippetMetadata.timeStart / tapestryDuration) * 100,
        (snippetMetadata.timeEnd / tapestryDuration) * 100,
        (snippetMetadata.duration / tapestryDuration) * 100,
        snippetMetadata.upvote,
        snippetMetadata.downvote
      )
    })
    snippetOutMetadataList
  }

  def getSnippetVideoStream(id: String) = {
    val file = new File(getResourcePath("snippets/" + id + ".mp4"))
    HttpEntity(MediaTypes.`application/octet-stream`, file.length(), FileIO.fromPath(file.toPath))
  }

  def getDuration(filePath: String): Float = {
    val stdout = new StringBuilder
    val stderr = new StringBuilder
    val exitCode = s"""ffprobe -i $filePath -show_entries format=duration -of csv="p=0"""" ! ProcessLogger(stdout append _, stderr append _ + "\n")
    if (exitCode == 1) throw new Exception(s"Couldn't get video duration for ${filePath}\n$stderr")
    stdout.toFloat
  }
}
