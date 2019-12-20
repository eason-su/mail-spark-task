package com.chongdianleme.mail
//import是引用相关的类库
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.recommendation.{ALS, Rating}
import scopt.OptionParser
import scala.collection.mutable.{ArrayBuffer}

/**
  * 给用户推荐商品类
  */
object AlsUser {
  //定义main函数的入口参数
  case class Params(
                     inputPath: String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\als\\input\\充电了么购买课程日志.txt",
                     outputPath: String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\als\\output\\",
                     rank: Int = 166,
                     numIterations: Int = 5,
                     lambda: Double = 0.01,
                     alpha: Double = 0.03,
                     topCount: Int = 36,
                     mode: String = "local"
                   )

  def main(args: Array[String]) {
    val defaultParams = Params()
    val parser = new OptionParser[Params]("ChongdianlemeALSJob") {
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
      note(
        """
          			  |For example, the following command runs this app on a ChongdianlemeALSJob dataset:
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

  def run(input: String, output: String, rank: Int, numIterations: Int, recommendNum: Int, alpha: Double, lambda: Double, mode: String) = {
    val sparkConf = new SparkConf()
    sparkConf.setAppName("alsJob")
    if (mode.equals("local"))
      sparkConf.setMaster(mode)
    val sc = new SparkContext(sparkConf)
    //加载数据文件
    val data = sc.textFile(input)
    //加载数据把数据格式转化成Rating的RDD
    val ratings = data.map(_.split("\t") match { case Array(user, item) =>
      Rating(user.toInt, item.toInt, 1.0)
    })
    val trainStart = System.currentTimeMillis()
    //训练隐含模型，忽略评分。相当于布尔型的协同过滤，用户对某个商品要么喜欢，要么不喜欢，这种方式在电商更常用，简单有效。
    val model = ALS.trainImplicit(ratings, rank, numIterations, lambda, alpha)
    val trainEnd = System.currentTimeMillis()
    val trainTime = s"训练时间：${(trainEnd - trainStart)} 毫秒"
    //为所有用户推荐前几个商品集合,猜您喜欢，某用户最喜欢的前几个商品。
    val allProductsForUsers = model.recommendProductsForUsers(recommendNum)
    val out = allProductsForUsers.flatMap { case (userid, list) => {
      val result = ArrayBuffer[String]()
      list.foreach { case Rating(user, product, rate) => {
        val line = userid + "\t" + product + "\t" + rate
        println("1、allProductsForUsers = "+line)
        result += line
      }
      }
      result
    }
    }
    out.saveAsTextFile(output)
    val predictEnd = System.currentTimeMillis()
    val genTime = s"生成推荐列表时间：${(predictEnd - trainEnd)} 毫秒"
    println(trainTime)
    println(genTime)
    //为单个用户推荐商品
    val productsPerUser = model.recommendProducts(1, 20)
    productsPerUser.foreach { case Rating(user, product, rate) => {
      println("2、productsPerUser - user:" + user + "  product:" + product + "  rate:" + rate)
    }
    }
    //为单个商品推荐用户,对某个商品最感兴趣的前几个用户
    val usersPerItem = model.recommendUsers(100001, 20)
    usersPerItem.foreach { case Rating(user, product, rate) => {
      println("3、usersPerItem  == user:" + user + "  product:" + product + "  rate:" + rate)
    }
    }
    //为所有商品推荐前几个用户集合，对某个商品最感兴趣的前几个用户
    val allUsersForProducts = model.recommendUsersForProducts(recommendNum)
    allUsersForProducts.flatMap { case (product_id, list) => {
      val result = ArrayBuffer[String]()
      list.foreach { case Rating(user, product, rate) => {
        val line =  "4、allUsersForProducts = "+product_id + "\t" + user + "\t" + rate
        println(line)
      }
      }
      result
    }
    }.count()
    //
  }
}