package models

/**
 *
 * @project: backend_scala
 * @author: Junhao Shen
 * @date: 11/16/21
 * */
class Population {

  val popSize = 10
  val individuals = new Array[Individual](10)
  var fittest = 0.0

  //Initialize population
  def initializePopulation(size: Int, genelen: Int = 3): Unit = {
    for (i <- 0 until individuals.length) {
      individuals(i) = new Individual(genelen)
    }
  }

  //Get the fittest individual
  def getFittest: Individual = {
    var maxFit = Double.MinValue
    var maxFitIndex = 0
    for (i <- 0 until individuals.length) {
      if (maxFit <= individuals(i).fitness) {
        maxFit = individuals(i).fitness
        maxFitIndex = i
      }
    }
    fittest = individuals(maxFitIndex).fitness
    individuals(maxFitIndex)
  }

  //Get the second most fittest individual
  def getSecondFittest: Individual = {
    var maxFit1 = 0
    var maxFit2 = 0
    for (i <- 0 until individuals.length) {
      if (individuals(i).fitness > individuals(maxFit1).fitness) {
        maxFit2 = maxFit1
        maxFit1 = i
      }
      else if (individuals(i).fitness > individuals(maxFit2).fitness) maxFit2 = i
    }
    individuals(maxFit2)
  }

  //Get index of least fittest individual
  def getLeastFittestIndex: Int = {
    var minFitVal = Double.MinValue
    var minFitIndex = 0
    for (i <- 0 until individuals.length) {
      if (minFitVal >= individuals(i).fitness) {
        minFitVal = individuals(i).fitness
        minFitIndex = i
      }
    }
    minFitIndex
  }

  //Calculate fitness of each individual
  def calculateFitness(): Unit = {
    for (i <- 0 until individuals.length) {
      individuals(i).calcFitness()
    }
    getFittest
  }
}

