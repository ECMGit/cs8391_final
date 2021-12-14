package controllers

import javax.inject._
import play.api.mvc._
import play.api.data._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import play.api.libs.json._
import play.api.libs.ws._
import play.api.http.HttpEntity
import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.util.ByteString
import models.{Paper, PaperIndex, Topic}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(ws: WSClient, cc: ControllerComponents) (implicit ec: ExecutionContext) extends AbstractController(cc) {


  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    Ok(views.html.index("Author: Junhao Shen"))
  }



  def lda_category = Action.async { implicit request =>
    // create cast JSON format
    implicit val topicListJson = Json.format[Topic]
    ws.url("http://localhost:9000/loadLDA").withMethod("GET").stream().map { response =>
      val topics = response.json.as[Seq[Topic]]
      topics.foreach(topic => println(topic.terms))
      Ok(views.html.lda_category(topics))
    }
  }

  def LDACategoryByTopic(tid: Long) = Action.async { implicit request =>
    //todo
    implicit val paperlist = Json.format[PaperIndex]
    ws.url("http://localhost:9000/getPapersByTopic/"+tid.toString).withMethod("GET").stream().map { response =>
      println(response.json)
      val res_paperlist = response.json.as[Seq[PaperIndex]]
      Ok(views.html.topic_paperlist(res_paperlist))
    }
  }

  def locate = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.locate())
  }

  def login = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.login())
  }
  def signUp = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.signup())

  }

}
