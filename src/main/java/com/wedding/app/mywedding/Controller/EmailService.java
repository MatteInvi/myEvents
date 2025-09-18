package com.wedding.app.mywedding.Controller;

  
 
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.mail.SimpleMailMessage;
    import org.springframework.mail.javamail.JavaMailSender;
    import org.springframework.stereotype.Service;



    @Service
    public class EmailService {

        @Autowired
        private JavaMailSender mailSender;

        public void sendEmail(String to) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Partecipazione matrimonio");
            message.setText(
                "Sei invitato..."
            );
            mailSender.send(message);
        }
    }