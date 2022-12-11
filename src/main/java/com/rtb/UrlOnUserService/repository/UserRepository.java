package com.rtb.UrlOnUserService.repository;

import com.rtb.UrlOnUserService.domain.UserAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByUsername(String username);

    Optional<UserAccount> findByEmailId(String emailId);

    Optional<UserAccount> findByUid(String uid);

    Optional<UserAccount> findByResetPasswordToken(String resetPasswordToken);

    @Query("select user from UserAccount user inner join Follower f on ")
    Page<UserAccount> findAllFollowersOfUser(String uid, Pageable pageable);
}
