package com.events.app.myevents.Controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.events.app.myevents.Model.Event;
import com.events.app.myevents.Model.User;
import com.events.app.myevents.Repository.EventRepository;
import com.events.app.myevents.Repository.UserRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/event")
public class EventController {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    UserRepository userRepository;

    @Value("${app.url}")
    private String appUrl;

    @GetMapping("/create")
    public String create(Model model) {
        Event newEvent = new Event();
        model.addAttribute("event", newEvent);
        return "event/create";
    }

    @PostMapping("/create")
    public String save(@Valid @ModelAttribute("event") Event event, BindingResult bindingResult, Model model,
            Authentication authentication) {

        Optional<User> userLogged = userRepository.findByEmail(authentication.getName());
        if (bindingResult.hasErrors()) {
            return "event/create";
        }

        event.setUser(userLogged.get());

        eventRepository.save(event);

        event.setLinkEventPhotos(appUrl + "/photo/upload/" + event.getId());

        eventRepository.save(event);

        return "redirect:/event/info/" + event.getId();
    }

    @GetMapping("/info/{id}")
    public String show(Model model, @PathVariable("id") Integer id){
        model.addAttribute("event", eventRepository.findById(id).get());
        return "event/info";
    }

    // Indirizzo alla pagina di caricamento invito passando l'evento
    @GetMapping("/invite/upload/{id}")
    public String inviteUpload(Model model, @PathVariable Integer id){
        model.addAttribute("event", eventRepository.findById(id).get());
        return "photo/uploadInvite";
    }

}
