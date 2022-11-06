package com.rtb.UrlOnUserService.repository;

import com.rtb.UrlOnUserService.domain.ConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {

    Optional<ConfirmationToken> findByConfirmationToken(String token);
    Optional<ConfirmationToken> findByUserEmailId(String email);
}
