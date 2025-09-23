package com.wedding.app.mywedding.Controller;

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

import com.wedding.app.mywedding.Model.Invited;
import com.wedding.app.mywedding.Model.User;
import com.wedding.app.mywedding.Repository.InvitedRepository;
import com.wedding.app.mywedding.Repository.RoleRepository;
import com.wedding.app.mywedding.Repository.UserRepository;
import com.wedding.app.mywedding.Service.EmailService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/invited")
public class InvitedController {

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
    @GetMapping
    public String index(Model model, @RequestParam(required = false) String search, Authentication authentication) {
        Optional<User> userLogged = userRepository.findByEmail(authentication.getName());
        Invited invited = new Invited();
        model.addAttribute("invited", invited);
        List<Invited> inviteds = new ArrayList<>();
        for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
            if (grantedAuthority.getAuthority().equals("ADMIN")) {
                if (search != null && !search.isEmpty()) {
                    inviteds = invitedRepository.findByNameIgnoreCase(search);
                } else {
                    inviteds = invitedRepository.findAll();

                }
            } else if (grantedAuthority.getAuthority().equals("USER")) {
                List<Invited> userInvited = invitedRepository.findByUser(userLogged.get());
                for (Invited singleInvited : userInvited) {
                    inviteds.add(singleInvited);
                }

            }
        }

        model.addAttribute("inviteds", inviteds);

        return "invited/index";
    }

    // Chiamata post con validazione per creazione invitati
    @PostMapping("/create")
    public String store(@Valid @ModelAttribute("invited") Invited formInvited,
            BindingResult bindingResult, Model model, Authentication authentication) {
        Optional<User> userLogged = userRepository.findByEmail(authentication.getName());
        if (bindingResult.hasErrors()) {
            model.addAttribute("showModal", true);
            model.addAttribute("inviteds", invitedRepository.findAll());
            return "invited/index";
        }

        if (invitedRepository.existsByEmail(formInvited.getEmail())) {
            model.addAttribute("showModal", true);
            model.addAttribute("inviteds", invitedRepository.findAll());
            bindingResult.rejectValue("email", "error.invited", "Email già registrata da un altro utente");
            return "invited/index";

        }

        formInvited.setUser(userLogged.get());
        invitedRepository.save(formInvited);
        return "redirect:/invited";

    }

    @PostMapping("/email/send/{id}")
    public String emailSend(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Invited> invited = invitedRepository.findById(id);
        try {
            emailService.sendEmail(invited.get().getEmail(), invited.get());
        } catch (Exception e) {
            model.addAttribute("message", "Errore nell'invio: " + e);
        }
        redirectAttributes.addFlashAttribute("message", "Email inviata con successo!");
        return "redirect:/invited";
    }

    // Get per reindirizzare su modifica dati invitato
    @GetMapping("edit/{id}")
    public String edit(@PathVariable Integer id, Model model, Authentication authentication) {
        Optional<User> loggedUser = userRepository.findByEmail(authentication.getName());
        Optional<Invited> singleInvited = invitedRepository.findById(id);
        // Controllo che l'invitato si assegnato a questo utente (Se sei admin puoi
        // accedere sempre)
        for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
            if ((grantedAuthority.getAuthority().equals("ADMIN"))
                    || singleInvited.get().getUser() == loggedUser.get()) {
                model.addAttribute("invited", invitedRepository.findById(id).get());
            }
        }

        return "/invited/edit";

    }

    // Post per validare e modificare dati invitato
    @PostMapping("edit/{id}")
    public String update(@PathVariable Integer id, @Valid @ModelAttribute("invited") Invited formInvited,
            BindingResult bindingResult, Model model) {

        Invited invited = invitedRepository.findById(id).get();
        if (bindingResult.hasErrors()) {
            return "invited/edit";
        }

        formInvited.setUser(invited.getUser());
        invitedRepository.save(formInvited);
        return "redirect:/invited";

    }

    // Chiamata post per eliminazione invitati
    @PostMapping("delete/{id}")
    public String delete(@PathVariable Integer id, Authentication authentication) {
        Optional<Invited> singleInvited = invitedRepository.findById(id);
        Optional<User> loggedUser = userRepository.findByEmail(authentication.getName());
        for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
            if ((grantedAuthority.getAuthority().equals("ADMIN"))
                    || singleInvited.get().getUser() == loggedUser.get()) {
                invitedRepository.delete(singleInvited.get());

            }
        }

        return "redirect:/invited";
    }
}
