package com.events.app.myevents.Controller;

import com.cloudinary.AuthToken;
import com.events.app.myevents.Model.User;
import com.events.app.myevents.Model.authToken;
import com.events.app.myevents.Repository.TokenRepository;
import com.events.app.myevents.Repository.UserRepository;
import com.events.app.myevents.Service.EmailService;

import ch.qos.logback.core.subst.Token;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/")
public class HomeController {

    @Autowired
    TokenRepository tokenRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailService emailService;

    @GetMapping
    public String Home(Authentication authenication, Model model, RedirectAttributes redirectAttributes) {
        if (authenication == null || !authenication.isAuthenticated()) {
            return "pages/home";
        }

        Optional<User> utenteLoggato = userRepository.findByEmail(authenication.getName());
        if (utenteLoggato.get().getVerified() == false) {

            if (utenteLoggato.get().getAuthToken() != null) {
                // Elimina token precedente
                authToken oldToken = utenteLoggato.get().getAuthToken();
                utenteLoggato.get().setAuthToken(null);
                userRepository.save(utenteLoggato.get());
                tokenRepository.delete(oldToken);
            }

            // Generiamo un token di verifica settando i parametri dello stesso
            String token = UUID.randomUUID().toString();
            authToken authToken = new authToken();
            authToken.setToken(token);
            authToken.setUser(utenteLoggato.get());
            authToken.setExpireDate(LocalDateTime.now().plusHours(24));
            utenteLoggato.get().setAuthToken(authToken);

            tokenRepository.save(authToken);
            model.addAttribute("message", "Controllare la mail per confermare la registrazione");

            try {
                emailService.registerEmail(utenteLoggato.get(), authToken);
            } catch (Exception e) {
                model.addAttribute("message", "Errore nell'invio: " + e);
            }
            return "pages/message";
        }
        return "pages/home";
    }

    @GetMapping("/login")
    public String login() {

        return "pages/login";
    }

}
