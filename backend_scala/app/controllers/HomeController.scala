package controllers
import scala.concurrent._
import ExecutionContext.Implicits.global
import javax.inject._
import models.{PaperIndex, Topic, Paper}
import play.api.mvc._
import services.{PaperServices, LDAModel, ReadJSON, FindWorkflowbyGA}
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}

import scala.collection.mutable

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents, paperServices: PaperServices) extends AbstractController(cc) {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    Ok(views.html.index("Your backend application is ready."))
  }

  //curl localhost:[portnumber]/trainLDA
  def trainLDA = Action {
    LDAModel.train
    println("trained finished!")
    Ok("trained finshed!")
  }

  def loadLDAModel = Action {
    //format Topic class to Json, otherwise you cannot use Json.toJson method
    implicit val topicListJson = Json.format[Topic]

    val res = LDAModel.load(paperServices)
    val topicList = new mutable.ListBuffer[Topic]()
    // wrap topic distritbuions to array of Topic objects
    res.zipWithIndex.foreach { case (topic, i) =>
      println(s"TOPIC $i")
      val tid = i
      val terms = new mutable.ListBuffer[String]()
      val termsWeight = new mutable.ListBuffer[Double]()
      topic.foreach { case (term, weight) => {
        terms += term
        termsWeight += weight
        println(s"$term\t$weight")
      }}
      val addTopic = new Topic(tid, terms.toSeq, termsWeight.toSeq)
      topicList += addTopic
      println(s"==========")
    }
    println("load successfully")
    if (topicList.isEmpty) NoContent else Ok(Json.toJson(topicList))
  }

  def getPapersByTopic(tid: Int) = Action.async { implicit request: Request[AnyContent] =>
    implicit val paperlist = Json.format[PaperIndex]
    paperServices.getPapersByTopic(tid) map { res =>
      val resjson = res.map(p => PaperIndex tupled p)
      Ok(Json.toJson(resjson.slice(0, 200)))
    }
  }

  //curl localhost"[portnumber]/extractAbstract
  def extractAbstract = Action {
    import services.ExtractAbstract
    ExtractAbstract.extract(papersServices = paperServices)
    Ok("Paper abtract has been extracted!")
  }

  def getByID(id: Long) = Action.async { implicit request: Request[AnyContent] =>
    implicit val paperformat = Json.format[Paper]
    paperServices.getPaperByID(id) map { paper =>
      Ok(Json.toJson(paper))
    }
  }

  def getByTitle(title: String) = Action.async { implicit request: Request[AnyContent] =>
    println(title)
    paperServices.getByTitle(title) map { paper =>
      Ok(s"This is papers: ${paper.toString}")
    }

  }

  def runGeneticAlgorithm = Action {
    var res = FindWorkflowbyGA.run(2)
    res += FindWorkflowbyGA.run(3)
    Ok(res)
  }

}
