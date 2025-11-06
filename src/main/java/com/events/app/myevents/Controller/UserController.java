package com.events.app.myevents.Controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.events.app.myevents.Model.Role;
import com.events.app.myevents.Model.User;
import com.events.app.myevents.Model.authToken;
import com.events.app.myevents.Repository.PasswordResetTokenRepository;
import com.events.app.myevents.Repository.RoleRepository;
import com.events.app.myevents.Repository.TokenRepository;
import com.events.app.myevents.Repository.UserRepository;
import com.events.app.myevents.Service.CloudinaryService;
import com.events.app.myevents.Service.EmailService;
import com.events.app.myevents.Service.PasswordResetService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {

    // Dichiarazione piattaforma su cui salvare foto

    @Autowired
    Cloudinary cloudinary;

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

    @Autowired
    PasswordResetService passwordResetService;

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    CloudinaryService cloudinaryService;

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
                    users = userRepository.findAll();

                }
                model.addAttribute("users", users);
                return "utenti/index";
            }
        }

        model.addAttribute("message", "Non sei autorizzato a vedere questa pagina!");
        return "pages/message";

    }

    // Show user
    @GetMapping("/show/{id}")
    public String show(@PathVariable Integer id, Model model, Authentication authentication) {

        Optional<User> utenteLoggato = userRepository.findByEmail(authentication.getName());
        Optional<User> utenteOptional = userRepository.findById(id);
        for (GrantedAuthority auth : authentication.getAuthorities()) {
            if (auth.getAuthority().equals("ADMIN")
                    || (auth.getAuthority().equals("USER") && utenteOptional.get().equals(utenteLoggato.get()))) {
                model.addAttribute("user", userRepository.findById(id).get());
                return "utenti/info";
            }
        }

        model.addAttribute("message", "Non sei autorizzato a vedere questa pagina!");
        return "pages/message";
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
        if (!formUser.getPassword().equals(formUser.getConfirmPassword())) {
            bindingResult.rejectValue("password", "error.user", "Le password non coincidono");
            bindingResult.rejectValue("confirmPassword", "error.user", "Le password non coincidono");
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
        formUser.setConfirmPassword(passwordEncoder.encode(formUser.getConfirmPassword()));
        userRepository.save(formUser);

        // Generiamo un token di verifica settando i parametri dello stesso e salviamo
        // nel db
        String token = UUID.randomUUID().toString();
        authToken authToken = new authToken();
        authToken.setToken(token);
        authToken.setUser(formUser);
        authToken.setExpireDate(LocalDateTime.now().plusHours(24));
        formUser.setAuthToken(authToken);
        tokenRepository.save(authToken);

        // Inviamo mail all'utente passando i dati del form compilato(per recuperare la
        // mail) e il token generato
        try {
            emailService.registerEmail(formUser, authToken);
            model.addAttribute("message", "Controllare la mail per confermare la registrazione!");
        } catch (Exception e) {
            model.addAttribute("message", "Errore nell'invio: " + e);
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
            Authentication authentication, @PathVariable Integer id, Model model) {
        Optional<User> utenteLoggato = userRepository.findByEmail(authentication.getName());
        Optional<User> utenteOptional = userRepository.findById(id);

        if (!userForm.getPassword().equals(userForm.getConfirmPassword())) {
            bindingResult.rejectValue("password", "error.user", "Le password non coincidono");
            bindingResult.rejectValue("confirmPassword", "error.user", "Le password non coincidono");
            return "utenti/edit";
        }

        if (userRepository.existsByEmailAndIdNot(userForm.getEmail(), userForm.getId())) {
            bindingResult.rejectValue("email", "error.user", "Email già in uso");
            return "utenti/edit";
        }

        if (bindingResult.hasErrors()) {
            return "utenti/edit";
        }

        for (GrantedAuthority auth : authentication.getAuthorities()) {
            if (auth.getAuthority().equals("ADMIN")
                    || (auth.getAuthority().equals("USER") && utenteLoggato.get().equals(utenteOptional.get()))) {

                // Settaggio informazioni non modificabili
                userForm.setLinkProfilePhoto(utenteOptional.get().getLinkProfilePhoto());
                userForm.setVerified(utenteOptional.get().getVerified());
                userForm.setRoles(utenteOptional.get().getRoles());
                userForm.setPassword(passwordEncoder.encode(utenteOptional.get().getPassword()));
                userForm.setConfirmPassword(passwordEncoder.encode(userForm.getConfirmPassword()));
                userRepository.save(userForm);
                return "redirect:/user/show/" + utenteOptional.get().getId();

            }

        }
        model.addAttribute("message", "Non sei autorizzato a effettuare questa operazione");
        return "pages/message";

    }

    // Modifica foto profilo
    @PostMapping("/profilePhoto/{id}")
    public String profilePhotoUpdate(@RequestParam MultipartFile file, RedirectAttributes redirectAttributes,
            Authentication authentication, @PathVariable Integer id, Model model) {
        Optional<User> utenteOptional = userRepository.findById(id);
        Optional<User> utenteLoggato = userRepository.findByEmail(authentication.getName());

        for (GrantedAuthority auth : authentication.getAuthorities()) {
            if (auth.getAuthority().equals("ADMIN")
                    || (auth.getAuthority().equals("USER") && utenteOptional.get().equals(utenteLoggato.get()))) {

                try {
                    if (file.isEmpty()) {
                        redirectAttributes.addFlashAttribute("error", "Nessun file selezionato");
                        redirectAttributes.addFlashAttribute("user", utenteOptional.get());
                        return "redirect:/user/show/" + utenteOptional.get().getId();
                    }

                    // Elimino foto precedente se esiste
                    String imageUrl = utenteOptional.get().getLinkProfilePhoto();
                    if (imageUrl != null) {
                        cloudinaryService.deleteByUrl(imageUrl);
                    }

                    // Carico il file su Cloudinary
                    Map uploadResult = cloudinary.uploader().upload(
                            file.getBytes(),
                            ObjectUtils.asMap("folder", "myEventsPhoto/profile/" + utenteOptional.get().getId()));

                    // Info del file caricato
                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("url", uploadResult.get("secure_url"));
                    fileInfo.put("publicId", uploadResult.get("public_id"));
                    fileInfo.put("name", file.getOriginalFilename());

                    // Aggiungo al model per la view
                    redirectAttributes.addFlashAttribute("success", "Caricamento avvenuto con successo!");
                    redirectAttributes.addFlashAttribute("user", utenteOptional.get());

                    utenteOptional.get().setLinkProfilePhoto(uploadResult.get("secure_url").toString());
                    userRepository.save(utenteOptional.get());

                } catch (IOException e) {
                    redirectAttributes.addFlashAttribute("error", "Errore durante il caricamento: " + e.getMessage());
                    redirectAttributes.addFlashAttribute("user", utenteOptional.get());
                    return "redirect:/user/show/" + utenteOptional.get().getId();
                }
                return "redirect:/user/show/" + utenteOptional.get().getId();
            }

        }

        model.addAttribute("message", "Non sei autorizzato a effettuare questa operazione");
        return "pages/message";

    }

    // Recupero password
    @GetMapping("/passwordRecovery")
    String passwordRecovery() {
        return "utenti/passwordRecovery";
    }

    @PostMapping("/passwordRecovery")
    String passwordRecoverySend(@RequestParam String email, RedirectAttributes redirectAttributes) {
        passwordResetService.sendResetLink(email);
        redirectAttributes.addFlashAttribute("message",
                "Se l'email è registrata, riceverai un link per reimpostare la password.");
        return "redirect:/user/passwordRecovery";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        var resetToken = passwordResetTokenRepository.findByToken(token)
                .orElse(null);
        if (resetToken == null || resetToken.isExpired()) {
            model.addAttribute("error", "Token non valido o scaduto.");
            return "utenti/reset-password"; // templates/reset-password-error.html
        }

        model.addAttribute("token", token);
        return "utenti/reset-password"; // templates/reset-password.html
    }

    // Gestisce il submit del form per impostare la nuova password
    @PostMapping("/reset-password")
    public String processResetPassword(
            @RequestParam("token") String token,
            @RequestParam("password") String password,
            Model model) {

        try {
            passwordResetService.resetPassword(token, password);
            model.addAttribute("message", "Password aggiornata con successo!");
            return "utenti/reset-password"; // templates/reset-password-success.html
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "utenti/reset-password";
        }
    }

    
}
