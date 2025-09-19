package com.wedding.app.mywedding.Security;

import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.wedding.app.mywedding.Model.User;
import com.wedding.app.mywedding.Repository.UserRepository;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{

        Optional<User> user = userRepository.findByUsername(username);

        if (user.isPresent()){
            return new DatabaseUserDetails(user.get());
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }
    

}
