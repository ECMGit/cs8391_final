package controllers

/**
 *
 * @project: lab2_frontend_scala
 * @author: Junhao Shen
 * @date: 10/7/21
 * */
object LoginForm {
  import play.api.data.Forms._
  import play.api.data.Form

  case class Data(username: String, password: String)


  val form = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )
}
