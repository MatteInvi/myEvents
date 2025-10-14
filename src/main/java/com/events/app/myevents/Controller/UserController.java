package com.events.app.myevents.Controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
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

import com.events.app.myevents.Model.Role;
import com.events.app.myevents.Model.User;
import com.events.app.myevents.Model.authToken;
import com.events.app.myevents.Repository.RoleRepository;
import com.events.app.myevents.Repository.TokenRepository;
import com.events.app.myevents.Repository.UserRepository;
import com.events.app.myevents.Service.EmailService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    EmailService emailService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TokenRepository tokenRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Value("${app.url}")
    private String appUrl;

    // Index user (per ADMIN)
    @GetMapping("/index")
    public String index(@RequestParam(required = false) String search, Authentication authentication, Model model) {
        for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
            if (grantedAuthority.getAuthority().equals("ADMIN")) {
                List<User> users = new ArrayList<>();
                if (search != null && !search.isEmpty()) {
                    users = userRepository.findByNameContainingIgnoreCase(search);
                } else {
                    users= userRepository.findAll();

                }
                model.addAttribute("users", users);
                return "utenti/index";
            }
        }
        model.addAttribute("message", "Non sei autorizzato a vedere questa pagina!");
        return "pages/message";

    }

    // Show user (per ADMIN)
    @GetMapping("/show/{id}")
    public String show(@PathVariable Integer id, Model model, Authentication authentication) {
        for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
            if (grantedAuthority.getAuthority().equals("ADMIN")) {
                model.addAttribute("user", userRepository.findById(id).get());
                return "utenti/info";
            }
        }
        model.addAttribute("message", "Non sei autorizzato a vedere questa pagina!");
        return "pages/message";
    }

    // info User loggato
    @GetMapping("/info")
    public String info(Model model, Authentication authentication) {
        Optional<User> utenteLoggato = userRepository.findByEmail(authentication.getName());

        model.addAttribute("user", utenteLoggato.get());

        return "utenti/info";
    }

    // Create User
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("user", new User());
        return "utenti/register";
    };

    // Chiamata post per salvare l'utente
    @PostMapping("/create")
    public String save(@Valid @ModelAttribute("user") User formUser, BindingResult bindingResult, Model model,
            RedirectAttributes redirectAttributes) {
        // Se la mail non è registrata nel db viene salvato l'utente senza verifica
        if (userRepository.existsByEmail(formUser.getEmail())) {
            bindingResult.rejectValue("email", "error.user", "Email già in uso");

            return "utenti/register";
        }

        if (bindingResult.hasErrors()) {
            return "utenti/register";
        }

        // Individuo e setto il ruolo di utente per tutti i nuovi registrati
        Role userRole = new Role();
        for (Role role : roleRepository.findAll()) {
            if (role.getNome().equals("USER")) {
                userRole = role;
            }
        }
        formUser.setRoles(Set.of(userRole));

        // Setto password Encoder e salvo utente nel db
        formUser.setPassword(passwordEncoder.encode(formUser.getPassword()));
        userRepository.save(formUser);

        // Generiamo un token di verifica settando i parametri dello stesso
        String token = UUID.randomUUID().toString();
        authToken authToken = new authToken();
        authToken.setToken(token);
        authToken.setUser(formUser);
        authToken.setExpireDate(LocalDateTime.now().plusHours(24));
        formUser.setAuthToken(authToken);

        // Salviamo il token nel db e restituiamo un invito a confermare la
        // registrazione
        tokenRepository.save(authToken);
        model.addAttribute("message", "Controllare la mail per confermare la registrazione!");

        // Inviamo mail all'utente passando i dati del form compilato(per recuperare la
        // mail) e il token generato
        try {
            emailService.registerEmail(formUser, authToken);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Errore nell'invio:" + e);
        }
        return "pages/message";

    }

    // Sezione di conferma registrazione
    @GetMapping("/confirm")
    public String confirmRegistration(@RequestParam String token, Model model, HttpServletRequest request,
            HttpServletResponse response) {

        // Andiamo a prendere il token dalla repository seguendo il link inviato
        // all'utente
        Optional<authToken> authToken = tokenRepository.findByToken(token);

        // Se non è scaduto(24h) passiamo a prendere l'utente associato al token e
        // settare il suo stato come verificato
        if (authToken.get().getExpireDate().isBefore(LocalDateTime.now())) {

            model.addAttribute("message", "Token scaduto!");
            return "pages/message";
        }

        User user = authToken.get().getUser();
        user.setVerified(true);

        // Setto il link da mandare all'utente da condividere per poter salvare le foto
        user.setLinkPhotoUpload(appUrl + "/photo/upload/" + user.getId());

        // Qui aggiorniamo l'utente nel db e resituiamo un messaggio di avvenuta
        // verifica
        userRepository.save(user);
        // Effettuo logout con messagio di avvenuta registrazione
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        model.addAttribute("confirm", "Registrazione confermata con successo!");
        return "pages/login";

    }

    // Edit user
    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable Integer id, Authentication authentication) {
        Optional<User> utenteOptional = userRepository.findByEmail(authentication.getName());
        Optional<User> singleUser = userRepository.findById(id);
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority.getAuthority().equals("ADMIN")) {
                model.addAttribute("user", singleUser.get());
                return "utenti/edit";
            } else if (authority.getAuthority().equals("USER")) {
                if (utenteOptional.get().equals(userRepository.findById(id).get())) {
                    model.addAttribute("user", singleUser.get());
                    return "utenti/edit";
                }
            }

        }
        model.addAttribute("message", "Non sei autorizzato a vedere questa pagina!");
        return "pages/message";

    }

    @PostMapping("/edit/{id}")
    public String update(@Valid @ModelAttribute("user") User userForm, BindingResult bindingResult,
            Authentication authentication) {
        Optional<User> utenteLoggato = userRepository.findByEmail(authentication.getName());

        if (bindingResult.hasErrors()) {
            return "utenti/edit";
        }

        // Settaggio informazioni non modificabili
        userForm.setLinkInvite(utenteLoggato.get().getLinkInvite());
        userForm.setVerified(utenteLoggato.get().getVerified());
        userForm.setLinkPhotoUpload(utenteLoggato.get().getLinkPhotoUpload());
        userForm.setRoles(utenteLoggato.get().getRoles());
        userForm.setPassword(passwordEncoder.encode(userForm.getPassword()));
        userRepository.save(userForm);
        return "utenti/info";
    }

}
