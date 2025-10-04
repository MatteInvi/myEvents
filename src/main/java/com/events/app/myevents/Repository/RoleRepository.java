package com.events.app.myevents.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.events.app.myevents.Model.Role;

import java.util.Optional;



public interface RoleRepository extends JpaRepository<Role, Integer> {

    public Optional<Role> findById(Integer id);

}
