package com.chongdianleme.job;

import com.alibaba.fastjson.JSONArray;
import org.apache.hadoop.hbase.util.Strings;

import javax.mail.*;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chenjinglei on 2019/08/03.
 */
public class SendTuiGuang {
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {

    }



    public static Boolean sendLine(String toUser, String host,String port,String from,String fromShouquanma,String table,String batch) {
        if (Strings.isEmpty(toUser)) {
            System.out.println("为空");
            return false;
        }
        System.out.println("发送邮件");
        if(!isEmail(toUser))
        {
            System.out.println(toUser+"-非法邮箱");
            return false;
        }
        boolean success = true;
        try {
            MimeMessage message = getMessage(host, port, from, fromShouquanma);
            String md5toUser = MD5(toUser);
            String subject = "员工福利：充电了么App-帮助员工提高工作效率的职业技能提升在线教育平台";
            if (!isExistEmail(md5toUser, table))
            {
                String content = getHtmlContent(md5toUser, new ArrayList<>(),  new ArrayList<>(), table);
                long start = System.currentTimeMillis();
                success = TySend(message, toUser, subject, content);
                String inserttime = simpleDateFormat.format(new Date());
                long end = System.currentTimeMillis();
                long ts = end - start;
                String cg = success ? "1" : "0";
                String columns = "id,touser,ahost,aport,afrom,inserttime,cg,asubject,ts,batch";
                String value = md5toUser + "\001" + toUser + "\001" + host + "\001" + port + "\001" + from + "\001" + inserttime + "\001" + cg + "\001" + subject + "\001" + ts + "\001" + batch;
                //appendFile(value,"/home/hadoop/sparktask/mail/c.txt");
                SQLHelper.insert(table, columns, value, "\001");


            }

        }catch (Exception ex)
        {
            System.out.println("xy2:" + toUser);
            success = false;
        }
        return success;
    }

    public static boolean isEmail(String email) {
        try {
            // 正常邮箱
            // /^\w+((-\w)|(\.\w))*\@[A-Za-z0-9]+((\.|-)[A-Za-z0-9]+)*\.[A-Za-z0-9]+$/

            // 含有特殊 字符的 个人邮箱 和 正常邮箱
            // js: 个人邮箱
            // /^[\-!#\$%&'\*\+\\\.\/0-9=\?A-Z\^_`a-z{|}~]+@[\-!#\$%&'\*\+\\\.\/0-9=\?A-Z\^_`a-z{|}~]+(\.[\-!#\$%&'\*\+\\\.\/0-9=\?A-Z\^_`a-z{|}~]+)+$/

            // java：个人邮箱
            // [\\w.\\\\+\\-\\*\\/\\=\\`\\~\\!\\#\\$\\%\\^\\&\\*\\{\\}\\|\\'\\_\\?]+@[\\w.\\\\+\\-\\*\\/\\=\\`\\~\\!\\#\\$\\%\\^\\&\\*\\{\\}\\|\\'\\_\\?]+\\.[\\w.\\\\+\\-\\*\\/\\=\\`\\~\\!\\#\\$\\%\\^\\&\\*\\{\\}\\|\\'\\_\\?]+

            // 范围 更广的 邮箱验证 “/^[^@]+@.+\\..+$/”
            final String pattern1 = "[\\w.\\\\+\\-\\*\\/\\=\\`\\~\\!\\#\\$\\%\\^\\&\\*\\{\\}\\|\\'\\_\\?]+@[\\w.\\\\+\\-\\*\\/\\=\\`\\~\\!\\#\\$\\%\\^\\&\\*\\{\\}\\|\\'\\_\\?]+\\.[\\w.\\\\+\\-\\*\\/\\=\\`\\~\\!\\#\\$\\%\\^\\&\\*\\{\\}\\|\\'\\_\\?]+";
            final Pattern pattern = Pattern.compile(pattern1);
            final Matcher mat = pattern.matcher(email);
            return mat.matches();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void appendFile(String line,String appendPath) {
        FileWriter fw = null;
        try {
//如果文件存在，则追加内容；如果文件不存在，则创建文件
            File f=new File(appendPath);
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
                "\t\t送给公司员工的福利：" +
                "<p>充电了么App-帮助员工提高工作效率的职业技能提升在线教育平台！</p> \n\n " +
                "<p><a href=\"http://www.chongdianleme.com/?from=toid替换&amp;tb=table替换\">充电了么公司官网：www.chongdianleme.com</a>\n\n </p>" +
                "\t\t\n <p><a href=\"https://a.app.qq.com/o/simple.jsp?pkgname=com.charged.app\">点击查看充电了么App官方应用市场下载地址</a>\n\n </p>" +
                "\t</h2>\n" +
                "\t<div style=\"margin: 21px 0 0 0;\">\n" +
                "\t<p><a href=\"https://a.app.qq.com/o/simple.jsp?pkgname=com.charged.app\" title=\"下载充电了么App,查看更多精彩免费视频课程\" target=\"_blank\"><img title=\"下载官网App送百万学币\" style=\"width:236px;height:236px;\" src=\"http://chongdianleme.com/images/wxz.png\" alt=\"下载官网App送百万学币\"></a></p>\n" +
                "\t\t<p>扫一扫，下载官网App</p>\n" +
                "\t</div>\n" +
                "\t<div style=\"border-top: 2px solid #40AA53; padding: 11px 0;\">\n" +
                "\t\t<a href=\"http://www.chongdianleme.com/?tdcode=toid替换&amp;from=toid替换&amp;tb=table替换\">退订充电了么老师推荐？</a>\n" +
                "\t</div>";
        html = html.replace("toid替换",toid).replace("table替换",table).replace("table替换",table);
        StringBuilder sb = new StringBuilder();
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
                nick=javax.mail.internet.MimeUtility.encodeText("充电了么App - 发放员工福利");
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
