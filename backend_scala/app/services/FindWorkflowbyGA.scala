package services
import scala.util.Random
import scala.math.min
import scala.math.max
import FindWorkflowbyGA.GENE_POOL
import models.{Individual, Population}

/**
 *
 * @project: backend_scala
 * @author: Junhao Shen
 * @date: 11/16/21
 * */
object FindWorkflowbyGA {

  val GENE_POOL = ReadJSON.read()
  println(GENE_POOL.toString())


  //for normalizing cost & time in one gene, we use min_max normalize,
  var min_cost, min_time = Int.MaxValue
  var max_cost, max_time = Int.MinValue
  for(sclist <- GENE_POOL){
    for(service <- sclist){
      min_cost = min(min_cost, service.cost)
      min_time = min(min_time, service.time)
      max_time = max(max_time, service.time)
      max_cost = max(max_time, service.cost)
    }
  }
  println("max cost:", max_cost, "min cost:", min_cost, "max_time:", max_time, "min_time:", min_time)

  val MAX_COST = max_cost
  val MAX_TIME = max_time
  val MIN_COST = min_cost
  val MIN_TIME = min_time

  def run(geneLen: Int): String= {
    var res = "When genes length is: " + geneLen
    println(res)
    val rn = new Random()
    val demo = new SimpleServiceGA
    //Initialize population
    demo.population.initializePopulation(10, geneLen)
    //Calculate fitness of each individual
    demo.population.calculateFitness()
    //    System.out.println("Generation: " + demo.generationCount + " Fittest: " + demo.population.fittest)
    var best_individual: Individual = demo.population.getFittest
    var best_generation = demo.generationCount
    //While population gets an individual with maximum fitness
    var max_fitness = demo.population.fittest
    while (demo.population.fittest >= max_fitness || demo.generationCount < 20) {
      demo.generationCount += 1
      //Do selection
      demo.selection()
      //Do crossover
      demo.crossover()
      //Do mutation under a random probability
      if (rn.nextInt % 7 < 5) demo.mutation()
      //Add fittest offspring to population
      demo.addFittestOffspring()
      //Calculate new fitness value
      demo.population.calculateFitness()
      //      System.out.println("Generation: " + demo.generationCount + " Fittest: " + demo.population.fittest + "workflow: " + demo.population.getFittest.toString)
      max_fitness = max(max_fitness, demo.population.fittest)
      if(max_fitness == demo.population.fittest){
        best_individual = demo.population.getFittest
        best_generation = demo.generationCount
      }
    }
    println("\nSolution found in generation " + best_generation)
    println("Fitness: " + max_fitness)
    res += "\nSolution found in generation " + best_generation + "\nFitness: " + max_fitness + "\nBest workflow: "
    for (i <- 0 until best_individual.geneLength) {
      if(i > 0) res += "->"
      if(geneLen == 2 && i == 1) res += "SC" + (i+2) +best_individual.genes(i).id
      else res += "SC" + (i+1) +best_individual.genes(i).id
    }
    res += "\n"
    print(res)
    res
  }
}

class SimpleServiceGA {
  val population = new Population
  var fittest: Individual = null
  var secondFittest: Individual = null
  var generationCount = 0

  //Selection
  def selection(): Unit = { //Select the most fittest individual
    fittest = population.getFittest
    //Select the second most fittest individual
    secondFittest = population.getSecondFittest
  }

  //Crossover
  def crossover(): Unit = {
    val rn = new Random
    //Select a random crossover point
    val crossOverPoint = rn.nextInt(population.individuals(0).geneLength)
    //Swap values among parents
    for (i <- 0 until crossOverPoint) {
      val temp = fittest.genes(i)
      fittest.genes(i) = secondFittest.genes(i)
      secondFittest.genes(i) = temp
    }
  }

  //Mutation
  def mutation(): Unit = {
    val rn = new Random
    //Select a random mutation point
    var mutationPoint = rn.nextInt(population.individuals(0).geneLength)
    //Flip values at the mutation point
    var mutationPointIdx = mutationPoint
    if(fittest.geneLength == 2 && mutationPoint == 1) mutationPointIdx += 1
    fittest.genes(mutationPoint) = GENE_POOL(mutationPointIdx)(rn.nextInt(GENE_POOL(mutationPointIdx).length))
    mutationPoint = rn.nextInt(population.individuals(0).geneLength)
    mutationPointIdx = mutationPoint
    if(fittest.geneLength == 2 && mutationPoint == 1) mutationPointIdx += 1
    secondFittest.genes(mutationPoint) = GENE_POOL(mutationPointIdx)(rn.nextInt(GENE_POOL(mutationPointIdx).length))
    secondFittest.genes(mutationPoint) = GENE_POOL(mutationPointIdx)(rn.nextInt(GENE_POOL(mutationPointIdx).length))
  }

  //Get fittest offspring
  def getFittestOffspring: Individual = {
    if (fittest.fitness > secondFittest.fitness) return fittest
    secondFittest
  }

  //Replace least fittest individual from most fittest offspring
  def addFittestOffspring(): Unit = { //Update fitness values of offspring
    fittest.calcFitness()
    secondFittest.calcFitness()
    //Get index of least fit individual
    val leastFittestIndex = population.getLeastFittestIndex
    population.individuals(leastFittestIndex) = getFittestOffspring
  }

}
