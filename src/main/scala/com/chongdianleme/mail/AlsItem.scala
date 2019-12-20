package com.chongdianleme.mail

import org.jblas.DoubleMatrix

import org.apache.spark.mllib.recommendation.{ALS, Rating}
import org.apache.spark.{SparkConf, SparkContext}
import scopt.OptionParser

import scala.collection.mutable.{ArrayBuffer, ListBuffer}

/**
  * Created by cjl on 2015/11/21.
  */
object AlsItem {

  case class Params(
                     inputPath: String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\als\\input\\充电了么购买课程日志.txt",
                     outputPath: String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\als\\output\\",
                     rank: Int = 166,
                     numIterations: Int = 5,
                     lambda: Double = 0.01,
                     alpha: Double = 0.03,
                     topCount: Int = 36,
                     mode:String="local"
                   )
  def main(args: Array[String]) {
    val defaultParams = Params()
    val parser = new OptionParser[Params]("ALSParser") {
      head("als: params.")
      opt[String]("inputPath")
        .text(s"inputPath, default: ${defaultParams.inputPath}}")
        .action((x, c) => c.copy(inputPath = x))
      opt[String]("outputPath")
        .text(s"outputPath, default: ${defaultParams.outputPath}}")
        .action((x, c) => c.copy(outputPath = x))
      opt[Int]("rank")
        .text(s"rank, default: ${defaultParams.rank}}")
        .action((x, c) => c.copy(rank = x))
      opt[Int]("numIterations")
        .text(s"numIterations, default: ${defaultParams.numIterations}}")
        .action((x, c) => c.copy(numIterations = x))
      opt[Int]("topCount")
        .text(s"topCount, default: ${defaultParams.topCount}}")
        .action((x, c) => c.copy(topCount = x))
      opt[Double]("lambda")
        .text(s"lambda, default: ${defaultParams.lambda}}")
        .action((x, c) => c.copy(lambda = x))
      opt[Double]("alpha")
        .text(s"alpha, default: ${defaultParams.alpha}}")
        .action((x, c) => c.copy(alpha = x))
      opt[String]("mode")
        .text(s"mode, default: ${defaultParams.mode}}")
        .action((x, c) => c.copy(mode = x))
      note( """
              			  |For example, the following command runs this app on a mixjob dataset:
              			  |
            			""".stripMargin)
    }
    parser.parse(args,
      defaultParams).map { params => {
      println("params：" +
        params)
      run(
        params.
          inputPath, params.outputPath,
        params.rank, params.numIterations, params.topCount, params.alpha, params.lambda, params.mode)
    }
    } getOrElse {
      System.exit(1)
    }
  }

  def
  run1(input:
       String,
       output: String, rank: Int, numIterations: Int, recommendNum: Int, alpha: Double, lambda: Double,mode:String) = {
    val sparkConf = new SparkConf()
    sparkConf.setAppName("als")
    if (mode.equals("local"))
      sparkConf.setMaster("local")
    val sc = new SparkContext(sparkConf)
    val data = sc.textFile(input)
    val ratings = data.map(_.split("\t") match { case Array(user, item) =>
      Rating(user.toInt, item.toInt, 1.0)
    })
    val trainStart = System.currentTimeMillis()
    val model = ALS.trainImplicit(ratings, rank, numIterations, lambda, alpha)
    val trainEnd = System.currentTimeMillis()
    val time1 = s"训练时间：${(trainEnd-trainStart)/(1000*60)} 分钟"
    val itemId = 16811162
    val itemIds = data.map(_.split("\t") match { case Array(user, item) =>item.toInt})
    val itemFactor = model.productFeatures.lookup(itemId).head
    val itemVector = new DoubleMatrix(itemFactor)
    val sim = model.productFeatures.map{case (id,factor)=>
      val factorVector = new DoubleMatrix(factor)
      val sim1 = cosineSimilarity(factorVector,itemVector)
      println("sim1"+sim1)
      (id,sim1)
    }
    val sortedSims = sim.top(10)(Ordering.by(_._2))
    println(sortedSims.take(10).mkString("\n"))
    /*<dependency>
      <groupId>org.jblas</groupId>
      <artifactId>jblas</artifactId>
      <version>${jblas.version}</version>
      <scope>test</scope>
    </dependency>*/

  }

  def run(input:
      String,
      output: String, rank: Int, numIterations: Int, recommendNum: Int, alpha: Double, lambda: Double,mode:String) = {
    val sparkConf = new SparkConf()
    sparkConf.setAppName("als")
    if (mode.equals("local"))
      sparkConf.setMaster("local")
    val sc = new SparkContext(sparkConf)
    val data = sc.textFile(input)
    //加载数据把数据格式转化成Rating的RDD
    val ratings = data.map(_.split("\t") match { case Array(user, item) =>
      Rating(user.toInt, item.toInt, 1.0)
    })
    val trainStart = System.currentTimeMillis()
    //训练隐含模型，忽略评分
    val model = ALS.trainImplicit(ratings, rank, numIterations, lambda, alpha)
    val trainEnd = System.currentTimeMillis()
    val time1 = s"训练时间：${(trainEnd - trainStart) / (1000 * 60)} 分钟"
    //val itemIds = data.map(_.split("\t") match { case Array(user, item) => item.toInt}).distinct().toArray()
    val productFeatures = model.productFeatures
    val idFactorMap = productFeatures.collectAsMap()
    val sim = productFeatures.flatMap { case (id, factor) =>
      val topList = new ListBuffer[String]()
      val leftVector = new DoubleMatrix(factor)
      val resultMap = collection.mutable.Map[Int, Double]()
      idFactorMap.foreach { case (rightId, rightFactor) => {
        val rightVector = new DoubleMatrix(rightFactor)
        val ratio = cosineSimilarity(rightVector, leftVector)
        resultMap.getOrElseUpdate(rightId, ratio)
      }
      }
      val sorted = resultMap.toList.sortBy(-_._2)
      val topItems = sorted.take(recommendNum)
      topItems.foreach { case (rightItemId, ratio) =>
        topList += id + "\t" + rightItemId + "\t" + ratio
      }
      topList
    }
    sim.saveAsTextFile(output)
    val predictEnd = System.currentTimeMillis()
    val time2 = s"生成推荐列表时间：${(predictEnd - trainEnd) / (1000 * 60)} 分钟"
    val array = ArrayBuffer[String]()
    array +=(time1, time2)
    sc.parallelize(array, 1).map(line => line).saveAsTextFile(output + "time")
  }

  /**
    * 计算余弦相似度
    * @param vec1
    * @param vec2
    * @return  返回相似度分值
    */
  def cosineSimilarity(vec1:DoubleMatrix,vec2:DoubleMatrix): Double =
  {
    vec1.dot(vec2)/(vec1.norm2()*vec2.norm2)
  }
}
