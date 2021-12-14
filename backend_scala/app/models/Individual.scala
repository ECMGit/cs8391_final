package models
import scala.util.Random
import services.FindWorkflowbyGA.{GENE_POOL, MAX_COST, MAX_TIME, MIN_COST, MIN_TIME}
/**
 *
 * @project: backend_scala
 * @author: Junhao Shen
 * @date: 11/16/21
 * */
class Individual(genelen: Int) {
  var fitness = 0.0

  val geneLength = genelen
  val genes = {
    val rn = Random
    val sc1_idx = rn.nextInt(GENE_POOL(0).length)
    val sc2_idx = rn.nextInt(GENE_POOL(1).length)
    val sc3_idx = rn.nextInt(GENE_POOL(2).length)
    if(genelen == 2) Array(GENE_POOL(0)(sc1_idx), GENE_POOL(2)(sc3_idx))
    else Array(GENE_POOL(0)(sc1_idx), GENE_POOL(1)(sc2_idx), GENE_POOL(2)(sc3_idx))
  }

  //  Calculate fitness
  def calcFitness(): Unit = {
    var cost_ef = 0.0
    var time_ef = 0.0
    var rel_ef = 0.0
    var avb_ef = 0.0
    for (i <- 0 until geneLength){
      cost_ef += 1.0 - (genes(i).cost - MIN_COST)/(MAX_COST - MIN_COST)
      time_ef += 1.0 - (genes(i).time - MIN_TIME)/(MAX_TIME - MIN_TIME)
      rel_ef *= genes(i).reliability
      avb_ef *= genes(i).avalability
    }
    fitness = 0.35 * cost_ef / geneLength + rel_ef * 0.1 +  0.2 * time_ef / geneLength + avb_ef * 0.35
  }

  override def toString: String = {
    var res = ""
    genes.foreach(service => res += service)
    res
  }
}
