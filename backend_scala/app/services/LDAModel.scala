package services
import org.apache.spark.sql.{Row, SaveMode, SparkSession}
import org.apache.spark.sql.functions.lower
import java.util.Properties

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.HashMap
import scala.collection.mutable.WrappedArray

/**
 *
 * @project: backend_scala
 * @author: Junhao Shen
 * @date: 10/24/21
 * */
object LDAModel extends Serializable {

  val spark : SparkSession = SparkSession.builder
    .appName("LDA Topic Modeling")
    .master("local") // use spark in local mode
    .getOrCreate()

  def train(): Unit = {
    //RDD based API reference: https://databricks-prod-cloudfront.cloud.databricks.com/public/4027ec902e239c93eaaa8714f173bcfc/3741049972324885/3783546674231782/4413065072037724/latest.html
    //spark ml API reference: https://spark.apache.org/docs/latest/ml-clustering.html


    import spark.implicits._
    // read data from mysql
    val sql="""SELECT pid, abstract FROM papers WHERE abstract IS NOT NULL"""
    val df = spark.read
      .format("jdbc")
      .option("url", "jdbc:mysql://localhost:3306/soc_lab1?serverTimezone=UTC")
      .option("dbtable",  s"( $sql ) t")
      .option("user", "root")
      .option("password", "password")
      .load()
//    df.show()
//    println(df.schema("pid").dataType)

      // read from csv file
//    val df = spark.read.csv("papers.csv").toDF("id", "corpus")


    val df2 = df.withColumn("abstract", lower(df.col("abstract")))
    val df3 = df2.withColumn("pid", df2.col("pid").cast("long"))
//    println(df3.schema("pid").dataType)
//    df3.filter("abstract is null").show()


    import org.apache.spark.ml.feature.RegexTokenizer

    // Set params for RegexTokenizer
    val tokenizer = new RegexTokenizer()
      .setPattern("[\\W_]+")
      .setMinTokenLength(4) // Filter away tokens with length < 4
      .setInputCol("abstract")
      .setOutputCol("tokens")
    val tokenized_df = tokenizer.transform(df3)
    // load stop words
    val stopwords = spark.sparkContext.textFile("public/stop_words.txt").collect()

    // remove stop words
    import org.apache.spark.ml.feature.StopWordsRemover

    // Set params for StopWordsRemover
    val remover = new StopWordsRemover()
      .setStopWords(stopwords) // This parameter is optional
      .setInputCol("tokens")
      .setOutputCol("filtered")

    // Create new DF with Stopwords removed
    val filtered_df = remover.transform(tokenized_df)

    import org.apache.spark.ml.feature.CountVectorizer

    // Set params for CountVectorizer
    val vectorizer = new CountVectorizer()
      .setInputCol("filtered")
      .setOutputCol("features")
      .setVocabSize(2000)
      .setMinDF(5)
      .fit(filtered_df)

//    vectorizer.save("public/vectorizer")
    vectorizer.write.overwrite().save("public/vectorizer")
    filtered_df.write.mode(SaveMode.Overwrite).save("public/filtered_df.parquet")

    // Create vector of token counts
    val countVectors = vectorizer.transform(filtered_df).select("pid", "features")
//    countVectors.show()
//    print(countVectors.schema("features").dataType)


    /**
     * Convert DF to RDD
     */
//    import org.apache.spark.mllib.linalg.Vector
//    val lda_countVector = countVectors.map { case Row(pid: Long, countVector: Vector) => (pid, countVector) }
//    display(lda_countVector)

    val numTopics = 20
    import org.apache.spark.ml.clustering.LDA

//     Trains a LDA model by using Dataframe Based API
    val lda = new LDA().setK(numTopics).setMaxIter(3)
    println("spark started to training...")
    val model = lda.fit(countVectors)

    val ll = model.logLikelihood(countVectors)
    val lp = model.logPerplexity(countVectors)
    println(s"The lower bound on the log likelihood of the entire corpus: $ll")
    println(s"The upper bound on perplexity: $lp")

    model.write.overwrite().save("public/lda_model")

    // Describe topics.
    val topicsIndices = model.describeTopics(20)
    println("The topics described by their top-weighted terms:")
    topicsIndices.show(false)

    // Shows the result.
    val transformed = model.transform(countVectors)
    transformed.show(false)

    // save to mysql(deprecated)
//    val topics_table = "topics"
//    val topics_distributions = "topic_distributions"
//    val connectionProperties = new Properties()
//
//    connectionProperties.put("user", "root")
//    connectionProperties.put("password", "password")
//
//    topicsIndices.write.jdbc("jdbc:mysql://localhost:3306/soc_lab1", s"${topics_table}", connectionProperties)
//    transformed.write.jdbc("jdbc:mysql://localhost:3306/soc_lab1", s"${topics_distributions}", connectionProperties)

//    topicsIndices.withColumn("termIndices").write.format("com.databricks.spark.csv").save("public/topic_table.csv")
//    transformed.write.format("com.databricks.spark.csv").save("public/topic_distributions.csv")

//    println(topicsIndices.schema("termIndices").dataType, topicsIndices.schema("termWeights").dataType)
    topicsIndices.schema.fields.foreach(f=>println(f.name +","+f.dataType))
    val vocabList = vectorizer.vocabulary
    val topics_array = topicsIndices.select("termIndices", "termWeights").collect()


    val topics = topics_array.map { case Row(terms: WrappedArray[Integer], termWeights: WrappedArray[Double]) =>
      terms.map(vocabList(_)).zip(termWeights)
    }
    println(s"$numTopics topics:")
    topics.zipWithIndex.foreach { case (topic, i) =>
      println(s"TOPIC $i")
      topic.foreach { case (term, weight) => println(s"$term\t$weight") }
      println(s"==========")
    }

    /**
     * train LDA by using RDD based API, deprecated
     */
    //    import org.apache.spark.mllib.clustering.{LDA, OnlineLDAOptimizer}
//
//    // Set LDA params
//    val lda = new LDA()
//      .setOptimizer("em")
//      .setK(numTopics)
//      .setMaxIterations(3)
//      .setDocConcentration(-1) // use default values
//      .setTopicConcentration(-1) // use default values
//
//    val ldaModel = lda.run(lda_countVector.rdd)

  }


