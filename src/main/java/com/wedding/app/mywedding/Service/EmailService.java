package com.wedding.app.mywedding.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.wedding.app.mywedding.Model.Invited;
import com.wedding.app.mywedding.Model.User;
import com.wedding.app.mywedding.Model.authToken;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

  @Autowired
  private JavaMailSender mailSender;
// Email di invio invito
  public void sendEmail(String to, Invited invited) throws Exception {
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
// Email per la conferma della registrazione
  public void registerEmail(User user, authToken token) throws Exception {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      String confimationURL = ServletUriComponentsBuilder.fromCurrentContextPath()
          .path("/register/confirm")
          .queryParam("token", token.getToken())
          .toUriString();

      helper.setTo(user.getEmail());
      helper.setSubject("Conferma registrazione");
      String html = String.format("""
          <html>
            <body>
              <h1>Conferma la tua registrazione</h1>
              <p>Clicca sul seguente link per confermare la registrazione a my weddingApp</p>
              <a style="background-color: orange ; border: 1px solid black; border-radius: 10%%; padidng:5px; text-decoration:none;" href="%s">Link</a>
            </body>
          </html>

            """, confimationURL);

      helper.setText(html, true);

      mailSender.send(message);
    } catch (Exception e) {
      System.err.println(e);
    }
  }
}