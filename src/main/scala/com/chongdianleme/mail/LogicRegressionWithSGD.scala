package com.chongdianleme.mail

import com.github.fommil.netlib.BLAS
import org.apache.spark._
import org.apache.spark.mllib.classification.{LogisticRegressionWithLBFGS, LogisticRegressionWithSGD}
import org.apache.spark.mllib.evaluation.{BinaryClassificationMetrics, MulticlassMetrics}
import org.apache.spark.mllib.feature.StandardScaler
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import scopt.OptionParser
import scala.collection.mutable.ArrayBuffer
/**
  * Created by chongdianleme 陈敬雷
  * 官网：http://chongdianleme.com/
  * SGD -- 随机梯度下降逻辑回归
  * SGD:随机从训练集选取数据训练，不归一化数据，需要专门在外面进行归一化，支持L1,L2正则化，不支持多分类。
  */
object LogicRegressionWithSGD {
  case class Params(
                 inputPath: String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\二值分类训练数据\\",
                 outputPath:String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\gsdout\\",
                 mode: String = "local",
                 stepSize:Double = 8,
                 niters:Int = 8
               )
  def main(args: Array[String]) {
    val defaultParams = Params()
    val parser = new OptionParser[Params]("TrainLogicRegressionJob") {
      head("TrainLogicRegressionWithSGDJob: 解析参数.")
      opt[String]("inputPath")
        .text(s"inputPath 输入目录, default: ${defaultParams.inputPath}}")
        .action((x, c) => c.copy(inputPath = x))
      opt[String]("outputPath")
        .text(s"outputPath 输入目录, default: ${defaultParams.outputPath}}")
        .action((x, c) => c.copy(outputPath = x))
      opt[String]("mode")
        .text(s"mode 运行模式, default: ${defaultParams.mode}")
        .action((x, c) => c.copy(mode = x))
      opt[Double]("stepSize")
        .text(s"stepSize 步长, default: ${defaultParams.stepSize}")
        .action((x, c) => c.copy(stepSize = x))
      opt[Int]("niters")
        .text(s"niters 迭代次数, default: ${defaultParams.niters}")
        .action((x, c) => c.copy(niters = x))
      note(
        """
          |For example, the following command runs this app on a TrainLogicRegressionJob dataset:
          |
        """.stripMargin)
    }
    parser.parse(args, defaultParams).map { params => {
        println("参数值："+params)
        trainLogicRegressionWithSGD(params.inputPath,
          params.outputPath,
          params.mode,params.stepSize,params.niters
        )
    }
    } getOrElse {
      System.exit(1)
    }
  }
  /**
    *  SGD随机梯度下降方式训练数据，得到权重和截距
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
    * @param stepSize 步长
    * @param niters  迭代次数
    */
  def trainLogicRegressionWithSGD(input : String,outputPath:String,mode:String,stepSize:Double,niters:Int): Unit = {
    val startTime = System.currentTimeMillis()
    //SparkConf配置实例化
    val sparkConf = new SparkConf().setAppName("trainLogicRegressionWithSGD")
    //运行模式，是在local本地运行，hadoop的yarn上分布式运行等
    sparkConf.setMaster(mode)
    val sc = new SparkContext(sparkConf)
    //加载训练数据
    val data = MLUtils.loadLabeledPoints(sc,input)
    //对数据进行随机的切分，70%作为训练集，30%作为测试集
    val splitsData = data.randomSplit(Array(0.7,0.3))
    val (trainningData, testData) = (splitsData(0), splitsData(1))
    //SGD算法本身不支持归一化，需要我们在训练之前先自己做好归一化处理，当然不归一化也是可以训练的，这是归一化后一般效果和准确率等会更好一些。
    val vectors = trainningData.map(lp=>lp.features)
    val scaler = new StandardScaler(withMean=true,withStd=true).fit(vectors)
    //val scaler = new StandardScaler().fit(vectors)
    val scaledData = trainningData.map(lp=>LabeledPoint(lp.label,scaler.transform(lp.features)))
    scaledData.cache()
    //开始训练数据
    val model = LogisticRegressionWithSGD.train(scaledData,niters, stepSize)
    val trainendTime = System.currentTimeMillis()
    //训练完成，打印各个特征权重，这些权重可以放到线上缓存中，供接口使用
    println("Weights: " + model.weights.toArray.mkString("[", ", ", "]"))
    //训练完成，打印截距，截距可以放到线上缓存中，供接口使用
    println("Intercept: " + model.intercept)
    //把权重和截距刷新到线上缓存或文件，用于线上模型的加载，进而基于这个模型来预测
    val wi = model.weights.toArray.mkString(",")+";"+model.intercept
    //加载测试数据，预测模型准确性
    val parsedData = testData
    val scoreAndLabels = parsedData.map { point =>
      val prediction = model.predict(scaler.transform(point.features))
      (prediction, point.label)
    }
    // Get evaluation metrics.二值分类通用指标ROC曲线面积
    val metrics = new BinaryClassificationMetrics(scoreAndLabels)
    val auROC = metrics.areaUnderROC()
    //打印ROC模型--ROC曲线值，越大越精准,ROC曲线下方的面积（Area Under the ROC Curve, AUC）提供了评价模型平均性能的另一种方法。如果模型是完美的，那么它的AUC = 1，如果模型是个简单的随机猜测模型，那么它的AUC = 0.5，如果一个模型好于另一个，则它的曲线下方面积相对较大
    println("Area under ROC = " + auROC)
    //准确度
    val metricsPrecision = new MulticlassMetrics(scoreAndLabels)
    val precision = metricsPrecision.precision
    println("precision = " + precision)
    val predictEndTime = System.currentTimeMillis()
    val time1 = s"训练时间：${(trainendTime - startTime) / (1000 * 60)} 分钟"
    val time2 = s"预测时间：${(predictEndTime - trainendTime) / (1000 * 60)} 分钟"
    //打印AUC的值，值越大，效果越好
    val auc = s"AUC:$auROC"
    val ps = s"precision$precision"
    val out = ArrayBuffer[String]()
    out +=("逻辑归回SGD:",time1,time2,auc,ps)
    sc.parallelize(out,1).saveAsTextFile(outputPath)
    sc.stop()
  }

  /**
    * 用BLAS基础线性代数子程序库来实时的高效预测数据特征属于正标签的概率值
    * @param dataMatrix 数据特征
    * @param weightMatrix  权重
    * @param intercept  截距
    * @return 预测数据特征属于正标签的概率值，0到1之间的小数，数值越大概率越高。
    */
  def getScore(dataMatrix: Array[Double],
               weightMatrix: Array[Double],
               intercept: Double) = {
    val n = weightMatrix.size
    val dot = BLAS.getInstance().ddot(n, weightMatrix, 1, dataMatrix, 1)
    val margin = dot + intercept
    val score = 1.0 / (1.0 + math.exp(-margin))
    score
  }
}