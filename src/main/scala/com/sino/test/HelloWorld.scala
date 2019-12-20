package com.sino.test

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object HelloWorld {
  def main(args: Array[String]): Unit = {
    println("Hello, world!")



    val sparkConf = new SparkConf().setAppName("HbaseJob")
    sparkConf.setMaster("local")
    val sc = new SparkContext(sparkConf)
    //从内存里构造 RDD
    val rdd01 = sc.makeRDD(List(1,2,3,4,5,6))

    val m_rdd01 = rdd01.map(x=>x+1)
    println("-------------------")

    //过文件系统构造 RDD
    val rdd002:RDD[String] = sc.textFile("file:///E:\\work\\code\\study\\ai-workspace\\chongdianleme-spark-task\\file\\send-mail\\input",1)




    val it = rdd002.collect().iterator

    while (it.hasNext){
      println(it.next())
    }
  }
}