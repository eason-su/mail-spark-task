package com.chongdianleme.mail
import com.chongdianleme.mail.SVMJob.readParquetFile
import org.apache.spark._
import org.apache.spark.mllib.fpm.PrefixSpan
import scopt.OptionParser
/**
  * Created by 充电了么App-陈敬雷
  * 官网：http://chongdianleme.com/
  * PrefixSpan 序列模式挖掘算法
  */
object PrefixSpanJob {
  case class Params(
                     inputPath: String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\PrefixSpan训练文本数据\\",
                     outputPath:String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\PrefixSpanOut\\",
                     modelPath: String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\PrefixSpanModel\\",
                     minSupport:Double = 0.01,//最小支持度,支持度(support)是D中事务同时包含X、Y的百分比,[[c], [d]], 5 ,总记录数9,5/9=0.55
                     maxPatternLength:Int = 3,//最大序列长度
                     mode: String = "local"
                   )
  def main(args: Array[String]) {
    val defaultParams = Params()
    val parser = new OptionParser[Params]("PrefixSpanJob") {
      head("PrefixSpanJob: 解析参数.")
      opt[String]("inputPath")
        .text(s"inputPath 输入目录, default: ${defaultParams.inputPath}}")
        .action((x, c) => c.copy(inputPath = x))
      opt[String]("outputPath")
        .text(s"outputPath 输入目录, default: ${defaultParams.outputPath}}")
        .action((x, c) => c.copy(outputPath = x))
      opt[String]("modelPath")
        .text(s"modelPath 模型输出, default: ${defaultParams.modelPath}}")
        .action((x, c) => c.copy(modelPath = x))
      opt[Double]("minSupport")
        .text(s"minSupport, default: ${defaultParams.minSupport}}")
        .action((x, c) => c.copy(minSupport = x))
      opt[Int]("maxPatternLength")
        .text(s"maxPatternLength, default: ${defaultParams.maxPatternLength}}")
        .action((x, c) => c.copy(maxPatternLength = x))
      opt[String]("mode")
        .text(s"mode 运行模式, default: ${defaultParams.mode}")
        .action((x, c) => c.copy(mode = x))
      note(
        """
          |PrefixSpan dataset:
        """.stripMargin)
    }
    parser.parse(args, defaultParams).map { params => {
      println("打印参数：" + params)
      val sparkConf = new SparkConf().setAppName("PrefixSpanJob")
      sparkConf.set("spark.sql.warehouse.dir", "file:///C:/warehouse/temp/")
      sparkConf.setMaster(params.mode)
      val sc = new SparkContext(sparkConf)
      val inputData = sc.textFile(params.inputPath)
      //把待训练的数据处理成PrefixSpan需要的格式
      val trainData = inputData.map{
        sentence =>{
          //对每个句子的分词分成一个数组
          val words = sentence.split(" ")
          //最终要返回的是RDD[Array[Array[String]]]格式数据
          val result = for (word <- words) yield Array(word)
          result
        }
      }.cache()
      val prefixSpan = new PrefixSpan()
        .setMinSupport(params.minSupport)//支持度(support)是D中事务同时包含X、Y的百分比,[[c], [d]], 5 ,总记录数9,5/9=0.55
        .setMaxPatternLength(params.maxPatternLength)//最大序列长度
        .setMaxLocalProjDBSize(32000000L)
      //训练模型
      val model = prefixSpan.run(trainData)
      //模型持久化
      model.save(sc,params.modelPath)
      //遍历有序的频繁项集自己存储到Hadoop的分布式文件系统里
      model.freqSequences.map{
        freqSequence =>
          val key = freqSequence.sequence.map(_.mkString("",":","")).mkString("",":","")
          val patternLength = key.split(":").length
          val support = freqSequence.freq
          //输出项集、序列长度、支持度三列以\001分割
          key + "\001"+ patternLength +"\001"+ support
      }.saveAsTextFile(params.outputPath)
      sc.stop()
      //查看下训练模型文件里的内容
      readParquetFile(params.modelPath + "data/*.parquet", 8000)
    }
    } getOrElse {
      System.exit(1)
    }
  }
}