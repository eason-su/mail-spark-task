package com.chongdianleme.mail
import org.apache.spark._
import org.apache.spark.mllib.evaluation.{BinaryClassificationMetrics, MulticlassMetrics}
import org.apache.spark.mllib.util.MLUtils
import scopt.OptionParser
import org.apache.spark.mllib.tree.GradientBoostedTrees
import org.apache.spark.mllib.tree.configuration.BoostingStrategy
import org.apache.spark.mllib.tree.model.{GradientBoostedTreesModel, RandomForestModel}

import scala.collection.mutable.ArrayBuffer
/**
  * Created by 充电了么App-陈敬雷
  * 官网：http://chongdianleme.com/
  * 梯度提升树是一种决策树的集成算法。它通过反复迭代训练决策树来最小化损失函数。决策树类似，梯度提升树具有可处理类别特征、易扩展到多分类问题、不需特征缩放等性质。Spark.ml通过使用现有decision tree工具来实现。
  */
object GradientBoostedTreesJob {
  case class Params(
                     inputPath: String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\二值分类训练数据\\",
                     outputPath: String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\GBDTOut\\",
                     modelPath: String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\GBDTModel\\",
                     mode: String = "local",
                     numIterations: Int = 8
                   )
  def main(args: Array[String]) {
    val defaultParams = Params()
    val parser = new OptionParser[Params]("GBDTJob") {
      head("GBDTJob: 解析参数.")
      opt[String]("inputPath")
        .text(s"inputPath 输入目录, default: ${defaultParams.inputPath}}")
        .action((x, c) => c.copy(inputPath = x))
      opt[String]("outputPath")
        .text(s"outputPath 输入目录, default: ${defaultParams.outputPath}}")
        .action((x, c) => c.copy(outputPath = x))
      opt[String]("mode")
        .text(s"mode 运行模式, default: ${defaultParams.mode}")
        .action((x, c) => c.copy(mode = x))
      opt[Int]("numIterations")
        .text(s"numIterations 迭代次数, default: ${defaultParams.numIterations}")
        .action((x, c) => c.copy(numIterations = x))
      note(
        """
          |For example, a GBDT dataset:
        """.stripMargin)
    }
    parser.parse(args, defaultParams).map { params => {
      println("参数值：" + params)
      trainGBDT(params.inputPath, params.outputPath, params.modelPath, params.mode, params.numIterations)
    }
    } getOrElse {
      System.exit(1)
    }
  }

  /**
    * 训练GBDT模型，以及如何持久化模型和加载，查看模型
    * @param inputPath 输入目录，格式如下
    *              第一列是类的标签值，逗号后面的都是特征值，多个特征以空格分割：
    *              1,-0.222222 0.5 -0.762712 -0.833333
    *              1,-0.555556 0.25 -0.864407 -0.916667
    *              1,-0.722222 -0.166667 -0.864407 -0.833333
    *              1,-0.722222 0.166667 -0.694915 -0.916667
    *              0,0.166667 -0.416667 0.457627 0.5
    *              1,-0.5 0.75 -0.830508 -1
    *              0,0.222222 -0.166667 0.423729 0.583333
    *              1,-0.722222 -0.166667 -0.864407 -1
    *              1,-0.5 0.166667 -0.864407 -0.916667
    * @param mode  运行模式
    * @numIterations 迭代次数
    */
  def trainGBDT(inputPath: String, outputPath: String, modelPath: String, mode: String, numIterations: Int): Unit = {
    val startTime = System.currentTimeMillis()
    val sparkConf = new SparkConf().setAppName("GBDTJob")
    sparkConf.set("spark.sql.warehouse.dir", "file:///C:/warehouse/temp/")
    sparkConf.setMaster(mode)
    val sc = new SparkContext(sparkConf)
    //加载训练数据
    val data = MLUtils.loadLabeledPoints(sc, inputPath)
    data.cache()
    //训练数据，随机拆分数据80%做为训练集，20%作为测试集
    val splits = data.randomSplit(Array(0.8, 0.2))
    val (trainingData, testData) = (splits(0), splits(1))
    //设置是分类任务还是回归任务
    val boostingStrategy = BoostingStrategy.defaultParams("Classification")
    //设置迭代次数
    boostingStrategy.numIterations = numIterations
    //训练模型，拿80%数据作为训练集
    val tempModel = GradientBoostedTrees.train(trainingData, boostingStrategy)
    //训练好的模型可以持久化到文件中，web服务或者其他预测项目里，直接加载这个模型文件到内存里面，进行直接预测，不用每次都训练
    tempModel.save(sc, modelPath)
    //加载刚才存储的这个模型文件到内存里面，进行后面的分类预测，这个例子是在演示如果做模型的持久化和加载。
    val model = GradientBoostedTreesModel.load(sc, modelPath)
    val trainendTime = System.currentTimeMillis()
    // 用测试集来评估模型的效果
    val predictData = testData
    val testErr = predictData.map { point =>
      //基于模型来预测数据特征属于哪个分类标签
      val prediction = model.predict(point.features)
      if (point.label == prediction) 1.0 else 0.0
    }.mean()
    println("Test Error = " + testErr)
    val scoreAndLabels = predictData.map { point =>
      val prediction = model.predict(point.features)
      (prediction, point.label)
    }
    // 模型评估指标：AUC   二值分类通用指标ROC曲线面积AUC
    val metrics = new BinaryClassificationMetrics(scoreAndLabels)
    val auROC = metrics.areaUnderROC()
    //打印ROC模型--ROC曲线值，越大越精准,ROC曲线下方的面积（Area Under the ROC Curve, AUC）提供了评价模型平均性能的另一种方法。如果模型是完美的，那么它的AUC = 1，如果模型是个简单的随机猜测模型，那么它的AUC = 0.5，如果一个模型好于另一个，则它的曲线下方面积相对较大
    println("Area under ROC = " + auROC)
    //模型评估指标：准确度
    val metricsPrecision = new MulticlassMetrics(scoreAndLabels)
    val precision = metricsPrecision.precision
    println("precision = " + precision)
    val predictEndTime = System.currentTimeMillis()
    val time1 = s"训练时间：${(trainendTime - startTime) / (1000 * 60)} 分钟"
    val time2 = s"预测时间：${(predictEndTime - trainendTime) / (1000 * 60)} 分钟"
    val auc = s"AUC:$auROC"
    val ps = s"precision$precision"
    val out = ArrayBuffer[String]()
    out += ("GBDT:", time1, time2, auc, ps)
    sc.parallelize(out, 1).saveAsTextFile(outputPath)
    sc.stop()
    //查看下训练模型文件里的内容
    readParquetFile(modelPath + "data/*.parquet", 8000)
  }
  /**
    * 读取Parquet文件
    *
    * @param pathFile 文件路径
    * @param n        读取前几行
    */
  def readParquetFile(pathFile: String, n: Int): Unit = {
    val sparkConf = new SparkConf().setAppName("readParquetFileJob")
    sparkConf.setMaster("local")
    sparkConf.set("spark.sql.warehouse.dir", "file:///C:/warehouse/temp/")
    val sc = new SparkContext(sparkConf)
    val sqlContext = new org.apache.spark.sql.SQLContext(sc)
    val parquetFile = sqlContext.parquetFile(pathFile)
    println("开始读取文件" + pathFile)
    parquetFile.take(n).foreach(println)
    println("读取结束")
    sc.stop()
  }
}