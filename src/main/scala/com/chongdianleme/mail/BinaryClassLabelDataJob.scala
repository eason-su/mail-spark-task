package com.chongdianleme.mail
import org.apache.spark._
import scopt.OptionParser

/**
  * Created by chongdianleme 陈敬雷
  * 官网：http://chongdianleme.com/
  * SGD逻辑回归二值分类训练数据的准备
  */
object BinaryClassLabelDataJob {

  case class Params(
                     inputPath: String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\sample_multiclass_classification_data.txt",
                     outputPath: String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\二值分类训练数据\\",
                     mode: String = "local"
                   )

  def main(args: Array[String]) {
    val defaultParams = Params()
    val parser = new OptionParser[Params]("etlJob") {
      head("etlJob: 解析参数.")
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
      println("参数值：" + params)
      println("trainLogicRegressionwithLBFGS!")
      etl(params.inputPath,
        params.outputPath,
        params.mode
      )
    }
    } getOrElse {
      System.exit(1)
    }
  }

  /**
    * 处理分类训练数据，把sample_multiclass_classification_data.txt
    *里面的数据转换成这种格式的
    *
    * @param input sample_multiclass_classification_data.txt数据，第一列是标签值，代表这条数据是哪个分类的数据，后面的列是特征数据，冒号前面代表的是第几个特征，冒号后面代表的是特征值：
    *              1 1:-0.222222 2:0.5 3:-0.762712 4:-0.833333
    *              1 1:-0.555556 2:0.25 3:-0.864407 4:-0.916667
    *              1 1:-0.722222 2:-0.166667 3:-0.864407 4:-0.833333
    *              1 1:-0.722222 2:0.166667 3:-0.694915 4:-0.916667
    *              0 1:0.166667 2:-0.416667 3:0.457627 4:0.5
    *              1 1:-0.833333 3:-0.864407 4:-0.916667
    *              2 1:-1.32455e-07 2:-0.166667 3:0.220339 4:0.0833333
    *              2 1:-1.32455e-07 2:-0.333333 3:0.0169491 4:-4.03573e-08
    * @param outputPath 处理转换后的格式如下：
    *  第一列是类的标签值，逗号后面的都是特征值，多个特征以空格分割：
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
    */
  def etl(input: String,
          outputPath: String,
          mode: String): Unit = {
    val startTime = System.currentTimeMillis()
    val sparkConf = new SparkConf().setAppName("etlJob")
    sparkConf.setMaster(mode)
    //首先SparkContext实例化
    val sc = new SparkContext(sparkConf)
    //加载多分类的demo数据
    sc.textFile(input)
      .filter(line=>{
        val arr = line.split(" ")
        //只要固定5个特征数据列，如果少了一个或多一个特征训练的时候会报错，用MLUtils.loadLabeledPoints(sc,input)方式加载数据的情况下
        //因为只需要二值分类，我们只提取类标签为0和1的样本数据
        arr.length==5&&(arr(0).equals("0")||arr(0).equals("1"))
      })
      .map(line => {
      val arr = line.split(" ")
      val sb = new StringBuilder
      var i = 0;
      arr.foreach(feature => {
        if (i == 0)
          sb.append(feature + ",")
        else {
          var fArr = feature.split(":")
          sb.append(fArr(1) + " ")
        }
        i = i +1
      })
      //把处理后的数据拼接成一行返回
      sb.toString().trim
    })
      //处理后的数据存成一个文件
      .saveAsTextFile(outputPath)
    sc.stop()
  }
}