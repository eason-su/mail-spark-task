package com.chongdianleme.job;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
public class ChongDianLeMeDemo {

    public static void main(String[] args) {
        //System.out.println("Hello, world!--www.chongdianleme.com充电了么App - 专注上班族职业技能提升的在线教育平台");
        // 输出Hello word，无返回值
        //helloWord();
        //字符串类型的参数，无返回值
        //helloWord("Hello, world!--充电了么App - 专注上班族职业技能提升的在线教育平台");
        //有入口参数，有返回值,函数重载
        //String s = helloWord("Hello, world! ===函数重载====","充电了么App - 专注上班族职业技能提升的在线教育平台");
        //System.out.println("下面打印字符串相加拼接的结果");
        //System.out.println(s);
        //String字符串变量，字符串可多次赋值更新
        //stringDemo();

        // i++   i--  ++i  --i 演示
        //iDemo();

        //functionDemo 函数说明
        //functionDemo("",1,1,1);

        //ctrl+alt+左箭头，返回上次的代码的鼠标位置

        // if else 分支，如果 否则
        //ifDemo(true);

        //JDBC方式连接Mysql数据库，数据库操作，返回布尔型变量
        //isOnSale("kc61800001");

        //while循环 例子
        whileDemo();

        //for循环例子
        //forDemo();

        //Try catch finally异常处理  例子演示
        //tryCatchDemo();

        //swtich  case 多分支演示例子演示
        //switchCaseDemo('C');
    }

    /**
     * 输出Hello word，无返回值
     */
    public static void helloWord() {
        System.out.println("Hello, world!--充电了么App - 专注上班族职业技能提升的在线教育平台");
    }

    /**
     * 带入参str
     * @param str 字符串类型的参数，无返回值
     */
    public static void helloWord(String str) {
        System.out.println(str);
    }

    /**
     * 有入口参数，有返回值
     * @param str
     * @param chongZai
     * @return
     */
    public static String helloWord(String str,String chongZai) {
        System.out.println(str);
        return chongZai + "  分隔符  "+ str;
    }
    /**
     * String字符串变量
     */
    public static void stringDemo() {
        String str = "Hello, world!--充电了么App";
        System.out.println(str);
        str = "888";//可以多次赋值给它，更新
        System.out.println(str);
    }

    /**
     * i++   i--  ++i  --i 演示
     */
    public static void iDemo() {
        int i = 0;
        System.out.println(i);
        i++;//i= i + 1;
        System.out.println(i);
        System.out.println(i++);
        System.out.println(++i);
        System.out.println(--i);
        System.out.println(i--);
    }

    /**
     * 函数演示  static 静态，返回值int ，public private protected等，public 和 private 最常用
     * @return
     */
    protected static int functionDemo(String a,int b,double c,float d) {
        int i = 0;
        System.out.println(i);
        i++;
        return i;
    }

    /**
     * if else 分支，如果 否则
     * @param isSale
     */
    public static void ifDemo(Boolean isSale) {
        //Boolean test = Boolean.parseBoolean("aaaaa");
        //System.out.println(test);
        if (isSale) {
            System.out.println("充电了么App-课程在售");
        }
         else {
            System.out.println("充电了么App-课程下架");
        }

    }

    /**
     * JDBC方式连接Mysql数据库，数据库操作，返回布尔型变量
     * @param kcId
     * @return
     */
    public static boolean isOnSale(String kcId) {
        ClassDemo classDemo = new ClassDemo();
        Boolean isSale = ClassDemo.isOnSale(kcId);
        classDemo.setKcId(kcId);
        classDemo.setKcName("大数据开发");
        classDemo.setPrice(888);
        classDemo.setSale(true);
        System.out.println("来自mysql数据库的值，商品在售："+isSale);
        return isSale;
    }

    /**
     * while循环 例子
     */
    public static void whileDemo() {
        int i = 1;
        int sum = 0;
        //计算1+2+3+4....+100的累加的和，5050.
        while(i <= 100)
        {
            sum = sum + i;
            i++;
            //1到80的和
            if (i>80)
                break;
            else
                continue;
        }
        System.out.println("和："+sum);
    }

    /**
     * for循环例子
     */
    public static void forDemo()
    {
        //for循环 普通
        List<String> list = new ArrayList<>();
        list.add("充电了么App");
        list.add("充电了么App是专注上班族职业技能提升的在线教育平台");
        list.add("充电了么app提高工作效率，为公司带来经济效益");
        for(int i=0;i<list.size();i++)
        {
            System.out.println("for i = ："+i+"   "+list.get(i));
        }
        //ctrl + shift + /
        for(String str : list)
        {
            System.out.println("for str："+str);
        }
        for(Iterator<String> it = list.iterator();it.hasNext();)
        {
            System.out.println("for Iterator："+it.next());
        }
    }

    /**
     * Try catch finally异常处理  例子演示
     */
    public static void tryCatchDemo()
    {
        int n = 1;
        try {

            System.out.println("try");

            n = Integer.parseInt("充电了么App - 专注上班族职业技能提升的在线教育平台");

        } catch (Exception ex) {
             ex.printStackTrace();
        } finally {
            n=0;
            System.out.println("finally");
        }
        System.out.println("打印n的值："+n);
    }

    /**
     * 多分支演示例子演示
     * @param grade
     */
    public static void switchCaseDemo(char grade)
    {
       // char grade = 'C';
        switch(grade)
        {
            case 'A' :
                System.out.println("优秀");
                break;
            case 'B' :
            case 'C' :
                System.out.println("良好");
                break;
            case 'D' :
                System.out.println("及格");
                break;
            case 'F' :
                System.out.println("你需要再努力努力");
                break;
            default :
                System.out.println("未知等级");
        }
        System.out.println("你的等级是 " + grade);
    }

}
