package com.openAnimation.app.controllers

import akka.util.ByteString
import com.openAnimation.app.tools.CustomClasspath.getResourcePath
import org.apache.commons.io.FileUtils
import scala.sys.process._
import java.nio.file.Paths

class InputController {

  val serverStatusMessage: String =
    "<div style='display:flex;padding-top:3em;justify-content:center;font-size:2em;'>" +
      "<h2 style=font-family:'Arial'>Server is up</h2>" +
    "</div>"
//  s"""ffmpeg -ss $startTime -to $endTime  -i $tapestryMp4 -c copy -an $outputPath""" ! ProcessLogger(stdout append _, stderr append _ + "\n")
//  s"""ffmpeg -f concat -safe 0 -i $textFile -c copy -an $outputPath""" ! ProcessLogger(stdout append _, stderr append _ + "\n")

  def reencodeInputVideo(filename: String): String = {
    val inputPath = getResourcePath("working") + s"/$filename"
    val outputPath = getResourcePath("working") + "/currentSnippet.mp4"
    val response = s"""ffmpeg -r 25 -i $inputPath -c:v libx264 -an $outputPath""" ! ProcessLogger(stdout append _, stderr append _ + "\n")
    ""
  }

  def addSnippetToFileSystem(filename: String, snippetVideoStream: ByteString): String = {
    FileUtils.writeByteArrayToFile(Paths.get(getResourcePath("working"), filename).toFile, snippetVideoStream.toArray)
    "Successfully wrote snippet video to working directory"
  }

  def createPlaceholderVideo(duration: Int): String = {
    val imagePath = getResourcePath("static/add-animation-sign.png")
    val outFile = getResourcePath("tapestry") + "/tapestry.mp4"
    val audioPath = getResourcePath("static/audiotrack.wav")
    // frame rate 1/5 means each image lasts 5 seconds but -r 25 overrides this so that the frame rate is 25 fps
    // -shortest clips the audio to be the same length as the video
    // -c:v selects the codecs for the video to be libx264 which is a common encoder i.e. -(codecs):(video)
    val response: String = s"ffmpeg -framerate 1/5 -r 25 -loop 1 -i ${imagePath} -i ${audioPath} -c:v libx264 -t ${duration} -pix_fmt yuv420p -shortest ${outFile}" !!

    response
  }

}