  def load(paperServices: PaperServices): Array[WrappedArray[(String, Double)]] = {
    // load trained LDA Model from Storage
    import org.apache.spark.ml.clustering.LocalLDAModel
    import org.apache.spark.ml.feature.CountVectorizerModel
    val model = LocalLDAModel.load("public/lda_model")
    val vectorizer = CountVectorizerModel.load("public/vectorizer")

    val filtered_df = spark.read.parquet("public/filtered_df.parquet")
    val countVectors = vectorizer.transform(filtered_df).select("pid", "features")
    // Describe topics.
    val topicsIndices = model.describeTopics(10)
    print(topicsIndices.schema("termWeights").dataType)
    println("The topics described by their top-weighted terms:")
//    topicsIndices.show(false)

    // Shows the result.
    val transformed = model.transform(countVectors)
//    transformed.show(false)
    println(transformed.schema("pid").dataType)
    println(transformed.schema("topicDistribution").dataType)

    val vocabList = vectorizer.vocabulary
    val topics_array = topicsIndices.select("termIndices", "termWeights").collect()


    val topic_pid_map = new HashMap[Int, ListBuffer[Long]]()

    val topic_category = transformed.select("pid", "topicDistribution").collect()
//    println(topic_category(0).getStruct(1))

    //categorize paper by its highest topic distributions
//    import org.apache.spark.ml.linalg.DenseVector
//    val temp = topic_category.map {
//      case Row(pid: Long, topicDistribution: DenseVector) => {
//        //                            paperServices.updatePaperCategory(Option(topicDistribution.toArray.zipWithIndex.maxBy(_._1)._2), pid
//        val topicID = topicDistribution.toArray.zipWithIndex.maxBy(_._1)._2
//        paperServices.updatePaperCategory(Option(topicID), pid)
//        if (topic_pid_map.contains(topicID)) {
//          val lb = topic_pid_map.get(topicID).get
//          lb += pid
//        } else {
//          val lb = new ListBuffer[Long]()
//          lb += pid
//          topic_pid_map.put(topicID, lb)
//        }
//      }
//    }

    val numTopics = 20
    val topics = topics_array.map { case Row(terms: WrappedArray[Integer], termWeights: WrappedArray[Double]) =>
      terms.map(vocabList(_)).zip(termWeights)
    }
    println(s"$numTopics topics:")
    topics.zipWithIndex.foreach { case (topic, i) =>
      println(s"TOPIC $i")
      topic.foreach { case (term, weight) => println(s"$term\t$weight") }
      println(s"==========")
    }
    return topics
  }


}
