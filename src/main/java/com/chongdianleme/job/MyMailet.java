package com.chongdianleme.job;

import org.apache.mailet.Mail;
import org.apache.mailet.MailAddress;
import org.apache.mailet.MailetConfig;
import org.apache.mailet.base.GenericMailet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Collection;
import java.util.Iterator;

public class MyMailet extends GenericMailet {
    private static final Logger logger = LoggerFactory.getLogger(MyMailet.class);

    @Override
    public void init(MailetConfig newConfig) throws MessagingException {
        super.init(newConfig);
    }

    @Override
    public void service(Mail mail){
        try {
            System.out.println("================================================================================================");
            System.out.println("================================================================================================");
            MailAddress sender = mail.getSender();
            Collection<MailAddress> recipients = mail.getRecipients();
            MimeMessage message = mail.getMessage();
            MimeMultipart content = (MimeMultipart) message.getContent();
//            int count = content.getCount();
            Iterator<MailAddress> iterator = recipients.iterator();
            System.out.println("==========================发件箱：" + sender.toInternetAddress().toString() + "==========================");
            while (iterator.hasNext()) {
                MailAddress next = iterator.next();
                System.out.println("==========================收件箱：" + next.toInternetAddress().toString() + "=====================");
            }
//            for (int i=0;i<count;i++) {
                BodyPart bodyPart = content.getBodyPart(0);
                Object content1 = bodyPart.getContent();
            if (content1 instanceof MimeMultipart) {
                MimeMultipart contentx = (MimeMultipart) content1;
                BodyPart bodyPart1 = contentx.getBodyPart(0);
                Object contentx2 = bodyPart1.getContent();
                System.out.println("===========================内容：" + contentx2.toString() + "===========================================================");
            }else {
                System.out.println("===========================内容：" + content1.toString() + "===========================================================");
            }
//            }
            System.out.println("===========================主题：" + message.getSubject() + "===========================================================");
//            System.out.println("===========================附件：" + message.getFileName() + "===========================================================");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}