package com.chongdianleme.mail
import org.apache.spark._
import SparkContext._
import org.apache.spark.mllib.evaluation.{BinaryClassificationMetrics, MulticlassMetrics}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.configuration.Algo
import org.apache.spark.mllib.tree.impurity.Entropy
import org.apache.spark.mllib.tree.model.{DecisionTreeModel, RandomForestModel}
import scopt.OptionParser
import scala.collection.mutable.ArrayBuffer
/**
  * Created by 陈敬雷
  * 决策树算法Demo
  * 这个例子是用来做分类任务
  */
object DecisionTreeJob {
  case class Params(
                     inputPath: String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\二值分类训练数据\\",
                     outputPath:String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\DecisionTreeOut\\",
                     modelPath:String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\DecisionTreeModel\\",
                     mode: String = "local",
                     maxTreeDepth:Int=20 //指定树的深度，在Spark实现里面最大的深度不超过30
                   )
  def main(args: Array[String]) {
    val defaultParams = Params()
    val parser = new OptionParser[Params]("TrainDecisionTree") {
      head("TrainDecisionTreeJob: 解析参数.")
      opt[String]("inputPath")
        .text(s"inputPath 输入目录, default: ${defaultParams.inputPath}}")
        .action((x, c) => c.copy(inputPath = x))
      opt[String]("outputPath")
        .text(s"outputPath 输入目录, default: ${defaultParams.outputPath}}")
        .action((x, c) => c.copy(outputPath = x))
      opt[String]("modelPath")
        .text(s"modelPath 训练模型数据的持久化存储目录, default: ${defaultParams.modelPath}}")
        .action((x, c) => c.copy(modelPath = x))
      opt[String]("mode")
        .text(s"mode 运行模式, default: ${defaultParams.mode}")
        .action((x, c) => c.copy(mode = x))
      opt[Int]("maxTreeDepth")
        .text(s"maxTreeDepth, default: ${defaultParams.maxTreeDepth}")
        .action((x, c) => c.copy(maxTreeDepth = x))
      note(
        """
          |For example,  TrainDecisionTree dataset:
          |
        """.stripMargin)
    }
    parser.parse(args, defaultParams).map { params => {
      println("参数值：" + params)
      trainDecisionTree(params.inputPath, params.outputPath,
        params.modelPath,
        params.mode,
        params.maxTreeDepth
      )
    }
    } getOrElse {
      System.exit(1)
    }
  }
    /**
    * 决策树算法，可用于监督学习的分类
    * @param input 输入目录，格式如下
    *第一列是类的标签值，逗号后面的都是特征值，多个特征以空格分割：
    *              1,-0.222222 0.5 -0.762712 -0.833333
    *              1,-0.555556 0.25 -0.864407 -0.916667
    *              1,-0.722222 -0.166667 -0.864407 -0.833333
    *              1,-0.722222 0.166667 -0.694915 -0.916667
    *              0,0.166667 -0.416667 0.457627 0.5
    *              1,-0.5 0.75 -0.830508 -1
    *              0,0.222222 -0.166667 0.423729 0.583333
    *              1,-0.722222 -0.166667 -0.864407 -1
    *              1,-0.5 0.166667 -0.864407 -0.916667
    * @param mode 运行模式
    */
  def trainDecisionTree(input : String,outputPath:String,modelPath:String,mode:String,maxTreeDepth:Int): Unit = {
    val startTime = System.currentTimeMillis()
    val sparkConf = new SparkConf().setAppName("trainDecisionTreeJob")
    sparkConf.setMaster(mode)
    sparkConf.set("spark.sql.warehouse.dir", "file:///C:/warehouse/temp/")
    val sc = new SparkContext(sparkConf)
    //加载训练数据
    val data = MLUtils.loadLabeledPoints(sc,input)
    //缓存
    data.cache()
    //训练数据，随机拆分数据80%做为训练集，20%作为测试集
    val splits = data.randomSplit(Array(0.8, 0.2))
    val (trainingData, testData) = (splits(0), splits(1))
    //按照设置的参数来训练数据，训练完成后，得到一个模型，模型可以持久化成文件，后面再根据文件来加载初始化模型，不用每次都训练
    val model = DecisionTree.train(trainingData,Algo.Classification, Entropy,maxTreeDepth)
    val trainendTime = System.currentTimeMillis()
    //加载测试数据，预测模型准确性
    val scoreAndLabels = testData.map { point =>
      //在web项目里面也是用model.predict预测特征最大分配给哪个分类标签的概率
      val prediction = model.predict(point.features)
      (prediction, point.label)
    }
    // 二值分类通用指标ROC曲线面积
    val metrics = new BinaryClassificationMetrics(scoreAndLabels)
    //AUC评价指标
    val auROC = metrics.areaUnderROC()
    //打印ROC模型--ROC曲线值，越大越精准,ROC曲线下方的面积（Area Under the ROC Curve, AUC）提供了评价模型平均性能的另一种方法。如果模型是完美的，那么它的AUC = 1，如果模型是个简单的随机猜测模型，那么它的AUC = 0.5，如果一个模型好于另一个，则它的曲线下方面积相对较大
    println("Area under ROC = " + auROC)
    //准确度评价指标
    val metricsPrecision = new MulticlassMetrics(scoreAndLabels)
    val precision = metricsPrecision.precision
    println("precision = " + precision)
    val predictEndTime = System.currentTimeMillis()
    val time1 = s"训练时间：${(trainendTime - startTime) / (1000 * 60)} 分钟"
    val time2 = s"预测时间：${(predictEndTime - trainendTime) / (1000 * 60)} 分钟"
    val auc = s"AUC:$auROC"
    val ps = s"precision$precision"
    val out = ArrayBuffer[String]()
    out +=("决策树:",time1,time2,auc,ps)
    sc.parallelize(out,1).saveAsTextFile(outputPath)
    //model模型可以存储到文件里面
    model.save(sc, modelPath)
    //然后在需要预测的项目里，直接加载这个模型文件，来直接初始化模型，不用每次都训练
    val loadModel = DecisionTreeModel.load(sc,modelPath)
    sc.stop()
    //查看下训练模型文件里的内容
    readParquetFile(modelPath + "data/*.parquet", 8000)
  }
  /**
    * 读取Parquet文件
    * @param pathFile  文件路径
    * @param n  读取前几行
    */
  def readParquetFile(pathFile:String,n:Int): Unit =
  {
    val sparkConf = new SparkConf().setAppName("readParquetFileJob")
    sparkConf.setMaster("local")
    sparkConf.set("spark.sql.warehouse.dir", "file:///C:/warehouse/temp/")
    val sc = new SparkContext(sparkConf)
    val sqlContext = new org.apache.spark.sql.SQLContext(sc)
    val parquetFile = sqlContext.parquetFile(pathFile)
    println("开始读取文件"+pathFile)
    parquetFile.take(n).foreach(println)
    println("读取结束")
    sc.stop()
  }
}