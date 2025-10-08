package com.events.app.myevents.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.events.app.myevents.Model.Invited;
import com.events.app.myevents.Model.User;
import com.events.app.myevents.Model.authToken;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

  @Autowired
  private JavaMailSender mailSender;

  // Email di invio invito
  public void inviteEmail(Invited invited, String linkInvite) throws Exception {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setTo(invited.getEmail());
      helper.setSubject("Invito evento");
      String html = String.format("""
          <html>
            <body style="font-family: Arial, sans-serif;">
              <h1 style="color:#2e6c80;">Sei invitato al mio Evento!</h1>
              <p>Ciao <strong>%s</strong> <strong>%s</strong> sei invitato... </p>
              <img style="width: 50vh;" class="text-align:center; border: 5 px solid royalblue; border-radius: 10px;" src="%s"> 
           </body>
          </html>
          """, invited.getName(), invited.getSurname(), linkInvite);

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
          .path("/user/confirm")
          .queryParam("token", token.getToken())
          .toUriString();

      helper.setTo(user.getEmail());
      helper.setSubject("Conferma registrazione");
      String html = String.format(
          """
              <html>

              <body style="background-color: rosybrown; padding:20px;">
                  <main style="margin:0 auto; width: 50%%; position: relative; top: 50%%;">
                      <h1 style="">Conferma la tua registrazione</h1>
                      <p>Clicca sul seguente link per confermare la registrazione a MyEvents</p>
                      <a style="background-color: royalblue; text-align: center; color: black; font-size: x-large; border-radius: 10px; padding: 5px; text-decoration: none; display: block; width: 30%%;"
                          href="%s">Conferma</a>
                  </main>
              </body>

              </html>

                          """,
          confimationURL);

      helper.setText(html, true);

      mailSender.send(message);
    } catch (Exception e) {
      System.err.println(e);
    }
  }

}