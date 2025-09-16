package com.wedding.app.mywedding.Controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.wedding.app.mywedding.Model.Invited;
import com.wedding.app.mywedding.Repository.InvitedRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/invited")
public class InvitedController {

    @Autowired
    InvitedRepository invitedRepository;

    @GetMapping
    public String index(Model model) {
        Invited invited = new Invited();
        model.addAttribute("invited", invited);
        model.addAttribute("inviteds", invitedRepository.findAll());

        return "invited/index";
    }

    @PostMapping("/create")
    public String store(@Valid @ModelAttribute("invited") Invited formInvited, BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("showModal", true);
            model.addAttribute("inviteds", invitedRepository.findAll());
            return "invited/index";
        }

        invitedRepository.save(formInvited);
        return "redirect:/invited";

    }

    @PostMapping("delete/{id}")
    public String delete(@PathVariable Integer id) {
        Optional<Invited> invited = invitedRepository.findById(id);

        invitedRepository.delete(invited.get());
        return "redirect:/invited";
    }

    @GetMapping("edit/{id}")
    public String edit( @PathVariable Integer id,Model model) {
        model.addAttribute("invited", invitedRepository.findById(id).get());

        return "/invited/edit";

    }

    @PostMapping("edit/{id}")
    public String update(@PathVariable Integer id, @Valid @ModelAttribute("invited") Invited formInvited,
            BindingResult bindingResult,  Model model) {
        if (bindingResult.hasErrors()) {
            return "invited/edit";
        }

        invitedRepository.save(formInvited);
        return "redirect:/invited";

    }
}
