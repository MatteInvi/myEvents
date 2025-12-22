package com.events.app.myevents.Controller.API;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.events.app.myevents.DTO.AuthResponse;
import com.events.app.myevents.DTO.LoginRequest;
import com.events.app.myevents.Model.Role;
import com.events.app.myevents.Model.User;
import com.events.app.myevents.Repository.RoleRepository;
import com.events.app.myevents.Repository.UserRepository;
import com.events.app.myevents.Service.JwtService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final PasswordEncoder passwordEncoder;


    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RoleRepository roleRepository;


    AuthController(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }





    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginRequest loginRequest) {
        // Implementazione del metodo di login
        Map<String, String> response = new HashMap<>();
        try{
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));

        } catch (BadCredentialsException e) {
            response.put("error", "Incorrect username or password");
            return ResponseEntity.status(401).body(response);
        }
        
        //Se l'autenticazione è andata a buon fine, controlla se l'email è verificata
        if (!userRepository.findByEmail(loginRequest.username()).get().isVerified()) {
            response.put("error", "Email not verified");
            return ResponseEntity.status(403).body(response);
        }

        //Se la mail è verificata carica l'utente
        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(loginRequest.username());

          //Generazione token JWT
          final String jwt = jwtService.generateToken(userDetails);

          //Restituzione del token
          return ResponseEntity.ok(new AuthResponse(jwt));

    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User registerRequest, BindingResult bindingResult){
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity
                    .status(400)
                    .body("Error: Email is already in use!");
        }

        if(bindingResult.hasErrors()){
            return ResponseEntity
                    .badRequest()
                    .body("Error: Invalid registration data!");
        }

        Role roleUser = new Role();
        for (Role role: roleRepository.findAll()){
            if (role.getNome().equals("USER")){
                roleUser = role;
            }
        }

        registerRequest.setRoles(Set.of(roleUser));
        registerRequest.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        userRepository.save(registerRequest);

        return ResponseEntity.ok("User registered successfully!");
    }
}
