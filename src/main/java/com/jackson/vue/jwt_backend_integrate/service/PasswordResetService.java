/***************************************************************
 * Author       :	 
 * Created Date :	
 * Version      : 	
 * History  :	
 * *************************************************************/
package com.jackson.vue.jwt_backend_integrate.service;

import com.jackson.vue.jwt_backend_integrate.model.PasswordResetTokenEntity;
import com.jackson.vue.jwt_backend_integrate.model.UserEntity;
import com.jackson.vue.jwt_backend_integrate.repository.PasswordResetTokenRepository;
import com.jackson.vue.jwt_backend_integrate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * PasswordResetService Class.
 * <p>
 * </p>
 *
 * @author
 */
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void createPasswordResetToken(String email) throws Exception {

        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String rawToken = UUID.randomUUID().toString() + "-" +System.nanoTime();
        String hashToken = passwordEncoder.encode(rawToken);

        PasswordResetTokenEntity passwordResetToken = new PasswordResetTokenEntity();
        passwordResetToken.setTokenHash(hashToken);
        passwordResetToken.setUser(user);
        passwordResetToken.setExpiryTime(LocalDateTime.now().plusMinutes(15));

        passwordResetTokenRepository.save(passwordResetToken);

    }

    public void createResetPassword(String resetToken, String newPassword) {

        PasswordResetTokenEntity tokenEntity = passwordResetTokenRepository.findAll().stream()
                    .filter(t -> passwordEncoder.matches(resetToken, t.getTokenHash()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (tokenEntity.getUsed() || tokenEntity.getExpiryTime().isBefore(LocalDateTime.now())){
            throw new IllegalStateException("Token Expired or already used");
        }

        UserEntity user = tokenEntity.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenEntity.setUsed(true);
        passwordResetTokenRepository.save(tokenEntity);

    }
}
