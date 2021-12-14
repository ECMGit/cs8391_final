package models

/**
 *
 * @project: backend_scala
 * @author: Junhao Shen
 * @date: 11/1/21
 * */
case class Topic(id: Long, terms: Seq[String], termsWeight: Seq[Double])