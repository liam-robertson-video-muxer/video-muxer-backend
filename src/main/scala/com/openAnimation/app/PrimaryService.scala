package com.openAnimation.app

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.ByteString

class PrimaryService extends Directives with JsonSupport {
  val primaryController = new PrimaryController;

  val routes: Route = {
    get {
      pathSingleSlash {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, primaryController.serverStatusMessage))
      } ~
        path("getTapestry") {
          complete(primaryController.getTapestry)
      } ~
        path("getAllSnippetsMetadata") {
          complete(primaryController.getAllSnippetsMetadata)
      } ~
        path("getSnippetVideoStream") {
        parameters("id") { (id: String) =>
          complete(primaryController.getSnippetVideoStream(id))
        }
      }
      } ~ path("addSnippetToTapestry") {
        post {
          formFields("user", "videoName", "videoStream".as[ByteString], "timeStart".as[Double], "timeEnd".as[Double], "duration".as[Double]) { (user, videoType, videoStream, timeStart, timeEnd, duration) =>
            complete(primaryController.addSnippetToTapestry(user, videoType, videoStream, timeStart, timeEnd, duration))
          }
        }
      }
    }
}
