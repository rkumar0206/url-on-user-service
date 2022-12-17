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

    @Query("select user from UserAccount user inner join Follower f on user.uid = f.followerUid where f.followingUid = ?1")
    Page<UserAccount> findAllFollowersOfUser(String uid, Pageable pageable);

    @Query("select user from UserAccount user inner join Follower f on user.uid = f.followingUid where f.followerUid = ?1")
    Page<UserAccount> findAllUserAccountsUserIsFollowing(String uid, Pageable pageable);
}
