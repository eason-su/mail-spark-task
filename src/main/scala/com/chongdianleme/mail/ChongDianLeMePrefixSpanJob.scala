package com.chongdianleme.mail
import org.apache.spark._
import scopt.OptionParser
import scala.collection.mutable
/**
  * Created by 充电了么App-陈敬雷
  * 官网：http://chongdianleme.com/
  * 轻量级序列模式挖掘算法，保证频繁项集的后面的项是和前面的项在原始文章句子中是紧挨着的。
  */
  object ChongDianLeMePrefixSpanJob {
  case class Params(
                     inputPath: String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\PrefixSpan训练文本数据\\",
                     outputPath:String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\ChongDianLeMePrefixSpanOut\\",
                     minSupport: Int = 1,
                     patternLength: Int = 3,
                     mode: String = "local"
                   )
  def main(args: Array[String]) {
    val defaultParams = Params()
    val parser = new OptionParser[Params]("ChongDianLeMePrefixSpanJob") {
      head("ChongDianLeMePrefixSpanJob: 解析参数.")
      opt[String]("inputPath")
        .text(s"inputPath 输入目录, default: ${defaultParams.inputPath}}")
        .action((x, c) => c.copy(inputPath = x))
      opt[String]("outputPath")
        .text(s"outputPath 输入目录, default: ${defaultParams.outputPath}}")
        .action((x, c) => c.copy(outputPath = x))
      opt[Int]("minSupport")
        .text(s"minSupport, default: ${defaultParams.minSupport}}")
        .action((x, c) => c.copy(minSupport = x))
      opt[Int]("patternLength")
        .text(s"patternLength, default: ${defaultParams.patternLength}}")
        .action((x, c) => c.copy(patternLength = x))
      opt[String]("mode")
        .text(s"mode 运行模式, default: ${defaultParams.mode}")
        .action((x, c) => c.copy(mode = x))
      note(
        """
          |ChongDianLeMePrefixSpanJob  dataset:
        """.stripMargin)
    }
    parser.parse(args, defaultParams).map { params => {
      println("参数值：" + params)
      val sparkConf = new SparkConf().setAppName("ChongDianLeMePrefixSpanJob")
      sparkConf.setMaster(params.mode)
      val sc = new SparkContext(sparkConf)
      //加载训练数据
      val inputData = sc.textFile(params.inputPath)
      //处理数据，拼接项集，把多个项集放到一个List中，再用flatMap打平了。
      val trainData = inputData.flatMap {
        sentence => {
          val words = sentence.split(" ")
          val items = mutable.ArrayBuilder.make[String]
          for (i <- 0 until words.length if ((i + params.patternLength - 1) < words.length)) {
            //println("i:"+i + "word:"+ words(i))
            val end = i + params.patternLength - 1
            val item = mutable.ArrayBuilder.make[String]
            for (j <- i to end) {
              item += words(j)
            }
            items += item.result().mkString(":")
          }
          val result = items.result()
          result
        }
      }.map(item => (item, 1L))
        .reduceByKey(_ + _)//项集计数统计
        .filter(_._2 >= params.minSupport)//筛选大于最低支持度的频繁项集
        .map {
          case (item, count) => {
            //输出频繁项集和对应出现频率
            item + "\001" + count
          }
        }
        .saveAsTextFile(params.outputPath)
      sc.stop()
    }
    } getOrElse {
      System.exit(1)
    }
  }
}