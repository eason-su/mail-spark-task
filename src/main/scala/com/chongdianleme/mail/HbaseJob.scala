package com.chongdianleme.mail
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{Result, Get, HConnectionManager}
import org.apache.hadoop.hbase.util.{ArrayUtils, Bytes}
import org.apache.spark._
import scopt.OptionParser
import scala.collection.mutable.ListBuffer
/**
 * Created by 充电了么App - 陈敬雷
  * Spark分布式操作Hbase实战
  * 网站：www.chongdianleme.com
  * 充电了么App - 专业上班族职业技能提升的在线教育平台
 */
object HbaseJob {
  case class Params(
                     //输入目录的数据就是课程ID,每行记录就一个课程ID，后面根据课程ID作为rowKey从Hbase里查询数据
                     inputPath: String = "file:///D:\\chongdianleme\\Hbase项目\\input",
                     outputPath: String =   "file:///D:\\chongdianleme\\Hbase项目\\output",
                     table: String = "chongdianleme_kc",
                     minPartitions: Int = 1,
                     mode: String = "local"
                   )

  def main(args: Array[String]) {
    val defaultParams = Params()
    val parser = new OptionParser[Params]("HbaseJob") {
      head("HbaseJob: 解析参数.")
      opt[String]("inputPath")
        .text(s"inputPath 输入目录, default: ${defaultParams.inputPath}}")
        .action((x, c) => c.copy(inputPath = x))
      opt[String]("outputPath")
        .text(s"outputPath 输出目录, default: ${defaultParams.outputPath}")
        .action((x, c) => c.copy(outputPath = x))
      opt[Int]("minPartitions")
        .text(s"minPartitions , default: ${defaultParams.minPartitions}")
        .action((x, c) => c.copy(minPartitions = x))
      opt[String]("table")
        .text(s"table table, default: ${defaultParams.table}")
        .action((x, c) => c.copy(table = x))
      opt[String]("mode")
        .text(s"mode 运行模式, default: ${defaultParams.mode}")
        .action((x, c) => c.copy(mode = x))
      note(
        """
         |For example, the following command runs this app on a HbaseJob dataset:
       """.stripMargin)
   }
    parser.parse(args, defaultParams).map { params => {
      println("参数值：" + params)
      readFilePath(params.inputPath,params.outputPath,params.table, params.minPartitions, params.mode)
   }
   }getOrElse {
     System.exit(1)
   }
   println("充电了么App - Spark分布式批量操作Hbase实战 -- 计算完成！")
 }
  def readFilePath(inputPath: String,outputPath:String,table:String,minPartitions:Int,mode:String) = {
    val sparkConf = new SparkConf().setAppName("HbaseJob")
    sparkConf.setMaster(mode)
    val sc = new SparkContext(sparkConf)
    //加载数据文件
    val data = sc.textFile(inputPath,minPartitions)

    data.mapPartitions(batch(_,table)).saveAsTextFile(outputPath)
    sc.stop()
  }
  def batch(keys: Iterator[String],hbaseTable:String) = {
    val lineList = ListBuffer[String]()
    import scala.collection.JavaConversions._
    val conf = HBaseConfiguration.create()
    //每批数据创建一个Hbase连接，多条数据操作共享这个连接
    val connection = HConnectionManager.createConnection(conf)
    //获取表
    val table = connection.getTable(hbaseTable)
    keys.foreach(rowKey=>{
      try {
        //根据rowKey主键也就是课程ID查询数据
        val get = new Get(rowKey.getBytes())
        //指定需要获取的列蔟和列
        get.addColumn("kcname".getBytes(), "name".getBytes())
        get.addColumn("saleinfo".getBytes(), "price".getBytes())
        get.addColumn("saleinfo".getBytes(), "issale".getBytes())
        val result = table.get(get)
        var nameRS= result.getValue("kcname".getBytes(),"name".getBytes())
        var kcName = "";
        if(nameRS != null&&nameRS.length > 0){
          kcName = new String(nameRS);
        }
        val priceRS = result.getValue("saleinfo".getBytes, "price".getBytes)
        var price = ""
        if (priceRS != null && priceRS.length > 0)
          price = new String(priceRS)
        val issaleRS = result.getValue("saleinfo".getBytes, "issale".getBytes)
        var issale = ""
        if (issaleRS != null && issaleRS.length > 0)
          issale = new String(issaleRS)
        lineList += rowKey+"\001"+ kcName + "\001"+ price + "\001"+issale
      } catch {
        case e: Exception =>  e.printStackTrace()
      }
    })
    //每批数据操作完毕，别忘了关闭表和数据库连接
    table.close()
    connection.close()
    lineList.toIterator
  }
}