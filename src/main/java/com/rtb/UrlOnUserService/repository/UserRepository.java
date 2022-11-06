package com.rtb.UrlOnUserService.repository;

import com.rtb.UrlOnUserService.domain.UrlOnUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UrlOnUser, Long> {

    Optional<UrlOnUser> findByUsername(String username);
    Optional<UrlOnUser> findByEmailId(String emailId);
    Optional<UrlOnUser> findByUid(String uid);
}
