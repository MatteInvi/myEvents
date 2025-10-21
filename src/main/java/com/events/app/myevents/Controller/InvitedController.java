package com.events.app.myevents.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.events.app.myevents.Model.Event;
import com.events.app.myevents.Model.Invited;
import com.events.app.myevents.Model.User;
import com.events.app.myevents.Repository.EventRepository;
import com.events.app.myevents.Repository.InvitedRepository;
import com.events.app.myevents.Repository.RoleRepository;
import com.events.app.myevents.Repository.UserRepository;
import com.events.app.myevents.Service.EmailService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/invited")
public class InvitedController {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    EmailService emailService;

    @Autowired
    InvitedRepository invitedRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    // Indice invitati con creazione nuovo inviato nel momento in cui si prema il
    // bottone in pagina
    @GetMapping("/index/{id}")
    public String index(Model model, @RequestParam(required = false) String search, @PathVariable Integer id,
            Authentication authentication) {
        Optional<User> userLogged = userRepository.findByEmail(authentication.getName());
        Optional<Event> eventOptional = eventRepository.findById(id);

        List<Invited> inviteds = new ArrayList<>();
        for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
            if (grantedAuthority.getAuthority().equals("ADMIN")) {
                if (search != null && !search.isEmpty()) {
                    inviteds = invitedRepository.findByEventAndNameContainingIgnoreCase(eventOptional.get(), search);
                } else {
                    inviteds = invitedRepository.findByEvent(eventRepository.findById(id).get());

                }
            } else if (grantedAuthority.getAuthority().equals("USER")
                    && eventOptional.get().getUser().equals(userLogged.get())) {
                inviteds = invitedRepository.findByUserAndEvent(userLogged.get(), eventOptional.get());
                if (search != null && !search.isEmpty()) {
                    inviteds = invitedRepository.findByUserAndEventAndNameContainingIgnoreCase(userLogged.get(),
                            eventOptional.get(), search);
                }

            }
        }

        model.addAttribute("event", eventOptional.get());
        model.addAttribute("inviteds", inviteds);
        return "invited/index";
    }

    // Indirizzio alla pagina di creazione
    @GetMapping("/create/{id}")
    public String create(Model model, @PathVariable Integer id, Authentication authentication) {
        Optional<Event> eventOptional = eventRepository.findById(id);
        Optional<User> userLogged = userRepository.findByEmail(authentication.getName());

        for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
            if (grantedAuthority.getAuthority().equals("ADMIN") || (grantedAuthority.getAuthority().equals("USER")
                    && eventOptional.get().getUser().equals(userLogged.get()))) {
                Invited invited = new Invited();
                model.addAttribute("invited", invited);
                model.addAttribute("event", eventOptional.get());
                return "invited/create";

            }
        }

        model.addAttribute("message", "Non puoi accedere a questa pagina");
        return "pages/message";

    }

    // Chiamata post con validazione per creazione invitati
    @PostMapping("/create/{id}")
    public String store(@Valid @ModelAttribute("invited") Invited formInvited,
            BindingResult bindingResult, Model model, Authentication authentication, @PathVariable Integer id) {

        Optional<User> userLogged = userRepository.findByEmail(authentication.getName());
        Optional<Event> eventOptional = eventRepository.findById(id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("event", eventOptional.get());
            return "invited/create";
        }



        formInvited.setId(null);
        formInvited.setUser(userLogged.get());
        formInvited.setEvent(eventOptional.get());
        invitedRepository.save(formInvited);
        return "redirect:/invited/index/" + eventOptional.get().getId();

    }

    // Invio email con l'invito
    @PostMapping("/email/send/{idInvited}/{idEvent}")
    public String emailSend(@PathVariable Integer idInvited, @PathVariable Integer idEvent, Model model,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
                
        // Prendo i dati dell'invitato per mandare la mail personalizzata
        Optional<Invited> invited = invitedRepository.findById(idInvited);

        // Prendo i dati dell'evento per mandare i dati giusti
        Optional<Event> event = eventRepository.findById(idEvent);

        // Invio email passando al service: invitato(per estrapolare i dati) e link
        // dell'immagine invito per l'utente loggato
        try {
            emailService.inviteEmail(invited.get(), event.get().getLinkInvite());
        } catch (Exception e) {
            model.addAttribute("message", "Errore nell'invio: " + e);
        }
        redirectAttributes.addFlashAttribute("message", "Email inviata con successo!");
        return "redirect:/invited/index/" + event.get().getId();
    }

    // Get per reindirizzare su modifica dati invitato
    @GetMapping("edit/{idInvited}/{idEvent}")
    public String edit(@PathVariable Integer idInvited, @PathVariable Integer idEvent, Model model,
            Authentication authentication) {
        Optional<User> loggedUser = userRepository.findByEmail(authentication.getName());
        Optional<Invited> singleInvited = invitedRepository.findById(idInvited);
        Optional<Event> singleEvent = eventRepository.findById(idEvent);

        // Controllo che l'invitato si assegnato a questo utente (Se sei admin puoi
        // accedere sempre)
        for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
            if ((grantedAuthority.getAuthority().equals("ADMIN"))
                    || singleInvited.get().getUser() == loggedUser.get()) {
                model.addAttribute("invited", singleInvited.get());
                model.addAttribute("event", singleEvent.get());
            }
        }

        return "invited/edit";

    }

    // Post per validare e modificare dati invitato
    @PostMapping("edit/{idInvited}/{idEvent}")
    public String update(@Valid @ModelAttribute("invited") Invited formInvited,
            BindingResult bindingResult, Model model, @PathVariable Integer idInvited, @PathVariable Integer idEvent) {

        Invited invited = invitedRepository.findById(idInvited).get();
        Event event = eventRepository.findById(idEvent).get();

        if (bindingResult.hasErrors()) {
            model.addAttribute("event", event);
            return "invited/edit";
        }

        formInvited.setEvent(invited.getEvent());
        formInvited.setUser(invited.getUser());
        invitedRepository.save(formInvited);
        return "redirect:/invited/index/" + event.getId();

    }

    // Chiamata post per eliminazione invitati
    @PostMapping("delete/{idInvited}/{idEvent}")
    public String delete(@PathVariable Integer idInvited, @PathVariable Integer idEvent,
            Authentication authentication) {
        Optional<Invited> singleInvited = invitedRepository.findById(idInvited);
        Optional<Event> singleEvent = eventRepository.findById(idEvent);
        Optional<User> loggedUser = userRepository.findByEmail(authentication.getName());

        for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
            if ((grantedAuthority.getAuthority().equals("ADMIN"))
                    || singleInvited.get().getUser() == loggedUser.get()) {
                invitedRepository.delete(singleInvited.get());

            }
        }

        return "redirect:/invited/index/" + singleEvent.get().getId();
    }
}
