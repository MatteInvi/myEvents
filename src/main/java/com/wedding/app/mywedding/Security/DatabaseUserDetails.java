package com.wedding.app.mywedding.Security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.wedding.app.mywedding.Model.Role;
import com.wedding.app.mywedding.Model.User;

public class DatabaseUserDetails implements UserDetails {

    private final Integer id;
    private final String username;
    private final String password;
    private final Set<GrantedAuthority> grantedAuthorities;

    public DatabaseUserDetails(User user) {

        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();

        this.grantedAuthorities = new HashSet<>();

        for (Role role : user.getRoles()) {
            this.grantedAuthorities.add(new SimpleGrantedAuthority(role.getNome()));
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return this.grantedAuthorities;
    }

    public Integer getId() {
        return this.id;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

}
