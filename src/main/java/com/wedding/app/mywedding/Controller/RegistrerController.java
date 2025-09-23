package com.wedding.app.mywedding.Controller;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.wedding.app.mywedding.Model.User;
import com.wedding.app.mywedding.Model.authToken;
import com.wedding.app.mywedding.Repository.TokenRepository;
import com.wedding.app.mywedding.Repository.UserRepository;
import com.wedding.app.mywedding.Service.EmailService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/register")
public class RegistrerController {

    @Autowired
    EmailService emailService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TokenRepository tokenRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @GetMapping
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "utenti/register";
    };

    // Chiamata post per salvare l'utente
    @PostMapping
    public String save(@Valid @ModelAttribute("user") User formUser, BindingResult bindingResult, Model model) {
        // Se la mail non è registrata nel db viene salvato l'utente senza verifica
        if (userRepository.existsByEmail(formUser.getEmail())) {
            bindingResult.rejectValue("email", "error.user", "Email già in uso");

            return "utenti/register";
        }

        if (bindingResult.hasErrors()) {
            return "utenti/register";
        }

        formUser.setPassword(passwordEncoder.encode(formUser.getPassword()));
        userRepository.save(formUser);

        // Generiamo un token di verifica settando i parametri dello stesso
        String token = UUID.randomUUID().toString();
        authToken authToken = new authToken();
        authToken.setToken(token);
        authToken.setUser(formUser);
        authToken.setExpireDate(LocalDateTime.now().plusHours(24));
        formUser.setAuthToken(authToken);

        // Salviamo il token nel db e restituiamo un invito a confermare la
        // registrazione
        tokenRepository.save(authToken);
        model.addAttribute("message", "Controllare la mail per confermare la registrazione");

        // Inviamo mail all'utente passando i dati del form compilato(per recuparare la
        // mail) e il token generato
        emailService.registerEmail(formUser, authToken);

        return "redirect:/";

    }

    // Sezione di conferma registrazione
    @GetMapping("/confirm")
    public String confirmRegistration(@RequestParam String token, Model model) {
        // Andiamo a prendere il token dalla repositori seguendo il link inviato
        // all'utente
        Optional<authToken> authToken = tokenRepository.findByToken(token);

        // Se non è scaduto(24h) passiamo a prendere l'utente associato al token e a
        // settare il suo stato come verificato
        if (authToken.get().getExpireDate().isBefore(LocalDateTime.now())) {

            model.addAttribute("message", "Token scaduto!");
            return "pages/home";
        }

        User user = authToken.get().getUser();
        user.setVerified(true);

        // Qui aggiorniamo l'utente nel db e resituiamo un messaggio di avvenuta
        // verifica
        userRepository.save(user);
        model.addAttribute("message", "Utente verificato!");
        return "pages/home";

    }
}
