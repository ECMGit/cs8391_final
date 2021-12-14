package services
import scala.xml.XML
import scala.math.{max, min}
import models.{Paper, Papers}
import java.nio.file.Paths
/**
 *
 * @project: backend_scala
 * @author: Junhao Shen
 * @date: 10/24/21
 * */
object ExtractAbstract {
  def extract(filename : String = "dblp_abstract_dataset.xml", papersServices: PaperServices): Unit = {
    val xml = XML.loadFile(filename)
    val allInproceedings = xml \\ "dblp" \ "inproceedings"
    val allArticles = xml \\ "dblp" \ "article"
    println("Number of inproceedings:", allInproceedings.length)
    println("NUmber of articles:", allArticles.length)
    papersServices.updateAbstractBatch(allInproceedings)
    papersServices.updateAbstractBatch(allArticles)
//    allInproceedings.foreach(inp => {
//      val title = inp \ "title"
//      val pabstract = inp \ "abstract"
//      papersServices.updateAbstractByTitle(title.text, Option(pabstract.text).filter(_.nonEmpty))
//    })
//    println("inporceedings have been updated")
//    allArticles.foreach(atc => {
//      val title = atc \ "title"
//      val pabstract = atc \ "abstract"
//      papersServices.updateAbstractByTitle(title.text, Option(pabstract.text).filter(_.nonEmpty))
//    })
    println("Articles have been updated")
  }
}
