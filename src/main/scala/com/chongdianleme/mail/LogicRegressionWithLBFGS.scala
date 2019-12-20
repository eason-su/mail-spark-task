package com.chongdianleme.mail
import com.github.fommil.netlib.BLAS
import org.apache.spark._
import org.apache.spark.mllib.classification.{LogisticRegressionWithLBFGS, LogisticRegressionWithSGD}
import org.apache.spark.mllib.evaluation.{MulticlassMetrics, BinaryClassificationMetrics}
import org.apache.spark.mllib.feature.StandardScaler
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import scopt.OptionParser
import scala.collection.mutable.ArrayBuffer

/**
  * Created by chongdianleme 陈敬雷
  * 官网：http://chongdianleme.com/
  * L-BFGS -- 拟牛顿法逻辑回归
  * 所有的数据都会参与训练，算法融入方差归一化和均值归一化。支持L1,L2正则化，支持多分类。
  */
object LogicRegressionWithLBFGS {
  case class Params(
                     inputPath: String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\特征分类训练数据",
                     outputPath:String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\LBFGSout\\",
                     mode: String = "local"
                   )
  def main(args: Array[String]) {
    val defaultParams = Params()
    val parser = new OptionParser[Params]("TrainLogicRegressionJob") {
      head("TrainLogicRegressionJob: 解析参数.")
      opt[String]("inputPath")
        .text(s"inputPath 输入目录, default: ${defaultParams.inputPath}}")
        .action((x, c) => c.copy(inputPath = x))
      opt[String]("outputPath")
        .text(s"outputPath 输入目录, default: ${defaultParams.outputPath}}")
        .action((x, c) => c.copy(outputPath = x))
      opt[String]("mode")
        .text(s"mode 运行模式, default: ${defaultParams.mode}")
        .action((x, c) => c.copy(mode = x))
      note(
        """
          |For example, the following command runs this app on a mixjob dataset:
          |
        """.stripMargin)
    }
    parser.parse(args, defaultParams).map { params => {
      println("参数值："+params)
        println("trainLogicRegressionwithLBFGS!")
        trainLogicRegressionwithLBFGS(params.inputPath,
          params.outputPath,
          params.mode
        )
    }
    } getOrElse {
      System.exit(1)
    }
  }
    /**
    * LBFGS拟牛顿法方式训练数据，得到的模型，主要是权重和截距，然后可以
    * 在把权重和截距存储到缓存、数据库、或文件，供线上web服务初始化的时候加载权重和截距，进而预测特征数据是哪个标签。
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
  def trainLogicRegressionwithLBFGS(input : String,
                                    outputPath:String,
                                    mode:String): Unit = {
    val startTime = System.currentTimeMillis()
    val sparkConf = new SparkConf().setAppName("trainLogicRegressionwithLBFGS")
    sparkConf.setMaster(mode)
    //首先SparkContext实例化
    val sc = new SparkContext(sparkConf)
    //用loadLabeledPoints加载训练数据
    val data = MLUtils.loadLabeledPoints(sc,input)
    //把训练数据随机拆分成两份，70%作为训练集，30%作为测试集
    //当然也可以80%、20%这么拆分
    val splitsData = data.randomSplit(Array(0.7,0.3))
    val (trainningData, testData) = (splitsData(0), splitsData(1))
    //把训练数据归一化处理，这样效果会更好一点，当然对于LBFGS来说这不是必须的
    val vectors = trainningData.map(lp=>lp.features)
    val scaler = new StandardScaler(withMean=true,withStd=true).fit(vectors)
    //val scaler = new StandardScaler().fit(vectors)
    val scaledData = trainningData.map(lp=>LabeledPoint(lp.label,scaler.transform(lp.features)))
    scaledData.cache()
    val model = new LogisticRegressionWithLBFGS()
      .setNumClasses(3)//二值分类设置为2就行,三个分类设置3
      .run(trainningData)
    val trainendTime = System.currentTimeMillis()
    //训练完成，打印各个特征权重，这些权重可以放到线上缓存中，供接口使用
    println("Weights: " + model.weights.toArray.mkString("[", ", ", "]"))
    //训练完成，打印截距，截距可以放到线上缓存中，供接口使用
    println("Intercept: " + model.intercept)
    //把权重和截距刷新到线上缓存、数据库等
    val wi = model.weights.toArray.mkString(",")+";"+model.intercept
    //后续处理可以把权重和截距数据存储到线上缓存，或者文件，供线上web服务加载模型使用
    //预测精准性
    val weights = model.weights
    val intercept = model.intercept
    val predictionAndLabels = testData.map { case LabeledPoint(label, features) =>
      val prediction = model.predict(scaler.transform(features))
      (prediction, label)
    }
    // Get evaluation metrics.
    val metrics = new MulticlassMetrics(predictionAndLabels)
    //效果评估指标：准确度
    val precision = metrics.precision
    println("Precision = " + precision)
    val metricsAUC = new BinaryClassificationMetrics(predictionAndLabels)
    //效果评估指标：AUC，值越大越好
    val auROC = metricsAUC.areaUnderROC()
    println("auROC:"+auROC)
    val predictEndTime = System.currentTimeMillis()
    val time1 = s"训练时间：${(trainendTime - startTime) / (1000 * 60)} 分钟"
    val time2 = s"预测时间：${(predictEndTime - trainendTime) / (1000 * 60)} 分钟"
    val auc = s"AUC:$auROC"
    val ps = s"precision:$precision"
    val out = ArrayBuffer[String]()
    out +=("逻辑归回LBFGS:",time1,time2,auc,ps)
    sc.parallelize(out,1).saveAsTextFile(outputPath)
  }
}