package com.wedding.app.mywedding.Controller;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    
    @GetMapping
    public String register(Model model){
        model.addAttribute("user", new User());
        return "utenti/register";
    };

    @PostMapping
    public String save(@Valid @ModelAttribute("user") User formUser, BindingResult bindingResult, Model model){
        
        if (bindingResult.hasErrors()){
            return "utenti/register";
        }
        String token = UUID.randomUUID().toString();
        authToken authToken = new authToken();
        authToken.setToken(token);
        authToken.setUser(formUser);
        authToken.setExpireDate(LocalDateTime.now().plusHours(24));

        emailService.registerEmail(formUser, authToken);

        

        model.addAttribute("message", "Controllare la mail per confermare la registrazione");
        return "pages/home";

        
    }

    // @GetMapping("/confirm")
    // public String confirmRegistration(@RequestParam("token") String token, Model model){
    //     authToken authToken = tokenRepository.findByToken(token);

    //     // if (authToken.getExpireDate().isBefore(LocalDateTime.now())){
    //     //     return model.addAttribute("message", "Token scaduto!");
    //     // }

   

    // }
}
