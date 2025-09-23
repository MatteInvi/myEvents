package com.wedding.app.mywedding.Controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Controller
@RequestMapping("/photo")
public class PhotoController {

    //Dichiarazione piattaforma su cui salvare foto
    private final Cloudinary cloudinary;

    public PhotoController(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @GetMapping
    public String Homepage() {
        return "pages/photo";
    }

    //Chiamata post per caricare foto
    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile[] files, Model model) {

        try {
            //Creazione array per selezione di foto multiple, controllo che la selezione non sia vuota
            //e in fine carico foto sulla piattaforma
            List<Map<String, Object>> upldodedFiles = new ArrayList<>();

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                            ObjectUtils.asMap("folder", "myWeddingPhoto"));

                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("url", uploadResult.get("secure_url"));
                    fileInfo.put("publicId", uploadResult.get("public_id"));
                    fileInfo.put("name", file.getOriginalFilename());

                    upldodedFiles.add(fileInfo);
                }

            }

            //Restituisco messaggio di avvenuto caricamento con i relativi dati
            //altrimenti restituisco errore se non sono state selezionate foto
            if (upldodedFiles.size() > 0) {
                model.addAttribute("success", "Caricamento avvenuto con successo!");
                model.addAttribute("uploadedFiles", upldodedFiles);
                return "pages/photo";
            } else {
                model.addAttribute("error", "Nessun file selezionato");
                return "pages/photo";

            }

        // Se il blocco precedente non viene caricare viene mostrato un errore con il relativo messaggio        
        } catch (IOException e) {
            model.addAttribute("error", "Errore caricamento " + e.getMessage());

        }
        return "pages/photo";

    }

}
