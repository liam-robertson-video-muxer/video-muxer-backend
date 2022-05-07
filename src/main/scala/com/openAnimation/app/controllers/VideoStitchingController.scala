package com.openAnimation.app.controllers

import com.openAnimation.app.tools.CustomClasspath.getResourcePath
import java.io.{BufferedWriter, File, FileWriter}
import java.lang.Math.floor
import scala.sys.process._
import scala.util.Try

class VideoStitchingController {

  def stitchSnippetIntoTapestry(snippetId: String, timeStart: Double, timeEnd: Double, tapestryDuration: Double): String = {
    val (timeStartStr, timeEndStr, tapestryDurationStr) = this.convertTimeToHHmmss(timeStart, timeEnd, tapestryDuration)
    val videoList1 = this.trimVideo("tapestryPart1", "00:00:00", timeStartStr, Array())
    val videoList2 = videoList1 :+  s"file '${getResourcePath("working")}/currentSnippet.mp4'"
    val videoList3 = this.trimVideo("tapestryPart2", timeEndStr, tapestryDurationStr, videoList2)
    this.stitchVideos(videoList3)
    "Successfully stitched new video into main animation!"
  }

  def trimVideo(videoName: String, startTime: String, endTime: String, videoList: Array[String]): Array[String] = {
    if (endTime != "0:0:0.0") {
      val stdout = new StringBuilder
      val stderr = new StringBuilder
      val tapestryMp4 = getResourcePath("tapestry/tapestry.mp4")
      val outputPath = getResourcePath("working") + s"/$videoName.mp4"
      println(s"""ffmpeg -y -ss $startTime -to $endTime  -i $tapestryMp4 -c copy -an $outputPath""")
      // -an means copy just video stream, not audio. -y means overwrite existing files
      val exitCode = s"""ffmpeg -y -ss $startTime -to $endTime  -i $tapestryMp4 -c copy -an $outputPath""" ! ProcessLogger(stdout append _, stderr append _ + "\n")
      if (exitCode != 0) {
        println(stderr.toString)
        throw new Exception(s"Failed trim video $videoName")
      } else {
        val finalVideoList = videoList :+ s"file '${getResourcePath("working")}/$videoName.mp4'"
        finalVideoList
      }
    } else {
      videoList
    }
  }

  def stitchVideos(videoList: Array[String]): Unit = {
    val stdout = new StringBuilder
    val stderr = new StringBuilder
    val numOfTapestries =  new File(getResourcePath("tapestry")).listFiles().length
    val textFile = new File(getResourcePath("working") + "/fileList.txt")
    val audioPath = new File(getResourcePath("static") + "/audiotrack.wav")
    val videoOutputPath = new File(getResourcePath("tapestry") + s"/tapestryVideo${numOfTapestries}.mp4")
    val finalOutputPath = new File(getResourcePath("tapestry") + s"/tapestry${numOfTapestries}.mp4")
    val bw = new BufferedWriter(new FileWriter(textFile))
    val videoTextList: String = videoList.mkString("\n")
    bw.write(videoTextList)
    bw.close()
    println(s"""ffmpeg -f concat -safe 0 -i $textFile -i $audioPath -shortest -c copy $videoOutputPath""")
//    val exitCode = s"""ffmpeg -f concat -safe 0 -i $textFile -i $audioPath -shortest -c copy $outputPath""" ! ProcessLogger(stdout append _, stderr append _ + "\n")
    val exitCodeVideo = s"""ffmpeg -f concat -safe 0 -i $textFile -c copy $videoOutputPath""" ! ProcessLogger(stdout append _, stderr append _ + "\n")
    if (exitCodeVideo != 0) {
      println(stderr.toString)
      throw new Exception("Failed to stitch new video into tapestry")
    }
    val exitCodeAudio = s"""ffmpeg -i $videoOutputPath -i $audioPath -shortest -c:v copy -c:a aac ${finalOutputPath}""" ! ProcessLogger(stdout append _, stderr append _ + "\n")
    if (exitCodeAudio != 0) {
      println(stderr.toString)
      throw new Exception("Failed to stitch audio to new video")
    }
    videoOutputPath.delete
  }

  def convertTimeToHHmmss(timeStart: Double, timeEnd: Double, tapestryDuration: Double): (String, String, String) = {
    val timeStartStr = this.convertTimeToHHmmss(timeStart)
    val timeEndStr = this.convertTimeToHHmmss(timeEnd)
    val tapestryDurationStr = this.convertTimeToHHmmss(tapestryDuration)
    (timeStartStr, timeEndStr, tapestryDurationStr)
  }

  def convertTimeToHHmmss(time: Double): String = {
    val timeStrTry = Try {
      val hours: Int = floor(time / 3600).toInt
      val minutes: Int = floor(time / 60).toInt
      val seconds: Double = if (minutes > 0) time - (floor(time / 60) * 60) else time
      s"$hours:$minutes:$seconds"
    }
    timeStrTry.getOrElse(throw new Exception(s"Failed to convert $time to hours:minutes:seconds format"))
  }

}
