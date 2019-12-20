package com.chongdianleme.mail

import java.util
import java.util.{ArrayList, Iterator, List}

import com.chongdianleme.job.ClassDemo

object ScalaDemo {
  def main(args: Array[String]): Unit = { //System.out.println("Hello, world!--www.chongdianleme.com充电了么App - 专注上班族职业技能提升的在线教育平台");
    // 输出Hello word，无返回值
    //helloWord()
    //字符串类型的参数，无返回值
    //helloWord("Hello, world!--充电了么App - 专注上班族职业技能提升的在线教育平台")
    //有入口参数，有返回值,函数重载
    //var s = helloWord("Hello, world! ===函数重载====", "充电了么App - 专注上班族职业技能提升的在线教育平台");
    //System.out.println("下面打印字符串相加拼接的结果");
    //System.out.println(s);
    //s = "新值测试"
    //println(s)
    //String字符串变量，字符串可多次赋值更新
    //stringDemo();
    // i++   i--  ++i  --i 演示
    //iDemo();
    //functionDemo 函数说明
    //functionDemo("",1,1,1);
    //ctrl+alt+左箭头，返回上次的代码的鼠标位置
    // if else 分支，如果 否则
    //val sale =ifDemo(true);
    //println(sale)
    //JDBC方式连接Mysql数据库，数据库操作，返回布尔型变量
    val classDemo = isOnSale("kc61800001")
    println(classDemo.getKcId)
    println(classDemo.getKcName)
    println(classDemo.getPrice)
    println(classDemo.isSale)
    //while循环 例子
    //whileDemo();
    //for循环例子
    //forDemo();
    //Try catch finally异常处理  例子演示
    tryCatchDemo();
    //swtich  case 多分支演示例子演示
    //matchCaseDemo('C');
  }

  /**
    * 输出Hello word，无返回值
    */
  def helloWord(): Unit = {
    println("Hello, world!--充电了么App - 专注上班族职业技能提升的在线教育平台")
  }

  /**
    * 带入参str
    *
    * @param str 字符串类型的参数，无返回值
    */
  def helloWord(str: String): Unit = {
    System.out.println(str)
  }

  /**
    * 有入口参数，有返回值
    *
    * @param str
    * @param chongZai
    * @return
    */
  def helloWord(str: String, chongZai: String): String = {
    System.out.println(str)
    chongZai + "  分隔符  " + str
  }

  /**
    * String字符串变量
    */
  def stringDemo(): Unit = {
    var str = "Hello, world!--充电了么App"
    System.out.println(str)
    str = "888" //可以多次赋值给它，更新

    println(str)
  }

  /**
    * i++   i--  ++i  --i 演示
    */
  def iDemo(): Unit = {
    var i = 0
    System.out.println(i)
    i += 1 //i= i + 1;

    System.out.println(i)
    System.out.println({
      i += 1;
      i - 1
    })
    System.out.println({
      i += 1;
      i
    })
    System.out.println({
      i -= 1;
      i
    })
    System.out.println({
      i -= 1;
      i + 1
    })
  }

  /**
    * 函数演示  static 静态，返回值int ，public private protected等，public 和 private 最常用
    *
    * @return
    */
  def functionDemo(a: String, b: Int, c: Double, d: Float): Int = {
    var i = 0
    System.out.println(i)
    i += 1// i= i + 1
    i
  }

  /**
    * if else 分支，如果 否则
    *
    * @param isSale
    */
  def ifDemo(isSale: Boolean): String = { //Boolean test = Boolean.parseBoolean("aaaaa");
    //System.out.println(test);
    if (isSale) System.out.println("充电了么App-课程在售")
    else System.out.println("充电了么App-课程下架")

    val sale = if (isSale) "aaaaa" else "bbbbbb"
    sale
  }

  /**
    * JDBC方式连接Mysql数据库，数据库操作，返回布尔型变量
    *
    * @param kcId
    * @return
    */
  def isOnSale(kcId: String): ClassDemo = {
    val classDemo = new ClassDemo
    val isSale = ClassDemo.isOnSale(kcId)
    classDemo.setKcId(kcId)
    classDemo.setKcName("大数据开发")
    classDemo.setPrice(888)
    classDemo.setSale(isSale)
    System.out.println("来自mysql数据库的值，商品在售：" + isSale)
    classDemo
  }

  /**
    * while循环 例子
    */
  def whileDemo(): Unit = {
    var i = 1
    var sum = 0
    //计算1+2+3+4....+100的累加的和，5050.
    //死循环：一直循环不退出。直到断电了。
    while (i <= 100) {
      if (i<=80) {
        sum = sum + i
      }
      i += 1
    }
    //等while循环后，再打印和
    System.out.println("和：" + sum)
  }
    /**
      * for循环例子
      */
    def forDemo(): Unit = { //for循环 普通
      val list = new util.ArrayList[String]
      list.add("充电了么App")
      list.add("充电了么App是专注上班族职业技能提升的在线教育平台")
      list.add("充电了么app提高工作效率，为公司带来经济效益")
      var i = 0
      while (i < list.size) {
        System.out.println("for i = ：" + i + "   " + list.get(i))
        i = i + 1;

      }
      //ctrl + shift + /
      import scala.collection.JavaConversions._
      for (str <- list) {
        System.out.println("for str：" + str)
      }
      val it = list.iterator
      while ( {
        it.hasNext
      }) System.out.println("for Iterator：" + it.next)
    }

  /**
    * Try catch finally异常处理  例子演示
    */
  def tryCatchDemo(): Unit = {
      var n = 1
      try {
        System.out.println("try")
        n = "充电了么App - 专注上班族职业技能提升的在线教育平台".toInt
      } catch {
        case ex: Exception =>
          ex.printStackTrace()
      } finally {
        n = 0
        System.out.println("finally")
      }
      System.out.println("打印n的值：" + n)
    }

    /**
      * 多分支演示例子演示
      *
      * @param grade
      */
    def matchCaseDemo(grade: Char): Unit = { // char grade = 'C';
      grade match {
        case 'A' =>
          System.out.println("优秀")
        //break //todo: break is not supported
        case 'B' =>
        case 'C' =>
          System.out.println("良好")
        //break //todo: break is not supported
        case 'D' =>
          System.out.println("及格")
        //break //todo: break is not supported
        case 'F' =>
          System.out.println("你需要再努力努力")
        //break //todo: break is not supported
        case _ =>
          System.out.println("未知等级")
      }
      System.out.println("你的等级是 " + grade)
    }

}

