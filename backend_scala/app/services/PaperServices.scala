package services
import com.google.inject.Inject
import models.{Paper, Papers}
import scala.xml.NodeSeq
import scala.concurrent.Future

/**
 *
 * @project: backend_scala
 * @author: Junhao Shen
 * @date: 10/27/21
 * */
class PaperServices @Inject() (papers: Papers) {

  def addPaper(paper: Paper): Future[String] = {
    papers.add(paper)
  }

  def deletePaper(id: Long): Future[Int] = {
    papers.delete(id)
  }

  def getPaperByID(id: Long): Future[Option[Paper]] = {
    papers.get(id)
  }

  def listAllPapers: Future[Seq[Paper]] = {
    papers.listAll
  }

  def getByTitle(title: String): Future[Option[Paper]] = {
    papers.getByTitle(title)
  }

  def updateAbstractByTitle(title: String, pabstract: Option[String]): Future[Int] = {
    papers.updateAstractByTitle(title, pabstract)
  }

  def updateAbstractBatch(node_seq: NodeSeq): Unit = {
    papers.updateAbstractBatch(node_seq)
  }

  def updatePaperCategory(topicID: Option[Int], pid: Long): Future[Int] = {
    papers.updatePaperCategoryByID(topicID, pid)
  }

  def getPapersByTopic(topicID: Int): Future[Seq[(Long, String)]] = {
   papers.getPapersByTopic(topicID)
  }

}
