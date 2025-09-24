package com.wedding.app.mywedding.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wedding.app.mywedding.Model.Invited;
import com.wedding.app.mywedding.Model.User;

public interface InvitedRepository extends JpaRepository<Invited, Integer> {

    public List<Invited> findByNameIgnoreCase(String name);
    public boolean existsByEmail(String email);
    public List<Invited> findByUser(User user);
    public List<Invited> findByUserAndNameContainingIgnoreCase(User user, String name);
    
}
