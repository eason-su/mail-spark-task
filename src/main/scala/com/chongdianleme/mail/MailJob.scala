package com.chongdianleme.mail

import com.chongdianleme.job.SendTuiGuang
import org.apache.spark._
import scopt.OptionParser

import scala.collection.mutable.{ArrayBuffer, ListBuffer}

/**
  * Created by 充电了么App - 陈敬雷
  * 充电了么App - Spark分布式大规模发送邮件项目操作演示
  * 充电了么App - 专业上班族职业技能提升的在线教育平台
  */
object MailJob {
  case class Params(
                     inputPath: String = "file:///E:\\work\\code\\study\\ai-workspace\\send-mail\\input",
                     fromPath: String =  "file:///E:\\work\\code\\study\\ai-workspace\\send-mail\\frommail",
                     table: String = "mail",//发送邮件后，记录日志到哪个mysql数据表
                     batch:String = "win",
                     minPartitions: Int = 1,
                     mode: String = "local"
                   )

  def main(args: Array[String]) {
    val defaultParams = Params()
    val parser = new OptionParser[Params]("MailJob") {
      head("mailJob: 解析参数.")
      opt[String]("inputPath")
        .text(s"inputPath 输入目录, default: ${defaultParams.inputPath}}")
        .action((x, c) => c.copy(inputPath = x))
      opt[String]("fromPath")
        .text(s"fromPath 发送人列表, default: ${defaultParams.fromPath}")
        .action((x, c) => c.copy(fromPath = x))
      opt[Int]("minPartitions")
        .text(s"minPartitions , default: ${defaultParams.minPartitions}")
        .action((x, c) => c.copy(minPartitions = x))
      opt[String]("table")
        .text(s"table table, default: ${defaultParams.table}")
        .action((x, c) => c.copy(table = x))
      opt[String]("batch")
        .text(s"batch batch, default: ${defaultParams.batch}")
        .action((x, c) => c.copy(batch = x))
      opt[String]("mode")
        .text(s"mode 运行模式, default: ${defaultParams.mode}")
        .action((x, c) => c.copy(mode = x))
      note(
        """
         |For example, the following command runs this app on a mailjob dataset:
         |1
         |2
         |3
         |4
         |5
         |6
         |6
       """.stripMargin)
   }
    parser.parse(args, defaultParams).map { params => {
      println("参数值：" + params)
      readFilePath(params.inputPath,params.fromPath,params.table, params.minPartitions,params.batch, params.mode)
   }
   }getOrElse {
     System.exit(1)
   }
   println("充电了么App - 分布式大规模发邮件项目 - 计算完成！")
 }

  def readFilePath(inputPath: String,fromPath: String,table:String,minPartitions:Int,batch:String,mode:String) = {
    val sparkConf = new SparkConf().setAppName("mailJob")
    if (mode.equals("local"))
        sparkConf.setMaster("local")
    val sc = new SparkContext(sparkConf)
    val fromList = sc.textFile(fromPath).distinct().collect().toList
    val data = sc.textFile(inputPath,minPartitions)
    data.mapPartitions(fenpi(_,fromList,table,batch))
        .count()
    sc.stop()
  }
  def fenpi(lines: Iterator[String],fromList:List[String],table:String,batch:String) = {
    val lineList = ListBuffer[String]()
    import scala.collection.JavaConversions._
    for (line <- lines) {
      val random = new java.util.Random();
      val n = random.nextInt(fromList.size());
      val fromMail = fromList.get(n);
      val arr = fromMail.split("\001")
      val host = arr(0)
      val port = arr(1)
      val from = arr(2)
      val fromShouquanma = arr(3)
      val br = SendTuiGuang.sendLine(line, host, port, from, fromShouquanma, table, batch)
      if (br)
        lineList += line
    }
    lineList.toIterator
  }


}