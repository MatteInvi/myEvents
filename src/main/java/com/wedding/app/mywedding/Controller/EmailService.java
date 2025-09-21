package com.wedding.app.mywedding.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.wedding.app.mywedding.Model.Invited;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

  @Autowired
  private JavaMailSender mailSender;

  public void sendEmail(String to, Invited invited) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setTo(to);
      helper.setSubject("Partecipazione matrimonio");
      String html = String.format("""
          <html>
            <body style="font-family: Arial, sans-serif;">
              <h1 style="color:#2e6c80;">Sei invitato al nostro matrimonio!</h1>
              <p>Ciao <strong>%s</strong> <strong>%s</strong> sei invitato... </p>
           </body>
          </html>
          """, invited.getName(), invited.getSurname());

      helper.setText(html, true);

      mailSender.send(message);
    } catch (Exception e) {

    }
  }

  public void registerEmail(String to, String content) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setTo(to);
      helper.setSubject("");
      String html = String.format("""

          """);

      helper.setText(html, true);

      mailSender.send(message);
    } catch (Exception e) {

    }
  }
}