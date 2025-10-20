package com.events.app.myevents.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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

    // Indice
    @GetMapping()
    public String index(Model model, Authentication authentication) {
        List<Event> events = new ArrayList<>();
        Optional<User> userLogged = userRepository.findByEmail(authentication.getName());
        for (GrantedAuthority auth : authentication.getAuthorities()) {
            if (auth.getAuthority().equals("ADMIN")) {
                events = eventRepository.findAll();
            } else if (auth.getAuthority().equals("USER")) {
                events = eventRepository.findByUser(userLogged.get());
            }

        }

        model.addAttribute("events", events);
        return "event/index";
    }

    // Creazione
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

    // Modifica
    @GetMapping("/edit/{idEvent}")
    public String edit(Model model, @PathVariable Integer idEvent, Authentication authentication) {
        Optional<Event> eventOptional = eventRepository.findById(idEvent);
        Optional<User> userLogged = userRepository.findByEmail(authentication.getName());

        for (GrantedAuthority auth : authentication.getAuthorities()) {
            if (auth.getAuthority().equals("ADMIN")) {
                model.addAttribute("event", eventRepository.findById(idEvent).get());
                return "event/edit";
                // Se è un utente verifico che l'evento appartenga a lui
            } else if (auth.getAuthority().equals("USER")) {
                if (eventOptional.get().getUser().equals(userLogged.get())) {
                    model.addAttribute("event", eventRepository.findById(idEvent).get());
                    return "event/edit";
                }
            }
        }
        model.addAttribute("message", "Non puoi accedere a questa pagina");
        return "pages/message";
    }

    @PostMapping("/edit/{idEvent}")
    public String update(@Valid @ModelAttribute("event") Event event, BindingResult bindingResult, Model model,
            Authentication authentication, @PathVariable Integer idEvent) {
        Optional<Event> eventOptional = eventRepository.findById(idEvent);
        Optional<User> userLogged = userRepository.findByEmail(authentication.getName());

        event.setUser(eventOptional.get().getUser());
        event.setId(idEvent);
        event.setInviteds(eventOptional.get().getInviteds());
        event.setLinkInvite(eventOptional.get().getLinkInvite());
        event.setLinkEventPhotos(eventOptional.get().getLinkEventPhotos());

        if (bindingResult.hasErrors()) {
            return "event/edit";
        }
        for (GrantedAuthority auth : authentication.getAuthorities()) {
            if (auth.getAuthority().equals("ADMIN")
                    || (auth.getAuthority().equals("USER") && eventOptional.get().getUser().equals(userLogged.get()))) {

                eventRepository.save(event);
                return "redirect:/event";
            }
        }
        model.addAttribute("message", "Non puoi modificare questo evento");
        return "pages/message";

    }

    // Show
    @GetMapping("/info/{id}")
    public String show(Model model, @PathVariable("id") Integer id) {
        model.addAttribute("event", eventRepository.findById(id).get());
        return "event/info";
    }

    // Indirizzo alla pagina di caricamento invito passando l'evento
    @GetMapping("/invite/upload/{id}")
    public String inviteUpload(Model model, @PathVariable Integer id) {
        model.addAttribute("event", eventRepository.findById(id).get());
        return "photo/uploadInvite";
    }

}
