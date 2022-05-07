package com.openAnimation.app

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.util.ByteString
import com.openAnimation.app.controllers.{InputController, RetrieveDataController, VideoStitchingController}
import com.openAnimation.app.tools.CustomClasspath.getResourcePath

import java.net.URL
import java.util.UUID

class PrimaryController() {

  val primaryDao = new PrimaryDao()
  val retrieveDataController = new RetrieveDataController()
  val inputController = new InputController()
  val videoStitchingController = new VideoStitchingController()
  val serverStatusMessage = inputController.serverStatusMessage

  /**
   * Snippet is the name for individual animation segments
   * Tapestry is the name for the animation segments that are stitched together
   *
   * Program outline:
   * - Takes in snippet from user
   * - Corrects video type and frame rate for snippet
   * - Checks if snippet should be added to tapestry
   * - Splits the current tapestry into two
   * - Stitches the current snippet into the tapestry
   * - Adds audio track on top of tapestry
   * - Sends the tapestry back to the user
   */

    def getTapestry = {
      retrieveDataController.getTapestry
    }

    def getAllSnippetsMetadata = {
      retrieveDataController.getAllSnippetsMetadata
    }

  def getSnippetVideoStream(id: String) = {
    retrieveDataController.getSnippetVideoStream(id)
  }

  def addSnippetToTapestry(user: String, videoName: String, videoStream: ByteString, timeStart: Double, timeEnd: Double, duration: Double) = {
    val snippetId = UUID.randomUUID.toString
    val tapestryDuration = retrieveDataController.getDuration(getResourcePath("tapestry/tapestry.mp4"))
    val filename = s"preEncodedSnippet.${videoName.split("\\.")(1)}"
    inputController.addSnippetToFileSystem(filename, videoStream)
    inputController.reencodeInputVideo(filename)
    videoStitchingController.stitchSnippetIntoTapestry(snippetId, timeStart, timeEnd, tapestryDuration)
    HttpResponse(StatusCodes.OK, entity = "Video stitched into animation successfully!")
  }

  def createInitialTapestry(): String = {
    val tryTapestryFile: Option[URL] = Option(this.getClass.getClassLoader.getResource("tapestry/tapestry.mp4"))
    val createTapestryResponse = tryTapestryFile match {
      case Some(i) => "Tapestry already exists"
      case None => inputController.createPlaceholderVideo(300)
    }
    createTapestryResponse
  }
}
