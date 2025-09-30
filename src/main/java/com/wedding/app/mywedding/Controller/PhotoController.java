package com.wedding.app.mywedding.Controller;

import java.io.IOException;
import java.lang.classfile.ClassFile.Option;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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
import com.wedding.app.mywedding.Model.User;
import com.wedding.app.mywedding.Repository.UserRepository;

@Controller
@RequestMapping("/photo")
public class PhotoController {

    // Dichiarazione piattaforma su cui salvare foto
    private final Cloudinary cloudinary;

    public PhotoController(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Autowired
    UserRepository userRepository;

    // Caricamento foto invito
    @GetMapping("/upload/invite")
    public String invitePhoto(Model model, Authentication authentication) {
        Optional<User> utenteLoggato = userRepository.findByEmail(authentication.getName());
        model.addAttribute("userID", utenteLoggato.get().getId());
        return "photo/uploadInvite";
    }

    @PostMapping("/upload/invite/{id}")
    public String inviteUpload(@RequestParam MultipartFile file, Model model, @PathVariable Integer id,
            Authentication authentication) {

        Optional<User> utenteLoggato = userRepository.findByEmail(authentication.getName());
        try {
            if (file.isEmpty()) {
                model.addAttribute("error", "Nessun file selezionato");
                model.addAttribute("userID", id);
                return "photo/uploadInvite";
            }

            // Carico il file su Cloudinary
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", "myWeddingPhoto/invite/" + id));

            // Info del file caricato
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("url", uploadResult.get("secure_url"));
            fileInfo.put("publicId", uploadResult.get("public_id"));
            fileInfo.put("name", file.getOriginalFilename());

            // Aggiungo al model per la view
            model.addAttribute("success", "Caricamento avvenuto con successo!");
            model.addAttribute("uploadedFile", fileInfo);
            model.addAttribute("userID", id);

            utenteLoggato.get().setLinkInvite(uploadResult.get("secure_url").toString());
            userRepository.save(utenteLoggato.get());

        } catch (IOException e) {
            model.addAttribute("error", "Errore durante il caricamento: " + e.getMessage());
            model.addAttribute("userID", id);
        }

        return "photo/uploadInvite";

    }

    // Carico foto evento

    // In caso si entri con utente loggato in automatico manda all'upload su l'id
    // dell'utente
    @GetMapping("/upload")
    public String userPhoto(Model model, Authentication authentication) {
        Optional<User> utenteLoggato = userRepository.findByEmail(authentication.getName());
        model.addAttribute("userID", utenteLoggato.get().getId());
        return "photo/uploadPhotos";
    }

    // Manda l'upload all'id che si è messo nell'indirizzo
    @GetMapping("/upload/{id}")
    public String photo(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("userID", id);
        return "photo/uploadPhotos";
    }

    // Chiamata post per caricare foto
    @PostMapping("/upload/{id}")
    public String upload(@RequestParam MultipartFile[] files, Model model, @PathVariable Integer id) {

        try {
            // Creazione array per selezione di foto multiple, controllo che la selezione
            // non sia vuota
            // e in fine carico foto sulla piattaforma
            List<Map<String, Object>> uploadedFiles = new ArrayList<>();

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                            ObjectUtils.asMap("folder", "myWeddingPhoto/" + id));

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
                model.addAttribute("userID", id);
                return "photo/uploadPhotos";
            } else {
                model.addAttribute("error", "Nessun file selezionato");
                model.addAttribute("userID", id);
                return "photo/uploadPhotos";

            }

            // Se il blocco precedente non viene caricare viene mostrato un errore con il
            // relativo messaggio
        } catch (IOException e) {
            model.addAttribute("error", "Errore caricamento " + e.getMessage());

        }
        return "photo/uploadPhotos";

    }

    //Galleria foto caricaate

    @GetMapping("/gallery")
    public String showGallery(Model model, Authentication authentication) throws Exception {
        Optional<User> utenteLoggato = userRepository.findByEmail(authentication.getName());
        String folderUrl = "myWeddingPhoto/" + utenteLoggato.get().getId();
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
