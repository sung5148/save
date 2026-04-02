package com.c1.donguri.email;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/*
    실제 SMTP 발송 담당 클래스
    Gmail SMTP
*/
public class SendEmail {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";

    // 실제 발송용 SMTP 계정
    private static final String SMTP_EMAIL = "donguriyuubin@gmail.com";

    // Gmail 앱 비밀번호
    private static final String SMTP_PASSWORD = "Donguri123!@#";

    /*
        매개변수:
        - receiverEmail : 받는 사람 이메일
        - title         : 메일 제목
        - subject       : 엽서 제목
        - content       : 엽서 내용
    */
    public static void send(String receiverEmail, String title, String subject, String content) throws Exception {
        Properties props = new Properties();

        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_EMAIL, SMTP_PASSWORD);
            }
        });

        MimeMessage message = new MimeMessage(session);

        // 실제 발송자
        message.setFrom(new InternetAddress(SMTP_EMAIL));

        // 수신자
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(receiverEmail));

        /*
            메일 제목 title 사용
        */
        message.setSubject(title, "UTF-8");

        /*
            메일 본문에는 엽서 제목 + 엽서 내용을 함께 표시
        */
        String body = "[엽서 제목]\n"
                + subject
                + "\n\n[엽서 내용]\n"
                + content;

        message.setText(body, "UTF-8");

        Transport.send(message);
    }
}