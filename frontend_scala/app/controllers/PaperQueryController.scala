package controllers
import javax.inject.Inject
import play.api.data._
import play.api.i18n._
import play.api.libs.ws._
import play.api.mvc._
import models.Paper
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext
/**
 *
 * @project: lab2_frontend_scala
 * @author: Junhao Shen
 * @date: 10/8/21
 * */
class PaperQueryController @Inject()(ws: WSClient, cc: MessagesControllerComponents) (implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {
  import TitleForm._

  val BASE_URL = "http://localhost:9000"
  private val postUrl = routes.PaperQueryController.getMetaByTitle

  var response_message = ""

  def PaperQuery = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.query_lab2(response_message, form, postUrl))
  }

  def getMetaByID(pid: Int) = Action.async { implicit request =>
    implicit val paperjson = Json.format[Paper]
    ws.url(BASE_URL+"/getPaperByID/"+pid).withMethod("GET").stream().map { response =>
      println(response.json)
      val paper = response.json.as[Paper]
      Ok(views.html.paper_meta(paper))
//      Ok(response.json)
    }
  }

  def getMetaByTitle = Action { implicit request: MessagesRequest[AnyContent] =>
    val errorFunction = { formWithErrors: Form[Data] =>
      // This is the bad case, where the form had validation errors.
      // Let's show the user the form again, with the errors highlighted.
      // Note how we pass the form with errors to the template.
      BadRequest(views.html.query_lab2("input errors", formWithErrors, postUrl))
    }
    val successFunction = { data: Data =>
      println(data.title)
      Redirect(routes.PaperQueryController.PaperQuery).flashing("info" -> "Widget added!")
    }
    val formValidationResult = form.bindFromRequest()
    formValidationResult.fold(errorFunction, successFunction)
  }

  //mashup api
  def getFromAminerByTitle(title: String) = Action.async { implicit request =>
    ws.url("https://api.aminer.org/api/search/pub/advanced?term="+title).withMethod("GET").stream().map { response =>
      println(response.json)
//      val paper = response.json.as[Paper]
      Ok(views.html.mashup_aminer(response.json, title))
      //      Ok(response.json)
    }.recover {
      case e: scala.concurrent.TimeoutException =>
        Ok(views.html.mashup_aminer(null, "no response returned from Aminer"))
    }
  }
}
