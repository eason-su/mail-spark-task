package com.chongdianleme.mail
import org.apache.spark._
import org.apache.spark.mllib.evaluation.{MulticlassMetrics, BinaryClassificationMetrics}
import org.apache.spark.mllib.tree.model.RandomForestModel
import org.apache.spark.mllib.util.MLUtils
import scopt.OptionParser
import org.apache.spark.mllib.tree.RandomForest
import scala.collection.mutable.ArrayBuffer
/**
* Created by 充电了么App-陈敬雷
* 官网：http://chongdianleme.com/
* 随机森林是决策树的集成算法。随机森林包含多个决策树来降低过拟合的风险。随机森林同样具有易解释性、可处理类别特征、易扩展到多分类问题、不需特征缩放等性质。
* 随机森林支持二分类、多分类以及回归，适用于连续特征以及类别特征。
* 随机森林的分类比如可以用在广告点击率预测，推荐系统Rerank二次排序
* 随机森林的回归可以用来电商网站的销量预测任务等
*/
object RandomForestJob {

  case class Params(
                     inputPath: String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\二值分类训练数据\\",
                     outputPath: String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\RandomForestOut\\",
                     modelPath: String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\RandomForestModel\\",
                     mode: String = "local", //单机还是分布式运行
                     numTrees: Int = 8, //设置几棵树
                     featureSubsetStrategy: String = "all", //每次分裂候选特征数量
                     numClasses: Int = 2, //用于几个分类，二值分类设置为2，三分类设置3
                     impurity: String = "gini", //纯度计算,推荐gini
                     maxDepth: Int = 8, //树的最大深度
                     maxBins: Int = 100 //特征最大装箱数，推荐100
                   )

  def main(args: Array[String]) {
    val defaultParams = Params()
    val parser = new OptionParser[Params]("RandomForestJob") {
      head("RandomForestJob: 解析参数.")
      opt[String]("inputPath")
        .text(s"inputPath 输入目录, default: ${defaultParams.inputPath}}")
        .action((x, c) => c.copy(inputPath = x))
      opt[String]("outputPath")
        .text(s"outputPath 输出目录, default: ${defaultParams.outputPath}}")
        .action((x, c) => c.copy(outputPath = x))
      opt[String]("modelPath")
        .text(s"modelPath 模型输出, default: ${defaultParams.modelPath}}")
        .action((x, c) => c.copy(modelPath = x))
      opt[String]("mode")
        .text(s"mode 运行模式, default: ${defaultParams.mode}")
        .action((x, c) => c.copy(mode = x))
      opt[Int]("numTrees")
        .text(s"numTrees, default: ${defaultParams.numTrees}")
        .action((x, c) => c.copy(numTrees = x))
      opt[Int]("numClasses")
        .text(s"numClasses, default: ${defaultParams.numClasses}")
        .action((x, c) => c.copy(numClasses = x))
      opt[Int]("maxDepth")
        .text(s"maxDepth, default: ${defaultParams.maxDepth}")
        .action((x, c) => c.copy(maxDepth = x))
      opt[Int]("maxBins")
        .text(s"maxBins, default: ${defaultParams.maxBins}")
        .action((x, c) => c.copy(maxBins = x))
      opt[String]("featureSubsetStrategy")
        .text(s"featureSubsetStrategy, default: ${defaultParams.featureSubsetStrategy}")
        .action((x, c) => c.copy(featureSubsetStrategy = x))
      opt[String]("impurity")
        .text(s"impurity, default: ${defaultParams.impurity}")
        .action((x, c) => c.copy(impurity = x))
      note(
        """
          |For example, RandomForestJob dataset:
          |
        """.stripMargin)
    }
    parser.parse(args, defaultParams).map { params => {
      println("参数值：" + params)
      trainRandomForest(params.inputPath,
        params.outputPath, params.mode, params.numTrees,
        params.featureSubsetStrategy, params.numClasses, params.impurity,
        params.maxDepth, params.maxBins, params.modelPath
      )
    }
    } getOrElse {
      System.exit(1)
    }
  }

  def trainRandomForest(inputPath: String, outputPath: String,
                        mode: String, numTrees: Int, //用几棵树来训练
                        featureSubsetStrategy: String = "all",
                        numClasses: Int = 2, //分类个数，和训练数据的分类数保持一致
                        impurity: String = "gini", //不纯度计算,推荐gini
                        maxDepth: Int = 8, //树的最大深度8
                        maxBins: Int = 100, //特征最大装箱数，推荐100
                        modelPath: String
                       ): Unit = {
    val startTime = System.currentTimeMillis()
    val sparkConf = new SparkConf().setAppName("trainRandomForest")
    sparkConf.setMaster(mode)
    sparkConf.set("spark.sql.warehouse.dir", "file:///C:/warehouse/temp/")
    val sc = new SparkContext(sparkConf)
    //加载训练数据
    val data = MLUtils.loadLabeledPoints(sc, inputPath)
    data.cache()
    //训练数据，随机拆分数据80%做为训练集，20%作为测试集
    val splits = data.randomSplit(Array(0.8, 0.2))
    val (trainingData, testData) = (splits(0), splits(1))
    val categoricalFeaturesInfo = Map[Int, Int]()
    //训练模型，拿80%数据作为训练集，分类个数为2，此demo例子是拿二值分类例子来训练的
    //但它可以支持多分类，多分类通过numClasses参数设定
    var tempModel = RandomForest.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
      numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins)
    //训练好的模型可以持久化到文件中，web服务或者其他预测项目里，直接加载这个模型文件到内存里面，进行直接预测，不用每次都训练
    tempModel.save(sc, modelPath)
    //加载刚才存储的这个模型文件到内存里面，进行后面的分类预测，这个例子是在演示如果做模型的持久化和加载。
    val model = RandomForestModel.load(sc, modelPath)
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
    out += ("随机森林算法Demo演示:", time1, time2, auc, ps)
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