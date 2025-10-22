package com.events.app.myevents.Controller;

import com.events.app.myevents.Model.Role;
import com.events.app.myevents.Model.User;
import com.events.app.myevents.Model.authToken;
import com.events.app.myevents.Repository.RoleRepository;
import com.events.app.myevents.Repository.TokenRepository;
import com.events.app.myevents.Repository.UserRepository;
import com.events.app.myevents.Service.EmailService;

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
    RoleRepository roleRepository;

    @Autowired
    TokenRepository tokenRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailService emailService;


    @GetMapping
    public String Home(Authentication authenication, Model model, RedirectAttributes redirectAttributes) {
        
        //Se non è loggato mostra la pagina home
        if (authenication == null || !authenication.isAuthenticated()) {
            return "pages/home";
        }

        Optional<User> utenteLoggato = userRepository.findByEmail(authenication.getName());
        //Se è admin può accedervi comunque anche se non verificato
        for (Role singleRole : utenteLoggato.get().getRoles()) {
            if (singleRole.getNome().equals("ADMIN")){
                return "pages/home";
            }            
        }
        

        // Se l'utente non è ancora verificato
        if (utenteLoggato.get().getVerified() == false) {
            // Elimino i token scaduti
            tokenRepository.deleteAllExpiredToken(LocalDateTime.now());

            // Se il token è vuoto (quindi era scaduto) ne genero uno nuovo direttamente
            if (utenteLoggato.get().getAuthToken() == null) {
                String token = UUID.randomUUID().toString();
                authToken authToken = new authToken();
                authToken.setToken(token);
                authToken.setUser(utenteLoggato.get());
                authToken.setExpireDate(LocalDateTime.now().plusHours(24));
                utenteLoggato.get().setAuthToken(authToken);

                tokenRepository.save(authToken);
              

                // Inviamo una nuova mail con il token di verifica
                try {
                    emailService.registerEmail(utenteLoggato.get(), authToken);
                    model.addAttribute("message", "Controllare la mail per confermare la registrazione");
                } catch (Exception e) {
                    model.addAttribute("message", "Errore nell'invio: " + e);
                }
                return "pages/message";
                
            // Altrimemti invito l'utente a confermare tramite il token precedentemente invitato
            } else {
                model.addAttribute("message", "Conferma l'account per poter utilizzare le funzioni");
                return "pages/message";
            }


        }
        return "pages/home";
    }

    @GetMapping("/login")
    public String login() {

        return "pages/login";
    }

}
