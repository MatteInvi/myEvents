package com.wedding.app.mywedding.Controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.wedding.app.mywedding.Model.Invited;
import com.wedding.app.mywedding.Model.Role;
import com.wedding.app.mywedding.Model.User;
import com.wedding.app.mywedding.Repository.InvitedRepository;
import com.wedding.app.mywedding.Repository.RoleRepository;
import com.wedding.app.mywedding.Repository.UserRepository;
import com.wedding.app.mywedding.Utility.PasswordGenerator;
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
    public String index(Model model, @RequestParam(required = false) String search) {
        Invited invited = new Invited();
        model.addAttribute("invited", invited);
        List<Invited> inviteds = new ArrayList<>();
        if (search != null && !search.isEmpty()) {
            inviteds = invitedRepository.findByNameIgnoreCase(search);
        } else {
            inviteds = invitedRepository.findAll();
            model.addAttribute("inviteds", inviteds);
        }

        return "invited/index";
    }

    // Chiamata post con validazione per creazione invitati
    @PostMapping("/create")
    public String store(@Valid @ModelAttribute("invited") Invited formInvited,
            BindingResult bindingResult, @ModelAttribute("users") User formUser,
            Model model) {

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

        Set<Role> roles = new HashSet<>();
        for (Role role : roleRepository.findAll()) {
            if (role.getId() == 2) {
                roles.add(role);
            }
        }
        formUser.setUsername(formInvited.getEmail());
        formUser.setPassword(PasswordGenerator.generateRandomPassword(8));
        formUser.setRoles(roles);

        // Invio mail con destinatario (da form) e dati per accesso
        emailService.sendEmail(formInvited.email, formUser);           
        
        userRepository.save(formUser);
        invitedRepository.save(formInvited);
        return "redirect:/invited";

    }

    // Chiamata post per eliminazione invitati
    @PostMapping("delete/{id}")
    public String delete(@PathVariable Integer id) {
        Optional<Invited> invited = invitedRepository.findById(id);

        invitedRepository.delete(invited.get());
        return "redirect:/invited";
    }

    // Get per reindirizzare su modifica dati invitato
    @GetMapping("edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("invited", invitedRepository.findById(id).get());

        return "/invited/edit";

    }

    // Get per far editare lo stato al singolo utente
    @GetMapping("editStato/{id}")
    public String editStato(@PathVariable Integer id, Model model) {
        model.addAttribute("invited", invitedRepository.findById(id).get());

        return "invited/editStato";
    }

    // Post per modificare il solo stato
    @PostMapping("editStato/{id}")
    public String updateStato(@PathVariable Integer id, @Valid @ModelAttribute("invited") Invited formInvited,
            BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            return "invited/editStato";
        }

        model.addAttribute("success", "Stato modificato con successo");
        invitedRepository.save(formInvited);

        return "invited/editStato";
    }

    // Post per validare e modificare dati invitato
    @PostMapping("edit/{id}")
    public String update(@PathVariable Integer id, @Valid @ModelAttribute("invited") Invited formInvited,
            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "invited/edit";
        }

        invitedRepository.save(formInvited);
        return "redirect:/invited";

    }
}
