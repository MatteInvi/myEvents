package com.events.app.myevents.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.events.app.myevents.Utility.CloudinaryUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;
    
    //Eliminazione immagine da Cloudinary tramite URL
    public Map deleteByUrl(String imageUrl) throws IOException {
        String publicId = CloudinaryUtils.extractPublicId(imageUrl);
        return cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}
