package com.events.app.myevents.Controller.API;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.events.app.myevents.Model.Event;
import com.events.app.myevents.Model.User;
import com.events.app.myevents.Repository.EventRepository;
import com.events.app.myevents.Repository.UserRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/events")
public class EventControllerAPI {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    UserRepository userRepository;

    // Recupera tutti gli eventi dell'utente loggato
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER_VERIFIED')")
    public ResponseEntity<List<Event>> getAllEvents(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        Optional<User> userLogged = userRepository.findByEmail(authentication.getName());
        List<Event> events = eventRepository.findByUser(userLogged.get());
        return ResponseEntity.ok(events);
    }

    // Recupera evento per ID
    @GetMapping("{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER_VERIFIED')")
    public ResponseEntity<?> getEventById(@PathVariable Integer id, Authentication authentication) {
        Optional<User> userLogged = userRepository.findByEmail(authentication.getName());
        Optional<Event> event = eventRepository.findById(id);
        if (event.isPresent() && !event.get().getUser().getId().equals(userLogged.get().getId())) {
            return ResponseEntity.status(403).body("Non sei autorizzato a visualizzare questo evento"); // Forbidden
        }

        return event.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Creazione nuovo evento
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER_VERIFIED')")
    public ResponseEntity<?> createEvent(@Valid @RequestBody Event event, BindingResult bindingResult,
            Authentication authentication) {
        Optional<User> userLogged = userRepository.findByEmail(authentication.getName());

        Map<String, String> response = new HashMap<>();

        if (bindingResult.hasErrors()) {
            response.put("error", "Dati evento non validi!");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            event.setUser(userLogged.get());
        } catch (Exception e) {
            response.put("error", "Utente non trovato!");
            return ResponseEntity.status(404).body(response);
        }

        try {
            eventRepository.save(event);
        } catch (Exception e) {
            response.put("error", "Errore durante la creazione dell'evento!");
            return ResponseEntity.status(500).body(response);
        }

        return ResponseEntity.ok(event);
    }

    // Aggiornamento evento
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER_VERIFIED')")
    public ResponseEntity<?> updateEvent(@PathVariable Integer id, @Valid @RequestBody Event event,
            Authentication authentication, BindingResult bindingResult) {
        Optional<User> userLogged = userRepository.findByEmail(authentication.getName());
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(400).build();
        }
        Optional<Event> eventToUpdate = eventRepository.findById(id);
        if (eventToUpdate.isPresent() && !eventToUpdate.get().getUser().getId().equals(userLogged.get().getId())) {
            return ResponseEntity.status(403).body("Non sei autorizzato a modificare questo evento");
        }
        if (eventToUpdate.isPresent()) {
            Event updatedEvent = eventToUpdate.get();
            updatedEvent.setName(event.getName());
            updatedEvent.setDescription(event.getDescription());
            updatedEvent.setDate(event.getDate());
            updatedEvent.setLinkEventPhotos(event.getLinkEventPhotos());
            updatedEvent.setLinkInvite(event.getLinkInvite());
            eventRepository.save(updatedEvent);
            return ResponseEntity.ok(updatedEvent);
        }
        return ResponseEntity.notFound().build();
    }

    // Eliminazione evento
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER_VERIFIED')")
    public ResponseEntity<?> deleteEvent(@PathVariable Integer id, Authentication authentication) {
        Optional<User> userLogged = userRepository.findByEmail(authentication.getName());
        Optional<Event> eventToDelete = eventRepository.findById(id);
        if (eventToDelete.isPresent() && !eventToDelete.get().getUser().getId().equals(userLogged.get().getId())) {
            return ResponseEntity.status(403).body("Non sei autorizzato a eliminare questo evento");
        }
        if (eventToDelete.isPresent()) {
            eventRepository.delete(eventToDelete.get());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}