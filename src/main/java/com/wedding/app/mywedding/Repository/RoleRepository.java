package com.wedding.app.mywedding.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wedding.app.mywedding.Model.Role;
import java.util.Optional;



public interface RoleRepository extends JpaRepository<Role, Integer> {

    public Optional<Role> findById(Integer id);

}
