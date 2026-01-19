package com.events.app.myevents.Controller.API;


import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.events.app.myevents.Model.Event;
import com.events.app.myevents.Model.Invited;
import com.events.app.myevents.Model.User;
import com.events.app.myevents.Repository.EventRepository;
import com.events.app.myevents.Repository.InvitedRepository;
import com.events.app.myevents.Repository.RoleRepository;
import com.events.app.myevents.Repository.UserRepository;
import com.events.app.myevents.Service.EmailService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/invited")
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500", "http://localhost:5173"})
public class InvitedRestController {
    
   @Autowired
    EmailService emailService;

    @Autowired
    InvitedRepository invitedRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EventRepository eventRepository;


//Creazione nuovo invitato
    @PostMapping("/create/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER_VERIFIED')")
    public ResponseEntity<Invited> create(@Valid @RequestBody Invited invited, @PathVariable Integer id, Authentication authentication) {
        Optional<User> loggedUser = userRepository.findByEmail(authentication.getName()); 
        Optional<Event> event = eventRepository.findById(id);       

        if (loggedUser.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if (event.get().getUser().getId() != loggedUser.get().getId()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        invited.setUser(loggedUser.get());
        invited.setEvent(eventRepository.findById(id).get());
        invitedRepository.save(invited);
        return new ResponseEntity<>(invited, HttpStatus.OK);
    }    

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER_VERIFIED')") 
    public ResponseEntity<Invited> updateInvited(@PathVariable Integer id, @Valid @RequestBody Invited invitedDetails,BindingResult bindingResult, Authentication authentication) {
        Optional<User> loggedUser = userRepository.findByEmail(authentication.getName());
        Optional<Invited> invitedOptional = invitedRepository.findById(id);

        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (loggedUser.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if (invitedOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Invited invited = invitedOptional.get();
        if (invited.getUser().getId() != loggedUser.get().getId()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        invited.setName(invitedDetails.getName());
        invited.setSurname(invitedDetails.getSurname());
        invited.setEmail(invitedDetails.getEmail());
        invited.setStatus(invitedDetails.getStatus());
        invited.setAnnotation(invitedDetails.getAnnotation());

        invitedRepository.save(invited);
        return new ResponseEntity<>(invited, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER_VERIFIED')")
    public ResponseEntity<Void> deleteInvited(@PathVariable Integer id, Authentication authentication) {
        Optional<User> loggedUser = userRepository.findByEmail(authentication.getName());
        Optional<Invited> invitedOptional = invitedRepository.findById(id);

        if (loggedUser.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if (invitedOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Invited invited = invitedOptional.get();
        if (invited.getUser().getId() != loggedUser.get().getId()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        invitedRepository.delete(invited);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    




}
