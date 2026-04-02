package com.c1.donguri.email;

import com.c1.donguri.util.DBManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

/*
    email_content 저장
    email_content_id로 1건 조회

*/
public class EmailDAO {

    /*
        UUID 생성
    */
    public String makeUUID() {
        return UUID.randomUUID().toString();
    }

    /*
        이메일 내용 저장
        - email_content_id가 비어 있으면 여기서 생성
        - created_at / updated_at은 DB default로 처리
    */
    public int insertEmailContent(EmailDTO e) {
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBManager.DB_MANAGER.getConnection();

            if (e.getEmailContentId() == null || e.getEmailContentId().trim().isEmpty()) {
                e.setEmailContentId(makeUUID());
            }

            String sql = "insert into email_content "
                    + "(email_content_id, template_id, sender_id, _title, subject, content) "
                    + "values (?, ?, ?, ?, ?, ?)";

            ps = con.prepareStatement(sql);
            ps.setString(1, e.getEmailContentId());
            ps.setString(2, e.getTemplateId());
            ps.setString(3, e.getSenderId());
            ps.setString(4, e.getTitle());
            ps.setString(5, e.getSubject());
            ps.setString(6, e.getContent());

            return ps.executeUpdate();

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            DBManager.DB_MANAGER.close(con, ps, null);
        }

        return 0;
    }

    /*
        email_content_id로 저장된 메일 내용 1건 조회
    */
    public EmailDTO getEmailContentById(String emailContentId) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBManager.DB_MANAGER.getConnection();

            String sql = "select * from email_content where email_content_id = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, emailContentId);

            rs = ps.executeQuery();

            if (rs.next()) {
                EmailDTO e = new EmailDTO();
                e.setEmailContentId(rs.getString("email_content_id"));
                e.setTemplateId(rs.getString("template_id"));
                e.setSenderId(rs.getString("sender_id"));
                e.setTitle(rs.getString("_title"));
                e.setSubject(rs.getString("subject"));
                e.setContent(rs.getString("content"));
                e.setCreatedAt(rs.getTimestamp("created_at"));
                e.setUpdatedAt(rs.getTimestamp("updated_at"));
                return e;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            com.c1.donguri.util.DBManager.DB_MANAGER.close(con, ps, rs);
        }

        return null;
    }
}