package com.chongdianleme.mail
import org.apache.spark._
import org.apache.spark.mllib.classification.{SVMModel, SVMWithSGD}
import org.apache.spark.mllib.evaluation.{BinaryClassificationMetrics, MulticlassMetrics}
import org.apache.spark.mllib.feature.StandardScaler
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import scopt.OptionParser
import scala.collection.mutable.ArrayBuffer
/**
  * Created by 充电了么App-陈敬雷
  * 官网：http://chongdianleme.com/
  * 支持向量机方法是建立在统计学习理论的VC维理论和结构风险最小原理基础上的，根据有限的样本信息在模型的复杂性（即对特定训练样本的学习精度，Accuracy）和学习能力（即无错误地识别任意样本的能力）之间寻求最佳折衷，以期获得最好的推广能力
  */
object SVMJob {
  case class Params(
                     inputPath: String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\二值分类训练数据\\",
                     outputPath: String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\SVMOut\\",
                     modelPath: String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\SVMModel\\",
                     mode: String = "local",
                     numIterations: Int = 8
                   )
  def main(args: Array[String]) {
    val defaultParams = Params()
    val parser = new OptionParser[Params]("svmJob") {
      head("svmJob: 解析参数.")
      opt[String]("inputPath")
        .text(s"inputPath 输入目录, default: ${defaultParams.inputPath}}")
        .action((x, c) => c.copy(inputPath = x))
      opt[String]("outputPath")
        .text(s"outputPath 输入目录, default: ${defaultParams.outputPath}}")
        .action((x, c) => c.copy(outputPath = x))
      opt[String]("modelPath")
        .text(s"modelPath 模型输出, default: ${defaultParams.modelPath}}")
        .action((x, c) => c.copy(modelPath = x))
      opt[String]("mode")
        .text(s"mode 运行模式, default: ${defaultParams.mode}")
        .action((x, c) => c.copy(mode = x))
      opt[Int]("numIterations")
        .text(s"numIterations 迭代次数, default: ${defaultParams.numIterations}")
        .action((x, c) => c.copy(numIterations = x))
      note(
        """
          |SVM dataset:
        """.stripMargin)
    }
    parser.parse(args, defaultParams).map { params => {
      println("参数值：" + params)
      trainSVM(params.inputPath, params.outputPath,params.modelPath, params.mode,params.numIterations
      )
    }
    } getOrElse {
      System.exit(1)
    }
  }
  /**
  *  SVM支持向量机：SGD随机梯度下降方式训练数据，得到权重和截距
  * @param inputPath 输入目录，格式如下
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
  * @param modelPath 模型持久化存储路径
  * @param numIterations  迭代次数
  */
  def trainSVM(inputPath: String, outputPath: String, modelPath: String, mode: String, numIterations: Int): Unit = {
    val startTime = System.currentTimeMillis()
    val sparkConf = new SparkConf().setAppName("svmJob")
    sparkConf.set("spark.sql.warehouse.dir", "file:///C:/warehouse/temp/")
    sparkConf.setMaster(mode)
    val sc = new SparkContext(sparkConf)
    //加载训练数据
    val data = MLUtils.loadLabeledPoints(sc, inputPath)
    //训练数据，随机拆分数据80%做为训练集，20%作为测试集
    val splitsData = data.randomSplit(Array(0.8, 0.2))
    val (trainningData, testData) = (splitsData(0), splitsData(1))
    //把训练数据归一化处理
    val vectors = trainningData.map(lp => lp.features)
    val scaler = new StandardScaler(withMean = true, withStd = true).fit(vectors)
    val scaledData = trainningData.map(lp => LabeledPoint(lp.label, scaler.transform(lp.features)))
    //训练模型，拿80%数据作为训练集，
    val saveModel = SVMWithSGD.train(scaledData, numIterations)
    //训练好的模型可以持久化到文件中，web服务或者其他预测项目里，直接加载这个模型文件到内存里面，进行直接预测，不用每次都训练
    saveModel.save(sc, modelPath)
    //加载刚才存储的这个模型文件到内存里面，进行后面的分类预测，这个例子是在演示如果做模型的持久化和加载。
    val model = SVMModel.load(sc, modelPath)
    val trainendTime = System.currentTimeMillis()
    //训练完成，打印各个特征权重，这些权重可以放到线上缓存中，供接口使用
    println("Weights: " + model.weights.toArray.mkString("[", ", ", "]"))
    //训练完成，打印截距，截距可以放到线上缓存中，供接口使用
    println("Intercept: " + model.intercept)
    //后续处理可以把权重和截距数据存储到线上缓存，或者文件，供线上web服务加载模型使用
    //存储到线上代码自己根据业务情况来完成
    // 用测试集来评估模型的效果
    val scoreAndLabels = testData.map { point =>
      //基于模型来预测归一化后的数据特征属于哪个分类标签
      val prediction = model.predict(scaler.transform(point.features))
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
    out += ("SVM支持向量机:", time1, time2, auc, ps)
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