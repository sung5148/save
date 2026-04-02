package com.c1.donguri.email;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
    사용자가 writeEmail.jsp에서 작성한 폼을 받아 처리하는 서블릿

    하는 일:
    1) 입력값 받기
    2) 유효성 검사
    3) email_content 테이블 저장
    4) Quartz 예약 등록
*/
@WebServlet("/reserveEmail")
public class ReserveEmailC extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        try {
            // -------------------------------------------------
            // 1. 화면에서 넘어온 값 받기
            // -------------------------------------------------
            String templateId = request.getParameter("templateId");
            String senderId = request.getParameter("senderId");
            String receiverEmail = request.getParameter("receiverEmail");
            String subject = request.getParameter("subject");
            String content = request.getParameter("content");
            String reserveDateTime = request.getParameter("reserveDateTime");

            String title = "[동구리]님에게 동구리가 찾아왔습니다";

            // -------------------------------------------------
            // 2. 기본 유효성 검사
            // -------------------------------------------------
            if (senderId == null || senderId.trim().isEmpty()) {
                throw new Exception("senderId가 필요합니다.");
            }

            if (receiverEmail == null || receiverEmail.trim().isEmpty()) {
                throw new Exception("받는 사람 이메일을 입력해주세요.");
            }

            if (content == null || content.trim().isEmpty()) {
                throw new Exception("엽서 내용을 입력해주세요.");
            }

            if (reserveDateTime == null || reserveDateTime.trim().isEmpty()) {
                throw new Exception("예약 일시를 입력해주세요.");
            }

            // -------------------------------------------------
            // 3. 문자열 -> Date 파싱
            // datetime-local 값 예: 2026-04-02T15:35
            // -------------------------------------------------
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            Date runTime = sdf.parse(reserveDateTime);

            // 현재 시간보다 이전이면 예약 불가
            if (runTime.before(new Date())) {
                throw new Exception("현재 시간 이후로만 예약할 수 있습니다.");
            }

            // 5분 단위 체크
            int minute = Integer.parseInt(reserveDateTime.substring(14, 16));
            if (minute % 5 != 0) {
                throw new Exception("예약 시간은 5분 단위로만 설정 가능합니다.");
            }

            // -------------------------------------------------
            // 4. DTO에 담기
            // -------------------------------------------------
            EmailDTO e = new EmailDTO();
            e.setTemplateId((templateId == null || templateId.trim().isEmpty()) ? null : templateId);
            e.setSenderId(senderId);
            e.setTitle(title);
            e.setSubject(subject);
            e.setContent(content);
            e.setReceiverEmail(receiverEmail);
            e.setReserveDateTime(runTime);

            // -------------------------------------------------
            // 5. DB 저장
            // -------------------------------------------------
            EmailDAO dao = new EmailDAO();
            int result = dao.insertEmailContent(e);

            if (result != 1) {
                throw new Exception("이메일 내용 저장 실패");
            }

            // -------------------------------------------------
            // 6. Quartz에 예약 등록
            // receiverEmail은 DB에 없으므로 JobDataMap에 넣음
            // -------------------------------------------------
            Scheduler scheduler = QuartzManager.getScheduler();

            if (scheduler == null) {
                throw new Exception("Quartz Scheduler 초기화 실패");
            }

            JobDetail job = JobBuilder.newJob(SendEmailJob.class)
                    .withIdentity("sendEmailJob_" + e.getEmailContentId(), "emailGroup")
                    .usingJobData("emailContentId", e.getEmailContentId())
                    .usingJobData("receiverEmail", receiverEmail)
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("sendEmailTrigger_" + e.getEmailContentId(), "emailGroup")
                    .startAt(runTime)
                    .build();

            scheduler.scheduleJob(job, trigger);

            // -------------------------------------------------
            // 7. 성공 응답
            // -------------------------------------------------
            response.getWriter().println("<script>");
            response.getWriter().println("alert('예약이 완료되었습니다.');");
            response.getWriter().println("location.href='" + request.getContextPath() + "/jsp/writeEmail.jsp';");
            response.getWriter().println("</script>");

        } catch (Exception e) {
            e.printStackTrace();

            response.getWriter().println("<script>");
            response.getWriter().println("alert('예약 실패: " + escapeJs(e.getMessage()) + "');");
            response.getWriter().println("history.back();");
            response.getWriter().println("</script>");
        }
    }

    /*
        alert 문구에서 따옴표 때문에 스크립트가 깨지지 않게 최소한으로 처리
    */
    private String escapeJs(String msg) {
        if (msg == null) {
            return "";
        }
        return msg.replace("'", "\\'");
    }
}