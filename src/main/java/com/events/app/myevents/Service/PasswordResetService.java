package com.events.app.myevents.Service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.events.app.myevents.Model.PasswordResetToken;
import com.events.app.myevents.Model.User;
import com.events.app.myevents.Repository.PasswordResetTokenRepository;
import com.events.app.myevents.Repository.UserRepository;

import jakarta.persistence.EntityManager;


@Service
public class PasswordResetService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    EmailService emailService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    EntityManager entityManager;

    @Transactional
    public void sendResetLink(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            // rimuovi token precedenti per lo stesso utente
    
            passwordResetTokenRepository.deleteByUser(user);
            entityManager.flush();

            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(token);
            resetToken.setUser(user);
            resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));
            passwordResetTokenRepository.save(resetToken);

            String link = "http://localhost:8080/user/reset-password?token=" + token;
            String subject = "Recupero password";
            String message = "Clicca il link per reimpostare la password: " + link;

            emailService.sendRecoveryEmail(user.getEmail(), subject, message);
        });
        // Risposta neutra anche se l'email non è registrata
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token non valido"));

        if (resetToken.isExpired()) {
            throw new IllegalArgumentException("Token scaduto");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
    }
}
