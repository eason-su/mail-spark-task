package com.chongdianleme.mail
import com.chongdianleme.mail.SVMJob.readParquetFile
import org.apache.spark.ml.classification.{MultilayerPerceptronClassificationModel, MultilayerPerceptronClassifier}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.sql.SparkSession
import scopt.OptionParser
import scala.collection.mutable.ArrayBuffer
/**
  * Created by 充电了么App-陈敬雷
  * 官网：http://chongdianleme.com/
  * MLP（Multi-Layer Perceptron），即多层感知器，是一种趋向结构的人工神经网络，映射一组输入向量到一组输出向量。MLP可以被看做是一个有向图，由多个节点层组成，每一层全连接到下一层。除了输入节点，每个节点都是一个带有非线性激活函数的神经元（或称处理单元）。一种被称为反向传播算法的监督学习方法常被用来训练MLP。MLP是感知器的推广，克服了感知器无法实现对线性不可分数据识别的缺点。
  */
object MultilayerPerceptronJob {
  case class LableFeature(label: Double, features: Vector)
  case class Params(
                     inputPath: String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\sample_multiclass_classification_data.txt",
                     outputPath:String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\MultilayerPerceptronOut\\",
                     modelPath:String="file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\MultilayerPerceptronModel\\",
                     warehousePath:String = "file:///c:/tmp/spark-warehouse",
                     featureCount:Int = 4,//数据特征有几个
                     intermediate1:Int = 166,//设置两个隐藏层，这是第一个隐藏层节点数为166
                     intermediate2:Int = 136,//这是第二个隐藏层，节点数为136
                     classCount:Int = 3,//输出层，也就是分类标签数。这次我们是三值多分类
                     mode: String = "local"
                   )
  def main(args: Array[String]): Unit = {
    val defaultParams = Params()
    val parser = new OptionParser[Params]("MultilayerPerceptronJob") {
      head("MultilayerPerceptronJob: 解析参数.")
      opt[String]("inputPath")
        .text(s"inputPath 输入目录, default: ${defaultParams.inputPath}}")
        .action((x, c) => c.copy(inputPath = x))
      opt[String]("modelPath")
        .text(s"modelPath , default: ${defaultParams.modelPath}}")
        .action((x, c) => c.copy(modelPath = x))
      opt[String]("outputPath")
        .text(s"outputPath, default: ${defaultParams.outputPath}}")
        .action((x, c) => c.copy(outputPath = x))
      opt[String]("warehousePath")
        .text(s"warehousePath , default: ${defaultParams.warehousePath}}")
        .action((x, c) => c.copy(warehousePath = x))
      opt[Int]("featureCount")
        .text(s"featureCount , default: ${defaultParams.featureCount}}")
        .action((x, c) => c.copy(featureCount = x))
      opt[Int]("intermediate1")
        .text(s"intermediate1 , default: ${defaultParams.intermediate1}}")
        .action((x, c) => c.copy(intermediate1 = x))
      opt[Int]("intermediate2")
        .text(s"intermediate2 , default: ${defaultParams.intermediate2}}")
        .action((x, c) => c.copy(intermediate2 = x))
      opt[Int]("classCount")
        .text(s"classCount , default: ${defaultParams.classCount}}")
        .action((x, c) => c.copy(classCount = x))
      opt[String]("mode")
        .text(s"mode 运行模式, default: ${defaultParams.mode}")
        .action((x, c) => c.copy(mode = x))
      note(
        """
          |For example,:MultilayerPerceptron
        """.stripMargin)
    }
    parser.parse(args, defaultParams).map { params => {
      println("参数值：" + params)
      trainMLP(params.inputPath,params.outputPath,params.modelPath,params.warehousePath,params.mode,
        params.featureCount, params.intermediate1,params.intermediate2, params.classCount)
    }
    } getOrElse {
      System.exit(1)
    }
  }
  /**
    *多层感知机神经网络分类--多值分类
    *
    * @param inputPath 输入数据格式,用Spark的data文件夹自带的sample_multiclass_classification_data.txt数据：
    *                  1 1:-0.222222 2:0.5 3:-0.762712 4:-0.833333
    *                  1 1:-0.555556 2:0.25 3:-0.864407 4:-0.916667
    *                  1 1:-0.722222 2:-0.166667 3:-0.864407 4:-0.833333
    *                  1 1:-0.722222 2:0.166667 3:-0.694915 4:-0.916667
    *                  0 1:0.166667 2:-0.416667 3:0.457627 4:0.5
    *                  1 1:-0.833333 3:-0.864407 4:-0.916667
    *                  2 1:-1.32455e-07 2:-0.166667 3:0.220339 4:0.0833333
    * @param outputPath
    * @param modelPath 模型持久化存储到文件
    * @param warehousePath 临时目录
    * @param mode  运行模式 local 或分布式
    * @param featureCount  数据特征个数
    * @param intermediate1  第一个隐藏层节点数
    * @param intermediate2  第二个隐藏层节点数
    * @param classCount  输出层，分类标签数
    */
  def trainMLP(inputPath:String,outputPath:String,modelPath:String,warehousePath:String,mode:String,
                   featureCount:Int, intermediate1:Int,intermediate2:Int,classCount:Int): Unit =
  {
    val startTime = System.currentTimeMillis()
    //创建Spark对象
    val spark = SparkSession
      .builder
      .config("spark.sql.warehouse.dir", warehousePath)
      .appName("MultilayerPerceptronClassifierJob")
      .master(mode)
      .getOrCreate()
    //读取训练数据，指定libsvm格式
    val data = spark.read.format("libsvm").load(inputPath)
    //训练数据，随机拆分数据80%做为训练集，20%作为测试集
    val splits = data.randomSplit(Array(0.8, 0.2), seed = 1234L)
    val (trainingData, testData) = (splits(0), splits(1))
    //神经网络图层设置，输入层4个节点，两个隐藏层intermediate1和intermediate2，输出层三个节点也就是三个分类
    val layers = Array[Int](featureCount, intermediate1,intermediate2, classCount)
    // 建立MLPC训练器并设置参数
    val trainer = new MultilayerPerceptronClassifier()
      .setLayers(layers)
      .setBlockSize(128)
      .setSeed(1234L)
      .setMaxIter(188)
    //数据和设置一切准备就绪，开始训练数据
    val model = trainer.fit(trainingData)
    val trainendTime = System.currentTimeMillis()
    //训练好的模型可以持久化到文件中，web服务或者其他预测项目里，直接加载这个模型文件到内存里面，进行直接预测，不用每次都训练
    model.save(modelPath)
    //加载刚才存储的这个模型文件到内存里面
    val loadModel = MultilayerPerceptronClassificationModel.load(modelPath)
    //基于加载的模型进行预测，这只是演示模型怎么持久化和加载的过程
    val predictResult = loadModel.transform(testData)
    //基于现在训练好的模型预测特征数据
    val result = model.transform(testData)
    //计算预测的准确度
    val predictionAndLabels = result.select("prediction", "label")
    //多值分类准确率计算工具
    val evaluator = new MulticlassClassificationEvaluator()
      .setMetricName("accuracy")
    val accuracy = evaluator.evaluate(predictionAndLabels)
    //把准确度打印出来
    println("Accuracy: " + accuracy)
    val predictEndTime = System.currentTimeMillis()
    val time1 = s"训练时间：${(trainendTime - startTime) / (1000 * 60.0)}分钟"
    val time2 = s"预测时间：${(predictEndTime - trainendTime) / (1000 * 60.0)}分钟"
    val precision = s"多值分类准确率：$accuracy"
    val out = ArrayBuffer[String]()
    out +=("MLP神经网络分类:", time1, time2,  precision)
    println(out)
    spark.stop()
    //查看下训练模型文件里的内容
    readParquetFile(modelPath + "data/*.parquet", 8000)
  }
}