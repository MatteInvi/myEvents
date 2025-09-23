package com.wedding.app.mywedding.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wedding.app.mywedding.Model.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    
    public Optional<User> findByEmail(String email);
    public boolean existsByEmail(String email);
    
}
