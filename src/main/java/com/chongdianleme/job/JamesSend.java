package com.chongdianleme.job;

import org.apache.commons.lang3.StringUtils;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;


public class JamesSend {
    //https://www.aliyun.com/jiaocheng/565619.html邮件服务器james环境搭建
    public static void main(String[] args) throws AddressException {

    }
    public static Boolean sendTo(String from,String password,String to,String host,String title,String content) throws AddressException {
        Boolean status = true;
        // 获取系统属性
        Properties properties = System.getProperties();

        // 设置邮件服务器
        String p = properties.getProperty("mail.smtp.host");
        if (StringUtils.isEmpty(p)) {
            properties.setProperty("mail.smtp.host", host);
            //properties.setProperty("mail.smtp.port", "465");
            properties.put("mail.smtp.auth", "true");
        }
        // 获取默认session对象
        Session session = Session.getDefaultInstance(properties,new Authenticator(){
            public PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(from, password); //发件人邮件用户名、密码
            }
        });
        try{

            // 创建默认的 MimeMessage 对象
            MimeMessage message = new MimeMessage(session);

            // Set From: 头部头字段
            message.setFrom(new InternetAddress(from));

            // Set To: 头部头字段
            message.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(to));

            // Set Subject: 头部头字段
            message.setSubject(title);

            // 设置消息体
            message.setContent(content, "text/html;charset=UTF-8");
//http://hanxin0311.iteye.com/blog/383765
            // 发送消息
           // session.getTransport().
            Transport.send(message);
        }catch (Exception mex) {
            status = false;
            mex.printStackTrace();

        }
        finally {

        }
        return status;
    }
}
