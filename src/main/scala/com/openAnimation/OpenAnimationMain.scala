package com.openAnimation

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives.{complete, handleExceptions}
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import com.openAnimation.app.{PrimaryController, PrimaryService}
import com.openAnimation.app.tools.CORSHandler

object OpenAnimationMain extends CORSHandler {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val executionContext = system.dispatcher

    val primaryService = new PrimaryService();
    val primaryController = new PrimaryController();

    def myExceptionHandler: ExceptionHandler =
      ExceptionHandler {
        case cause: Exception =>
          complete(HttpResponse(500, entity = cause.getMessage)
          )
      }

    val route: Route =
      corsHandler(
        handleExceptions(myExceptionHandler) {
          primaryService.routes
        }
      )

    val response = try {
      primaryController.createInitialTapestry()
    } catch {
      case exception: Exception => println(s"\n${exception}\n")
    }
    println(response)
    val host = "0.0.0.0"
    val port = sys.env.getOrElse("PORT", "8080").toInt

    val bindingFuture = Http().newServerAt(host, port).bind(route)
    println(s"Server online at http://localhost:${port}/")

  }

}
