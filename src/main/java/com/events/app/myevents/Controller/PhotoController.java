package com.events.app.myevents.Controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.events.app.myevents.Model.Event;
import com.events.app.myevents.Model.User;
import com.events.app.myevents.Repository.EventRepository;
import com.events.app.myevents.Repository.UserRepository;
import com.events.app.myevents.Service.CloudinaryService;

@Controller
@RequestMapping("/photo")
public class PhotoController {

    // Dichiarazione piattaforma su cui salvare foto
    @Autowired
    Cloudinary cloudinary;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CloudinaryService cloudinaryService;

    // Caricamento foto invito (id evento)
    @PostMapping("/upload/invite/{id}")
    public String inviteUpload(@RequestParam MultipartFile file, Model model, @PathVariable Integer id,
            Authentication authentication) {

        Optional<User> utenteLoggato = userRepository.findByEmail(authentication.getName());
        Optional<Event> eventOptional = eventRepository.findById(id);

        for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
            if ((grantedAuthority.getAuthority().equals("ADMIN"))
                    || (grantedAuthority.getAuthority().equals("USER")
                            && eventOptional.get().getUser().equals(utenteLoggato.get()))) {
                try {
                    if (file.isEmpty()) {
                        model.addAttribute("error", "Nessun file selezionato");
                        model.addAttribute("event", eventOptional.get());
                        return "photo/uploadInvite";
                    }

                    // Carico il file su Cloudinary
                    Map uploadResult = cloudinary.uploader().upload(
                            file.getBytes(),
                            ObjectUtils.asMap("folder", "myEventsPhoto/invite/" + "user=" + utenteLoggato.get().getId()
                                    + "event=" + eventOptional.get().getId()));

                    // Info del file caricato
                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("url", uploadResult.get("secure_url"));
                    fileInfo.put("publicId", uploadResult.get("public_id"));
                    fileInfo.put("name", file.getOriginalFilename());

                    // Aggiungo al model per la view
                    model.addAttribute("success", "Caricamento avvenuto con successo!");
                    model.addAttribute("uploadedFile", fileInfo);
                    model.addAttribute("event", eventOptional.get());

                    eventOptional.get().setLinkInvite(uploadResult.get("secure_url").toString());
                    eventRepository.save(eventOptional.get());

                } catch (IOException e) {
                    model.addAttribute("error", "Errore durante il caricamento: " + e.getMessage());
                    model.addAttribute("user", utenteLoggato.get());
                    return "photo/uploadInvite";
                }

                return "photo/uploadInvite";
            }
        }
        model.addAttribute("message", "Non sei autorizzato ad accedere a questa pagina");
        return "pages/message";

    }

    // Carico foto evento

    // Manda l'upload all'id che si è messo nell'indirizzo
    @GetMapping("/upload/{id}")
    public String photo(@PathVariable Integer id, Model model) {
        Optional<Event> eventOptional = eventRepository.findById(id);
        model.addAttribute("event", eventOptional.get());
        return "photo/uploadPhotos";
    }

    // Chiamata post per caricare foto
    @PostMapping("/upload/{id}")
    public String upload(@RequestParam MultipartFile[] files, Model model, @PathVariable Integer id) {

        Optional<Event> eventOptional = eventRepository.findById(id);
        try {
            // Creazione array per selezione di foto multiple, controllo che la selezione
            // non sia vuota
            // e infine carico foto sulla piattaforma
            List<Map<String, Object>> uploadedFiles = new ArrayList<>();

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                            ObjectUtils.asMap("folder",
                                    "myEventsPhoto/photos/" + "user/" + eventOptional.get().getUser().getId()
                                            + "/event/" + eventOptional.get().getId()));

                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("url", uploadResult.get("secure_url"));
                    fileInfo.put("publicId", uploadResult.get("public_id"));
                    fileInfo.put("name", file.getOriginalFilename());

                    uploadedFiles.add(fileInfo);
                }

            }

            // Restituisco messaggio di avvenuto caricamento con i relativi dati
            // altrimenti restituisco errore se non sono state selezionate foto
            if (uploadedFiles.size() > 0) {
                model.addAttribute("success", "Caricamento avvenuto con successo!");
                model.addAttribute("uploadedFiles", uploadedFiles);
                model.addAttribute("event", eventOptional.get());
                return "photo/uploadPhotos";
            } else {
                model.addAttribute("error", "Nessun file selezionato");
                model.addAttribute("event", eventOptional.get());
                return "photo/uploadPhotos";

            }

            // Se il blocco precedente non viene caricare viene mostrato un errore con il
            // relativo messaggio
        } catch (IOException e) {
            model.addAttribute("error", "Errore caricamento " + e.getMessage());

        }
        return "photo/uploadPhotos";

    }

    // Galleria foto caricaate

    @GetMapping("/gallery/{id}")
    public String showGallery(Model model, Authentication authentication, @PathVariable Integer id) throws Exception {
        Optional<User> utenteLoggato = userRepository.findByEmail(authentication.getName());
        Optional<Event> eventOptional = eventRepository.findById(id);

        for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
            if ((grantedAuthority.getAuthority().equals("ADMIN"))
                    || (grantedAuthority.getAuthority().equals("USER")
                            && eventOptional.get().getUser().equals(utenteLoggato.get()))) {

                String folderUrl = "myEventsPhoto/photos/user/" + eventOptional.get().getUser().getId() + "/event/"
                        + eventOptional.get().getId();
                Map result = cloudinary.search()
                        .expression("folder:" + folderUrl)
                        .maxResults(30)
                        .execute();

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> resources = (List<Map<String, Object>>) result.get("resources");
                List<String> imageUrls = resources.stream()
                        .map(r -> (String) r.get("secure_url"))
                        .toList();

                model.addAttribute("images", imageUrls);
                return "photo/gallery";
            }
        }

        model.addAttribute("message", "Pagina non trovata");
        return "pages/message";
    }

    @PostMapping("/deleteImage")
    public String deleteImage(@RequestParam("url") String imageUrl, Model model) throws IOException {
        try {
            cloudinaryService.deleteByUrl(imageUrl);
        } catch (IOException e) {
            model.addAttribute("message", "Errore nell'eliminazione: " + e);
            return "pages/message";
        }
        return "redirect:/event";

    }

}
