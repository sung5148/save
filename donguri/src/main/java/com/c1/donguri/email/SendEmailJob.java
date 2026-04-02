package com.c1.donguri.email;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/*
    예약 시간이 되었을 때 Quartz가 실행하는 Job

    수신자 이메일은 Quartz JobDataMap으로 직접 넘김.

    흐름:
    1) JobDataMap에서 emailContentId 조회
    2) DB에서 메일 본문 조회
    3) JobDataMap에서 receiverEmail 조회
    4) SendEmail.send(...) 호출
*/
public class SendEmailJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            String emailContentId = context.getMergedJobDataMap().getString("emailContentId");
            String receiverEmail = context.getMergedJobDataMap().getString("receiverEmail");

            EmailDAO dao = new EmailDAO();
            EmailDTO e = dao.getEmailContentById(emailContentId);

            if (e == null) {
                throw new Exception("email_content 데이터를 찾을 수 없습니다. id = " + emailContentId);
            }

            SendEmail.send(
                    receiverEmail,
                    e.getTitle(),
                    e.getSubject(),
                    e.getContent()
            );

            System.out.println("예약 메일 전송 완료: " + emailContentId);

        } catch (Exception e) {
            e.printStackTrace();
            throw new JobExecutionException(e);
        }
    }
}