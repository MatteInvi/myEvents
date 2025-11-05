package com.events.app.myevents.Controller.API;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

//Lista invitati per evento   
    @GetMapping("/index/{idEvent}")
    public ResponseEntity<List<Invited>> index(@PathVariable Integer idEvent){
        List<Invited> listInvited = eventRepository.findById(idEvent).get().getInviteds();
        if (listInvited.size() > 0){
            return new ResponseEntity<>(listInvited, HttpStatus.OK);
        }

        return new ResponseEntity<>(listInvited, HttpStatus.NO_CONTENT);
        
    }

//Creazione nuovo invitato
    @PostMapping("/create")
    public ResponseEntity<Invited> create(@Valid @RequestBody Invited invited, @PathVariable Integer id){
        Optional<User> singleUser = userRepository.findById(id);
        if (singleUser.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        invitedRepository.save(invited);
        return new ResponseEntity<>(invited, HttpStatus.OK);
    }    

    




}
