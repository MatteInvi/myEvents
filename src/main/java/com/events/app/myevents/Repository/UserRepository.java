package com.events.app.myevents.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.events.app.myevents.Model.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    
    public Optional<User> findByEmail(String email);
    public boolean existsByEmail(String email);
    
}
