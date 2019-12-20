package com.chongdianleme.mail
import com.hankcs.hanlp.HanLP
import org.apache.spark._
import scopt.OptionParser
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
/**
  * Created by 充电了么App 2019-10-09
  */
object ETLDeepQAJob {
  case class Params(
                     inputPath: String = "file:///D:\\充电了么\\QA", //输入目录
                     outputPath: String = "file:///D:\\充电了么\\Out", //输出目录
                     mode: String = "local"
                   )

  def main(args: Array[String]) {
    val defaultParams = Params()
    val parser = new OptionParser[Params]("NewsContentBaseJob") {
      head("NewsContentBaseJob: 解析参数.")
      opt[String]("inputPath")
        .text(s"inputPath 输入目录, default: ${defaultParams.inputPath}}")
        .action((x, c) => c.copy(inputPath = x))
      opt[String]("outputPath")
        .text(s"outputPath 输出目录, default: ${defaultParams.outputPath}")
        .action((x, c) => c.copy(outputPath = x))
      opt[String]("mode")
        .text(s"mode 运行模式, default: ${defaultParams.mode}")
        .action((x, c) => c.copy(mode = x))
      note(
        """
          |QA dataset:
        """.stripMargin)
    }
    parser.parse(args, defaultParams).map { params => {
      println("参数值：" + params)
      etlQA(params.inputPath,
        params.outputPath,
        params.mode
      )
    }
    }getOrElse {
      System.exit(1)
    }
    println("计算完成！")
  }

  def etlQA(inputPath: String,outputPath: String, mode: String) = {
    val sparkConf = new SparkConf().setAppName("充电了么App-对话机器人数据处理-演示Job")
    sparkConf.setMaster(mode)
    //SparkContext实例化
    val sc = new SparkContext(sparkConf)
    //加载中文对话数据文件
    val qaFileRDD = sc.textFile(inputPath)
    //初始值
    var i = 888660
    val linesList  = ListBuffer[String]()
    val conversationsList = ListBuffer[String]()
    val testList = ListBuffer[String]()
    val tempList = ListBuffer[String]()
    qaFileRDD.collect().foreach(line=>{
      if (line.startsWith("E")) {
        if (tempList.size>1)
        {
          val lineIDList = ArrayBuffer[String]()
          tempList.foreach(newLine=>{
            val lineID = newLine.split(" ")(0)
            lineIDList += "'"+lineID+ "'"
            linesList += newLine
          })
          conversationsList += "u0 +++$+++ u2 +++$+++ m0 +++$+++ ["+lineIDList.mkString(", ")+"]"
          tempList.clear()
        }
      }
      else {
        if (line.length>2)
        {
          //去除斜杆/字符，准备走中文分词
          val formatLine = line.replace("M","").replace(" ","").replace("/","")
          import scala.collection.JavaConversions._
          //使用HanLP开源分词工具
          val termList = HanLP.segment(formatLine);
          val list = ArrayBuffer[String]()
          for(term <- termList)
          {
            list += term.word
          }
          //中文分词后以空格分割连起来，训练的时候就像把中文分词当英文单词来拆分单词一样
          val segmentLine = list.mkString(" ")
          val newLine = if (tempList.size==0) {
            testList += segmentLine
            "L" + String.valueOf(i) + " +++$+++ u2 +++$+++ m0 +++$+++ z +++$+++ " + segmentLine
          }
          else  "L"+ String.valueOf(i) + " +++$+++ u2 +++$+++ m0 +++$+++ l +++$+++ " + segmentLine
          tempList += newLine
          i = i + 1
        }
      }
    })
    sc.parallelize(linesList, 1).saveAsTextFile(outputPath+"linesList")
    sc.parallelize(conversationsList, 1).saveAsTextFile(outputPath+"conversationsList")
    sc.parallelize(testList, 1).saveAsTextFile(outputPath+"测试List")
    sc.stop()
  }

}