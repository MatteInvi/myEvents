package com.events.app.myevents.Controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.events.app.myevents.Model.User;
import com.events.app.myevents.Repository.UserRepository;

@org.springframework.web.bind.annotation.ControllerAdvice
public class ControllerAdvice {

    @Autowired
    UserRepository userRepository;

    @ModelAttribute
    public void addLoggedUser(Model model, Authentication authentication) {

        if (authentication != null && authentication.isAuthenticated()) {
            Optional<User> utenteLoggato = userRepository.findByEmail(authentication.getName());
            model.addAttribute("userLogged", utenteLoggato.get());
        }
    }

}
