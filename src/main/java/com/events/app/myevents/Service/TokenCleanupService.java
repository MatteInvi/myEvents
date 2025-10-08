package com.events.app.myevents.Service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.events.app.myevents.Repository.TokenRepository;

@Component
public class TokenCleanupService {

    @Autowired
    TokenRepository tokenRepository;

    // Controllo ed elimino token scaduti ogni minuto
    @Scheduled(fixedDelay = 60 * 1000)
    private void deletingExpriredToken(){
        tokenRepository.deleteAllExpiredToken(LocalDateTime.now());
    }
    
}
