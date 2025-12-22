package com.events.app.myevents.Utility;

public class CloudinaryUtils {

    public static String extractPublicId(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        try {
            // Trova la parte dopo "/upload/"
            String[] parts = imageUrl.split("/upload/");
            if (parts.length < 2) {
                return null;
            }

            // Rimuove la versione e l'estensione del file
            String pathPart = parts[1];
           

            // Rimuove la versione
            pathPart = pathPart.replaceFirst("^v\\d+/", "");

            // Rimuove l'estensione (.jpg, .png, ecc.)
            int dotIndex = pathPart.lastIndexOf('.');
            if (dotIndex != -1) {
                pathPart = pathPart.substring(0, dotIndex);
            }

            return pathPart;
        } catch (Exception e) {
            return null;
        }
    }
}