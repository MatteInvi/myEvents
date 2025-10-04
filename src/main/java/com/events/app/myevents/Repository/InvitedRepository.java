package com.events.app.myevents.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.events.app.myevents.Model.Invited;
import com.events.app.myevents.Model.User;

public interface InvitedRepository extends JpaRepository<Invited, Integer> {

    public List<Invited> findByNameIgnoreCase(String name);
    public boolean existsByEmail(String email);
    public List<Invited> findByUser(User user);
    public List<Invited> findByUserAndNameContainingIgnoreCase(User user, String name);
    
}
