package com.wedding.app.mywedding.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.wedding.app.mywedding.Model.User;
import com.wedding.app.mywedding.Repository.UserRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/register")
public class RegistrerController {

    @Autowired
    EmailService emailService;

    @Autowired 
    UserRepository userRepository;
    
    @GetMapping("/")
    public String register(){
        return "user/register";
    };

    @PostMapping()
    public String save(@Valid @ModelAttribute("user") User formUser, BindingResult bindingResult, Model model){
        if (bindingResult.hasErrors()){
            return "user/register";
        }

        emailService.registerEmail(formUser.getUsername(), null);

        

        model.addAttribute("registerSuccess", "Controllare la mail per confermare la registrazione");
        return "pages/home";

        
    }
}
