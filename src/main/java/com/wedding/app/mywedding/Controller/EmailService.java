package com.wedding.app.mywedding.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.wedding.app.mywedding.Model.User;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, User credential) {
            try{
             MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject("Partecipazione matrimonio");
            String html = String.format("""
                    <html>
                      <body style="font-family: Arial, sans-serif;">
                        <h1 style="color:#2e6c80;">Sei invitato al nostro matrimonio!</h1>
                        <p>Qui trovarai i dati per accedere alla nostra piattaforma</p>
                        <p>Username: <strong>%s</strong></p>
                        <p>Passowrd: <strong>%s</strong></p>
                     </body>
                    </html>
                    """, credential.getUsername(), credential.getPassword());

            helper.setText(html, true);



            mailSender.send(message);
        } catch(Exception e){

        }
    }
}