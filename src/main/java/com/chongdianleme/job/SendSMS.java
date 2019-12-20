//package com.chongdianleme.job;
//
//import com.alibaba.fastjson.JSONArray;
////import com.alicom.dysms.api.SmsDemo;
//import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
//
//import javax.mail.*;
//import javax.mail.Message.RecipientType;
//import javax.mail.internet.AddressException;
//import javax.mail.internet.InternetAddress;
//import javax.mail.internet.MimeMessage;
//import java.io.*;
//import java.security.MessageDigest;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.List;
//import java.util.Properties;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
///**
// * Created by chenjinglei on 2019/02/02.
// */
//public class SendSMS {
//    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//    public static void main(String[] args) {
//
//    }
//
//    public static Boolean sendPhone(String phone,String tcode,String table,String batch) {
//        boolean success = true;
//        try {
//            String md5toUser = MD5(phone);
//            if (!isExistEmail(md5toUser, table))
//            {
//                long start = System.currentTimeMillis();
//                SendSmsResponse smsResponse = SmsDemo.sendTuiGuang(phone,tcode);
//                String rcode = smsResponse.getCode();
//                if (rcode.equals("OK"))
//                    success=true;
//                else
//                    success=false;
//                String inserttime = simpleDateFormat.format(new Date());
//                long end = System.currentTimeMillis();
//                long ts = end - start;
//                String cg = success ? "1" : "0";
//                String columns = "id,touser,tcode,inserttime,cg,rcode,ts,batch";
//                String value = md5toUser + "\001" + phone + "\001" + tcode + "\001" + inserttime + "\001" + cg + "\001" + rcode + "\001" + ts + "\001" + batch;
//                SQLHelper.insert(table, columns, value, "\001");
//
//            }
//
//        }catch (Exception ex)
//        {
//            System.out.println("phone:" + phone);
//            success = false;
//        }
//        return success;
//    }
//
//
//    public static boolean isExistEmail(String id,String table)
//    {
//        String sql = "select id from "+table+" where id=\""+id+"\"";
//        JSONArray jsonArray = SQLHelper.query(sql);
//        if (jsonArray.size()>0)
//            return true;
//        else
//            return false;
//    }
//    public static String MD5(String key) {
//        char hexDigits[] = {
//                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
//        };
//        try {
//            byte[] btInput = key.getBytes();
//            // 获得MD5摘要算法的 MessageDigest 对象
//            MessageDigest mdInst = MessageDigest.getInstance("MD5");
//            // 使用指定的字节更新摘要
//            mdInst.update(btInput);
//            // 获得密文
//            byte[] md = mdInst.digest();
//            // 把密文转换成十六进制的字符串形式
//            int j = md.length;
//            char str[] = new char[j * 2];
//            int k = 0;
//            for (int i = 0; i < j; i++) {
//                byte byte0 = md[i];
//                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
//                str[k++] = hexDigits[byte0 & 0xf];
//            }
//            return new String(str);
//        } catch (Exception e) {
//            return null;
//        }
//    }
//}
