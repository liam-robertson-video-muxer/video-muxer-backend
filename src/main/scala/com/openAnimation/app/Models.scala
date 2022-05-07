package com.openAnimation.app

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol.arrayFormat
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val parametersFormat: RootJsonFormat[UpdateParameters] = jsonFormat3(UpdateParameters)
  implicit val snippetInFormat: RootJsonFormat[SnippetIn] = jsonFormat7(SnippetIn)
  implicit val snippetFormat: RootJsonFormat[Snippet] = jsonFormat11(Snippet)
  implicit val snippetReponse: RootJsonFormat[SnippetReponse] = jsonFormat1(SnippetReponse)
  implicit val snippetRow: RootJsonFormat[SnippetPeristReq] = jsonFormat5(SnippetPeristReq)
  implicit val snippetInMetadata: RootJsonFormat[SnippetInMetadata] = jsonFormat5(SnippetInMetadata)
  implicit val snippetMetadata: RootJsonFormat[SnippetMetadata] = jsonFormat7(SnippetMetadata)
  implicit val snippetOutMetadata: RootJsonFormat[SnippetPctMetadata] = jsonFormat7(SnippetPctMetadata)
  implicit val snippetOut: RootJsonFormat[SnippetOut] = jsonFormat8(SnippetOut)
}

case class UpdateParameters(timestamp: String, name: String, mp4: String)
case class SnippetIn(user: String, videoStream: Array[Byte], videoType: String, timeStart: Int, timeEnd: Int, upvote: Int, downvote: Int)
case class Snippet(id: Int, videoDiv: String, videoStream: Array[Byte], user: String, timeStartPct: Float, timeEndPct: Float, durationPct: Float, currentTime: Float, upvote: Int, downvote: Int, visible: Boolean)
case class SnippetReponse(result: Int)
case class SnippetPeristReq(videoType: String, user: String, timeStartPct: Double, timeEnd: Double, duration: Double)

case class SnippetInMetadata(id: String, user: String, timeStart: String, timeEnd: String, duration: Double)
case class SnippetMetadata(id: String, user: String, timeStart: Double, timeEnd: Double, duration: Double, upvote: Int, downvote: Int)
case class SnippetOut(id: String, videoStream: Array[Byte], user: String, timeStart: Double, timeEnd: Double, duration: Double, upvote: Int, downvote: Int)
case class SnippetPctMetadata(id: String, user: String, timeStartPct: Double, timeEndPct: Double, durationPct: Double, upvote: Int, downvote: Int)


