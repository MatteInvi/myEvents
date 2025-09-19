package com.wedding.app.mywedding.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wedding.app.mywedding.Model.Invited;

public interface InvitedRepository extends JpaRepository<Invited, Integer> {

    public List<Invited> findByNameIgnoreCase(String name);
    public boolean existsByEmail(String email);
    
}
