package com.wedding.app.mywedding.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wedding.app.mywedding.Model.Invited;

public interface InvitedRepository extends JpaRepository<Invited, Integer> {
    
}
