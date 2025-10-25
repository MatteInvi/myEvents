package com.events.app.myevents.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.events.app.myevents.Model.PasswordResetToken;
import com.events.app.myevents.Model.User;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
     Optional<PasswordResetToken> findByToken(String token);
      Optional<PasswordResetToken> findByUser(User user);
     void deleteByUser(User user);
}
