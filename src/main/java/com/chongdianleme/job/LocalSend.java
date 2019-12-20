package com.chongdianleme.job;

import com.alibaba.fastjson.JSONArray;
import org.apache.commons.lang3.StringUtils;

import javax.mail.*;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by chenjinglei on 2018/10/28.
 */
public class LocalSend {
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
       /* String host = "smtp.qq.com";
        String port = "587";//或25
        String from = "2097125931@qq.com";
        String fromShouquanma = "sgkmvyifjxzmeefb";*/
        String host = "edm.chongdianleme.com";// String host = "mail.chongdianleme.com";
        String port = "25";//或25
        String from = "student@edm.chongdianleme.com";
        String fromShouquanma = "cdlm@123456";

        MimeMessage message = getMessage(host,port,from,fromShouquanma);
        List<String> lines = new ArrayList<>();
        //String toUser = "604586220@qq.com";
        //"邮箱\006听课s(多行用\002,fields\001)\003阅读s(多行用\002,fields\001)"
        String qqline = "604586220@qq.com\006//v.youku.com/v_show/id_XMzQ5MTQ4MTM3Mg==.html\u0001你好student665\u00012018-3-26\u0001宏皓2017\u0001鸟批量企业融资渠道与实操\u0001//vthumb.ykimg.com/054204085AB8633C0000016072030DD1\u000120\u000122\u0001\u0001aa\002//v.youku.com/v_show/id_XMzYwOTExMDUwOA==.html\u0001软件企业会计考点汇总_软件企业会计做账难点_软件企业会计实操指点\u00012018-5-16\u0001前进的未来\u0001企业融资渠道与实操\u0001//vthumb.ykimg.com/054204085AFBB0F600000126DF0E5E2D\u000149\u0001878\u0001\u0001aa\003//v.youku.com/v_show/id_XMjgyNDY4NDExMg==.html\u0001成本会计实操培训_外贸会计实操培训_企业会计实操视频\u0001会计学堂";
        String wyline = "cjldl7119@126.com\006//v.youku.com/v_show/id_XMzQ5MTQ4MTM3Mg==.html\u0001企业融资私募股权基金产业基金经济金融专家宏皓给淮安市政府讲授转型升级之路66\u00012018-3-26\u0001宏皓2017\u0001企业融资渠道与实操\u0001//vthumb.ykimg.com/054204085AB8633C0000016072030DD1\u000120\u000122\u0001\u0001aa\002//v.youku.com/v_show/id_XMzYwOTExMDUwOA==.html\u0001软件企业会计考点汇总_软件企业会计做账难点_软件企业会计实操指点\u00012018-5-16\u0001前进的未来\u0001企业融资渠道与实操\u0001//vthumb.ykimg.com/054204085AFBB0F600000126DF0E5E2D\u000149\u0001878\u0001\u0001aa\003//v.youku.com/v_show/id_XMjgyNDY4NDExMg==.html\u0001成本会计实操培训_外贸会计实操培训_企业会计实操视频\u0001会计学堂";
        String myline = "teacher@mail.chongdianleme.com\006//v.youku.com/v_show/id_XMzQ5MTQ4MTM3Mg==.html\u0001企业融资私募股权基金产业基金经济金融专家宏皓给淮安市政府讲授转型升级之路66\u00012018-3-26\u0001宏皓2017\u0001企业融资渠道与实操\u0001//vthumb.ykimg.com/054204085AB8633C0000016072030DD1\u000120\u000122\u0001\u0001aa\002//v.youku.com/v_show/id_XMzYwOTExMDUwOA==.html\u0001软件企业会计考点汇总_软件企业会计做账难点_软件企业会计实操指点\u00012018-5-16\u0001前进的未来\u0001企业融资渠道与实操\u0001//vthumb.ykimg.com/054204085AFBB0F600000126DF0E5E2D\u000149\u0001878\u0001\u0001aa\003//v.youku.com/v_show/id_XMjgyNDY4NDExMg==.html\u0001成本会计实操培训_外贸会计实操培训_企业会计实操视频\u0001会计学堂";
        lines.add(qqline);
        lines.add(wyline);
        lines.add(myline);
        for(String line : lines) {
            sendLine(line,host,port,from,fromShouquanma,"mail","win");
        }
    }

    /*public static void main(String[] args) {
       *//* String host = "smtp.qq.com";
        String port = "587";//或25
        String from = "2097125931@qq.com";
        String fromShouquanma = "sgkmvyifjxzmeefb";*//*
        String host = "edm.chongdianleme.com";// String host = "mail.chongdianleme.com";
        String port = "25";//或25
        String from = "student@edm.chongdianleme.com";
        String fromShouquanma = "cdlm@123456";

        MimeMessage message = getMessage(host,port,from,fromShouquanma);
        List<String> lines = new ArrayList<>();
        //String toUser = "604586220@qq.com";
        //"邮箱\006听课s(多行用\002,fields\001)\003阅读s(多行用\002,fields\001)"
        String qqline = "604586220@qq.com\006//v.youku.com/v_show/id_XMzQ5MTQ4MTM3Mg==.html\u0001你好student665\u00012018-3-26\u0001宏皓2017\u0001鸟批量企业融资渠道与实操\u0001//vthumb.ykimg.com/054204085AB8633C0000016072030DD1\u000120\u000122\u0001\u0001aa\002//v.youku.com/v_show/id_XMzYwOTExMDUwOA==.html\u0001软件企业会计考点汇总_软件企业会计做账难点_软件企业会计实操指点\u00012018-5-16\u0001前进的未来\u0001企业融资渠道与实操\u0001//vthumb.ykimg.com/054204085AFBB0F600000126DF0E5E2D\u000149\u0001878\u0001\u0001aa\003//v.youku.com/v_show/id_XMjgyNDY4NDExMg==.html\u0001成本会计实操培训_外贸会计实操培训_企业会计实操视频\u0001会计学堂";
        String wyline = "cjldl7119@126.com\006//v.youku.com/v_show/id_XMzQ5MTQ4MTM3Mg==.html\u0001企业融资私募股权基金产业基金经济金融专家宏皓给淮安市政府讲授转型升级之路66\u00012018-3-26\u0001宏皓2017\u0001企业融资渠道与实操\u0001//vthumb.ykimg.com/054204085AB8633C0000016072030DD1\u000120\u000122\u0001\u0001aa\002//v.youku.com/v_show/id_XMzYwOTExMDUwOA==.html\u0001软件企业会计考点汇总_软件企业会计做账难点_软件企业会计实操指点\u00012018-5-16\u0001前进的未来\u0001企业融资渠道与实操\u0001//vthumb.ykimg.com/054204085AFBB0F600000126DF0E5E2D\u000149\u0001878\u0001\u0001aa\003//v.youku.com/v_show/id_XMjgyNDY4NDExMg==.html\u0001成本会计实操培训_外贸会计实操培训_企业会计实操视频\u0001会计学堂";
        String myline = "teacher@mail.chongdianleme.com\006//v.youku.com/v_show/id_XMzQ5MTQ4MTM3Mg==.html\u0001企业融资私募股权基金产业基金经济金融专家宏皓给淮安市政府讲授转型升级之路66\u00012018-3-26\u0001宏皓2017\u0001企业融资渠道与实操\u0001//vthumb.ykimg.com/054204085AB8633C0000016072030DD1\u000120\u000122\u0001\u0001aa\002//v.youku.com/v_show/id_XMzYwOTExMDUwOA==.html\u0001软件企业会计考点汇总_软件企业会计做账难点_软件企业会计实操指点\u00012018-5-16\u0001前进的未来\u0001企业融资渠道与实操\u0001//vthumb.ykimg.com/054204085AFBB0F600000126DF0E5E2D\u000149\u0001878\u0001\u0001aa\003//v.youku.com/v_show/id_XMjgyNDY4NDExMg==.html\u0001成本会计实操培训_外贸会计实操培训_企业会计实操视频\u0001会计学堂";
        lines.add(qqline);
        lines.add(wyline);
        lines.add(myline);
        for(String line : lines) {
            String[] userArr = line.split("\006");
            String toUser = null;
            String subject = null;
            List<String> tkList = new ArrayList<>();
            List<String> ydList = new ArrayList<>();
            if (userArr.length == 2) {
                toUser = userArr[0];
                String tkydLine = userArr[1];
                String[] tkydArr = tkydLine.split("\003");
                String tks = tkydArr[0];
                String[] tkArr = tks.split("\002");
                subject = tkArr[0].split("\001")[1];
                tkList = Arrays.asList(tkArr);
                if (tkydArr.length==2) {
                    String yds = tkydArr[1];
                    String[] ydArr = yds.split("\002");
                    ydList = Arrays.asList(ydArr);
                }
            }
            //
            String content = getHtmlContent(MD5(toUser),tkList,ydList);
            TySend(message, toUser,subject,content);
        }
    }*/

    public static Boolean sendLine(String line, String host,String port,String from,String fromShouquanma,String table,String batch) {
       /* String host = "smtp.qq.com";
        String port = "587";//或25
        String from = "2097125931@qq.com";
        String fromShouquanma = "sgkmvyifjxzmeefb";*/
        /*String host = "edm.chongdianleme.com";// String host = "mail.chongdianleme.com";
        String port = "25";//或25
        String from = "student@edm.chongdianleme.com";
        String fromShouquanma = "cdlm@123456";
*/
       /* host = "edm.chongdianleme.com";
        // String host = "mail.chongdianleme.com";
         port = "25";
        //或25
        from = "teacher66@edm.chongdianleme.com";
        fromShouquanma = "cdlm@123456";*/
        System.out.println(from);
        boolean success = true;
        try {
            MimeMessage message = getMessage(host, port, from, fromShouquanma);
            String[] userArr = line.split("\006");
            String toUser = null;
            String subject = null;
            List<String> tkList = new ArrayList<>();
            List<String> ydList = new ArrayList<>();
            if (userArr.length == 2) {
                toUser = userArr[0];
                toUser = "604586220@qq.com";
                //toUser = "13582235092@163.com";
                ///toUser = "teacher666@mail.chongdianleme.com";
                System.out.println(toUser);
                String md5toUser = MD5(toUser);
                //第二次发送的时候退订的不发送。
                // if (!isExistEmail(md5toUser, table)&&!isExistEmail(md5toUser, "td")) {
               //if (!isExistEmail(md5toUser, table))
                {
                    String tkydLine = userArr[1];
                    String[] tkydArr = tkydLine.split("\003");
                    String tks = tkydArr[0];
                    String[] tkArr = tks.split("\002");
                    if (tkArr.length > 1)
                        subject = tkArr[1].split("\001")[1];
                    if (StringUtils.isEmpty(subject))
                        subject = tkArr[0].split("\001")[1];
                    else
                        subject = subject + "，" + tkArr[0].split("\001")[1];
                    tkList = Arrays.asList(tkArr);
                    if (tkydArr.length == 2) {
                        String yds = tkydArr[1];
                        String[] ydArr = yds.split("\002");
                        ydList = Arrays.asList(ydArr);
                    }
                    String content = getHtmlContent(md5toUser, tkList, ydList, table);
                    long start = System.currentTimeMillis();
                    success = TySend(message, toUser, subject, content);
                    System.out.println(success);
                    String inserttime = simpleDateFormat.format(new Date());
                    long end = System.currentTimeMillis();
                    long ts = end - start;
                    String cg = success ? "1" : "0";
                    String columns = "id,touser,ahost,aport,afrom,inserttime,cg,asubject,ts,batch";
                    String value = md5toUser + "\001" + toUser + "\001" + host + "\001" + port + "\001" + from + "\001" + inserttime + "\001" + cg + "\001" + subject + "\001" + ts + "\001" + batch;
                    //appendFile(value,"/home/hadoop/sparktask/mail/c.txt");
                    //SQLHelper.insert(table, columns, value, "\001");

                }
            } else {
                System.out.println("xy2:" + line);
                success = false;
            }
        }catch (Exception ex)
        {
            System.out.println("xy2:" + line);
            success = false;
        }
        return success;
    }
    public static void appendFile(String line,String appendPath) {
        FileWriter fw = null;
        try {
//如果文件存在，则追加内容；如果文件不存在，则创建文件
            //File f=new File("D:\\ykwindows.txt");
            File f=new File(appendPath);
            //File f=new File("/home/hadoop/sparktask/youku/listtu/alitu02.txt");///home/hadoop/sparktask/youku/out
            //File f=new File("/home/hadoop/sparktask/youku/listtu/handalitu.txt");
            //--input file:///home/hadoop/sparktask/tk/yk
            fw = new FileWriter(f, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(fw);
        pw.println(line);
        pw.flush();
        try {
            fw.flush();
            pw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static boolean isExistEmail(String id,String table)
    {
        String sql = "select id from "+table+" where id=\""+id+"\"";
        JSONArray jsonArray = SQLHelper.query(sql);
        if (jsonArray.size()>0)
            return true;
        else
            return false;
    }
    public static boolean TySend(MimeMessage message,String toUser,String subject,String content)
    {
        boolean success = false;
        try {
            //设置收件人的邮箱
            InternetAddress to = new InternetAddress(toUser);
            message.setRecipient(RecipientType.TO, to);
            //设置邮件标题
            message.setSubject(subject);
            //设置邮件的内容体
            message.setContent(content, "text/html;charset=UTF-8");
            //最后当然就是发送邮件啦
            Transport.send(message);
            success=true;
        } catch (AddressException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MessagingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        }
        return success;
    }
    public static String getHtmlContent(String toid, List<String> tks,List<String> yds,String table)
    {
        String html = "<h2 style=\"color: #1874CD; margin: 11px 0;\">\n" +
                "\t\t充电了么官网 ( <a href=\"http://www.chongdianleme.com/?from=toid替换&amp;tb=table替换\">www.chongdianleme.com</a>\n" +
                "\t\t) 的机器人老师精心为您挑选了以下免费课程：\n" +
                "\t</h2>\n" +
                "\t<p style=\"font-size: 13pt;\">尊敬的用户你好，充电了么App是针对<b>上班族</b>提高工作技能充电学习的<b>免费在线教育app</b>，属于北京充电了么科技有限公司的产品，在所有安卓应用商店和苹果App Store搜索“充电了么”即可下载。为用户提供了各种职位的学习课程,除了<b>专业技能</b>学习，还有<b>职场软技能</b>，比如职业生涯规划、社交礼仪、和上级同事沟通技巧、演讲技巧、工作压力如何放松、人脉关系等等。</p>\n" +
                "\t<a  style=\"font-size: 13pt; font-weight: bold; margin: 2px 10px 5px 0; display: block;color: #1874CD;\" href=\"https://a.app.qq.com/o/simple.jsp?pkgname=com.charged.app\">下载官网App查看更多精彩免费课程</a>\n" +
                "\t<hr>\n" +
                "\t<ul style=\"list-style-type: none; padding: 0; margin:11px 0 0 0;\">\n" +
                "\tulli替换\n" +
                "\t</ul>\n" +
                "\t<div style=\"margin: 21px 0 0 0;\">\n" +
                "\t<p><a href=\"https://a.app.qq.com/o/simple.jsp?pkgname=com.charged.app\" title=\"下载充电了么APP,查看更多精彩免费视频课程\" target=\"_blank\"><img title=\"下载充电了么APP立刻免费送6600金币\" style=\"width:188px;height:188px;\" src=\"http://chongdianleme.com/images/wxz.png\" alt=\"下载充电了么APP,查看更多精彩免费视频课程\"></a></p>\n" +
                "\t\t<p>扫一扫，下载充电了么APP,查看更多精彩免费视频课程！</p>\n" +
                "\t</div>\n" +
                "\t<div style=\"border-top: 2px solid #40AA53; padding: 11px 0;\">\n" +
                "\t\t<a href=\"http://www.chongdianleme.com/?tdcode=toid替换&amp;from=toid替换&amp;tb=table替换\">退订充电了么机器人老师推荐？</a>\n" +
                "\t</div>";
        html = html.replace("toid替换",toid).replace("table替换",table).replace("table替换",table);
        StringBuilder sb = new StringBuilder();
        for(String line:tks)
        {
            String[] arr = line.split("\001");
            if (arr.length>=5) {
                String url = arr[0];
                String title = arr[1];
                String author = arr[2];
                String playcount = arr[3];
                try {
                    playcount = String.valueOf(Integer.parseInt(arr[3])+500);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                String seconds = arr[4];
                sb.append("<li style=\"margin: 21px 0;\">");
                sb.append("<span style=\"font-size: 11pt; color: #00CD66; font-weight: bold; font-family: Courier New;\">价格：￥免费</span>");
                //?url=url替换&amp;from=toid替换&amp;lx=xq">标题替换</a></span>
                String spanTitle = "<span style=\"font-size: 13pt; font-weight: bold; margin: 2px 10px 5px 0; display: block;\"><a  style=\"color: #1874CD;\" href=\"http://chongdianleme.com/pages/tkDetail.jsp?url=url替换&amp;from=toid替换&amp;lx=xq&amp;tb=table替换\">标题替换</a></span>";
                spanTitle = spanTitle.replace("url替换", url).replace("toid替换", toid).replace("标题替换", title).replace("table替换", table);
                sb.append(spanTitle);
                //马士兵老师带你轻松征服hadoop二，不一样的程序员, 2017-8-9 ，654次播放， 时长：1时2分19秒 ，价格：￥免费
                String spanContent = "<span style=\"color: #666; font-size: 11pt;\">替换内容</span>";
                String content = "类型：视频课程，作者：" + author + "，" + playcount + "次播放，时长：" + seconds;
                spanContent = spanContent.replace("替换内容", content);
                sb.append(spanContent);
                sb.append("</li>");
            }
        }
        //阅读
        for(String line:yds)
        {
            String[] arr = line.split("\001");
            if (arr.length>=3) {
                String url = arr[0];
                String title = arr[1];
                String sourceFrom = arr[2];
                sb.append("<li style=\"margin: 21px 0;\">");
                sb.append("<span style=\"font-size: 11pt; color: #00CD66; font-weight: bold; font-family: Courier New;\">价格：￥免费</span>");
                //?url=url替换&amp;from=toid替换&amp;lx=xq">标题替换</a></span>
                String spanTitle = "<span style=\"font-size: 13pt; font-weight: bold; margin: 2px 10px 5px 0; display: block;\"><a  style=\"color: #1874CD;\" href=\"http://chongdianleme.com/pages/yddetail.jsp?url=url替换&amp;from=toid替换&amp;tb=table替换\">标题替换</a></span>";
                spanTitle = spanTitle.replace("url替换", url).replace("toid替换", toid).replace("标题替换", title).replace("table替换", table);
                sb.append(spanTitle);
                //马士兵老师带你轻松征服hadoop二，不一样的程序员, 2017-8-9 ，654次播放， 时长：1时2分19秒 ，价格：￥免费
                String spanContent = "<span style=\"color: #666; font-size: 11pt;\">替换内容</span>";
                String content = "类型：文章阅读，来源：" + sourceFrom;
                spanContent = spanContent.replace("替换内容", content);
                sb.append(spanContent);
                sb.append("</li>");
            }
        }
        html = html.replace("ulli替换",sb.toString());
        return html;
    }
    public static MimeMessage getMessage(String host,String port,String from,String fromShouquanma) {
        try {
            //创建Properties 类用于记录邮箱的一些属性
            final Properties props = new Properties();
            //表示SMTP发送邮件，必须进行身份验证
            props.put("mail.smtp.auth", "true");
            //此处填写SMTP服务器
            props.put("mail.smtp.host", host);
            //端口号，QQ邮箱给出了两个端口，但是另一个我一直使用不了，所以就给出这一个587
            props.put("mail.smtp.port",port);
            //此处填写你的账号
            props.put("mail.user",from);
            props.put("mail.smtp.timeout", "6000");//毫秒 props.put("mail.smtp.timeout", "6000");//毫秒
            //此处的密码就是前面说的16位STMP口令--设置账号-pop3stmp开启短信验证授权吗//https://service.mail.qq.com/cgi-bin/help?subtype=1&&id=28&&no=1001256
            props.put("mail.password", fromShouquanma);
            //构建授权信息，用于进行SMTP进行身份验证
            Authenticator authenticator = new Authenticator() {

                protected PasswordAuthentication getPasswordAuthentication() {
                    // 用户名、密码
                    String userName = props.getProperty("mail.user");
                    String password = props.getProperty("mail.password");
                    return new PasswordAuthentication(userName, password);
                }
            };
            //使用环境属性和授权信息，创建邮件会话
            Session mailSession = Session.getInstance(props, authenticator);
            //创建邮件消息
            MimeMessage message = new MimeMessage(mailSession);
            //设置发件人
            InternetAddress form = new InternetAddress(
                    props.getProperty("mail.user"));
            String nick="";
            try {
                nick=javax.mail.internet.MimeUtility.encodeText("充电了么");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            message.setFrom(new InternetAddress(nick+" <"+from+">"));
            return message;
        } catch (AddressException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MessagingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static String MD5(String key) {
        char hexDigits[] = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
        };
        try {
            byte[] btInput = key.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }
}
