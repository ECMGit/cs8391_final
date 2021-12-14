package models

/**
 *
 * @project: lab2_frontend_scala
 * @author: Junhao Shen
 * @date: 10/8/21
 * */
case class Paper(pid: Int,
                 p_type: String,
                 title: String,
                 pages: String,
                 year: String,
                 journal: Option[String],
                 volume: Option[String],
                 number: Option[String],
                 crossref: String,
                 bookTitle: String,
                 url: String,
                 topic: Int,
                 pabstract: String)

case class PaperIndex(pid: Int, title: String)
