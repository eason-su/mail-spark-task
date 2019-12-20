package com.chongdianleme.mail
import com.chongdianleme.mail.SVMJob.readParquetFile
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.mllib.feature.{Word2Vec, Word2VecModel}
import scopt.OptionParser
/**
  * Created by 充电了么App-陈敬雷
  * 官网：http://chongdianleme.com/
  * Word2vec，是一群用来产生词向量的相关模型。这些模型为浅而双层的神经网络，用来训练以重新建构语言学之词文本。网络以词表现，并且需猜测相邻位置的输入词，在word2vec中词袋模型假设下，词的顺序是不重要的。训练完成之后，word2vec模型可用来映射每个词到一个向量，可用来表示词对词之间的关系，该向量为神经网络之隐藏层。
  */
object Word2VecJob {
  case class Params(
                     inputPath: String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\PrefixSpan训练文本数据\\",
                     outputPath:String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\Word2VecOut\\",
                     modelPath:String="file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\Word2VecModel\\",
                     mode: String = "local",
                     warehousePath:String="file:///c:/tmp/spark-warehouse",
                     numPartitions:Int=16
                   )
  def main(args: Array[String]): Unit = {
    val defaultParams = Params()
    val parser = new OptionParser[Params]("Word2VecJob") {
      head("Word2VecJob: 解析参数.")
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
      opt[String]("warehousePath")
        .text(s"warehousePath , default: ${defaultParams.warehousePath}}")
        .action((x, c) => c.copy(warehousePath = x))
      opt[Int]("numPartitions")
        .text(s"numPartitions , default: ${defaultParams.numPartitions}}")
        .action((x, c) => c.copy(numPartitions = x))
      note(
        """
          |For example,Word2Vec
        """.stripMargin)
    }
    parser.parse(args, defaultParams).map { params => {
      println("参数值：" + params)
      word2vec(params.inputPath,
        params.outputPath,
        params.mode,
        params.modelPath,
        params.warehousePath,
        params.numPartitions
      )
    }
    } getOrElse {
      System.exit(1)
    }
  }
  /**
    * 训练模型
    * @param inputPath 输入数据格式每行记录可以是一篇文章，也可以是一个句子，分词后以空格分割
    * @param outputPath
    * @param mode
    * @param modelPath 持久化存储目录
    * @param warehousePath //临时目录
    * @param numPartitions //用多少Spark的Partition，用来提高并行程度，以便更快的训练完，但会消耗更多的服务器资源
    */
  def word2vec(inputPath : String,
               outputPath:String,
               mode:String,
               modelPath:String,
               warehousePath:String,
               numPartitions:Int): Unit =
  {
    val sparkConf = new SparkConf().setAppName("word2vec")
    sparkConf.set("spark.sql.warehouse.dir", warehousePath)
    sparkConf.setMaster(mode)
    //设置maxResultSize最大内存，因为word2vector源码中有collect操作，会占用较大内存
    sparkConf.set("spark.driver.maxResultSize","8g")
    val sc = new SparkContext(sparkConf)
    //训练格式可以是每篇文章分词后以空格分割，或者每行以句子分割
    val input = sc.textFile(inputPath).map(line => line.split(" ").toSeq)
    val word2vec = new Word2Vec()
    word2vec.setNumPartitions(numPartitions)
    word2vec.setLearningRate(0.1)//学习率
    word2vec.setMinCount(0)
    //训练模型
    val model = word2vec.fit(input)
    try {
        //有的词可能不在词典中，所有我们要加try catch处理这种异常
        val synonyms = model.findSynonyms("数据", 6)
        for((synonym, cosineSimilarity) <- synonyms) {
          println(s"相似词：$synonym  相似度：$cosineSimilarity")
        }
    } catch {
      case e: Exception =>  e.printStackTrace()
    }
    //训练好的模型可以持久化到文件中，web服务或者其他预测项目里，直接加载这个模型文件到内存里面，进行直接预测，不用每次都训练
    model.save(sc,modelPath)
    //加载刚才存储的这个模型文件到内存里面
    Word2VecModel.load(sc,modelPath)
    sc.stop()
    //查看下训练模型文件里的内容
    readParquetFile(modelPath + "data/*.parquet", 8000)
  }
}
