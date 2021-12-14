package models

import com.google.inject.Inject

import scala.xml.NodeSeq
import scala.xml.Node
//import play.api.data.Form
//import play.api.data.Forms.mapping
//import play.api.data.Forms._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
/**
 *
 * @project: backend_scala
 * @author: Junhao Shen
 * @date: 10/25/21
 * */

case class Paper(pid: Long,
                 p_type: String,
                 title: String,
                 pages: Option[String],
                 year: String,
                 volume: Option[String],
                 journal: Option[String],
                 number: Option[String],
                 crossref: String,
                 bookTitle: String,
                 url: String,
                 topic: Option[Int],
                 pabstract: Option[String])

case class PaperIndex(pid: Long, title: String)


import slick.jdbc.MySQLProfile.api._
class PaperTableDef(tag: Tag) extends Table[Paper](tag, "papers") {

  def pid = column[Long]("pid", O.PrimaryKey,O.AutoInc)
  def ptype = column[String]("type")
  def title = column[String]("title")
  def pages = column[Option[String]]("pages")
  def year = column[String]("year")
  def volume = column[Option[String]]("volume")
  def journal = column[Option[String]]("journal")
  def number = column[Option[String]]("number")
  def crossref = column[String]("crossref")
  def bookTitle = column[String]("bookTitle")
  def url = column[String]("url")
  def topic = column[Option[Int]]("topic")
  def pabstract = column[Option[String]]("abstract")

  override def * =
    (pid, ptype, title, pages, year, volume, journal, number, crossref, bookTitle, url, topic, pabstract) <>(Paper.tupled, Paper.unapply)
}

class Papers @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
                      (implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  // the HasDatabaseConfigProvider trait gives access to the
  // dbConfig object that we need to run the slick queries


  val papers = TableQuery[PaperTableDef]

  def add(paper: Paper): Future[String] = {
    dbConfig.db.run(papers += paper).map(res => "User successfully added").recover {
      case ex: Exception => ex.getCause.getMessage
    }
  }

  def delete(id: Long): Future[Int] = {
    dbConfig.db.run(papers.filter(_.pid === id).delete)
  }

  def get(id: Long): Future[Option[Paper]] = {
    dbConfig.db.run(papers.filter(_.pid === id).result.headOption)
  }

  def listAll: Future[Seq[Paper]] = {
    dbConfig.db.run(papers.result)
  }

  def getPapersByTopic(topic: Int): Future[Seq[(Long, String)]] = {
    dbConfig.db.run(papers.filter(_.topic === topic).map(p => (p.pid, p.title)).result)
  }

  def getByTitle(title: String): Future[Option[Paper]] = {
    val res = dbConfig.db.run(papers.filter(_.title === title).result.headOption)
    println(res.toString)
    return res

  }

  def updateAstractByTitle(title: String, pabstract: Option[String]): Future[Int] = {
    dbConfig.db.run(papers.filter(p => p.title === title && p.pabstract != null).map(p => p.pabstract).update(pabstract))
  }

  def updatePaperCategoryByID(topicID: Option[Int], pid: Long): Future[Int] = {
    dbConfig.db.run(papers.filter(p => p.pid === pid).map(_.topic).update(topicID))
  }

  //unpack node to title and abstract
  def unpack(node: Node): (String, String) = {
    val title = (node \ "title").text
    val pab = (node \ "abstract").text
    return (title, pab)
  }

  //update papers abstract sequentially
  def updateAbstractBatch(node_seq: NodeSeq): Unit = {
    val np = node_seq.map(unpack)
    val a = ( for{
      _ <- DBIO.seq(np.map(enp => papers.filter(_.title === enp._1).map(p => p.pabstract).update(Option(enp._2).filter(_.nonEmpty))): _*)
    } yield()).transactionally
    val b = dbConfig.db.run(a)
    println(b)
  }
}