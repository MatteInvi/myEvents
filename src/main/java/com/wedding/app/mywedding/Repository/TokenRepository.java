package com.wedding.app.mywedding.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wedding.app.mywedding.Model.authToken;


public interface TokenRepository extends JpaRepository<authToken, Integer> {


    public Optional<authToken> findByToken(String token);
}
