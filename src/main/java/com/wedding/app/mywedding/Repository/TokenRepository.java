package com.wedding.app.mywedding.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.wedding.app.mywedding.Model.authToken;

import jakarta.transaction.Transactional;


public interface TokenRepository extends JpaRepository<authToken, Integer> {


    public Optional<authToken> findByToken(String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM authToken token WHERE token.expireDate <= :now")
    void deleteAllExpiredSince(LocalDateTime now);
}
