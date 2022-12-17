package com.rtb.UrlOnUserService.repository;

import com.rtb.UrlOnUserService.domain.Follower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface FollowerRepository extends JpaRepository<Follower, Long> {

    // this is to check if the authenticated user is already following the requested user
    Optional<Follower> findByFollowerUidAndFollowingUid(String followerUid, String followingUid);

    // will unfollow any user
    @Transactional
    @Modifying
    @Query("delete from Follower f where f.followingUid = ?1 and f.followerUid = ?2")
    void deleteByFollowingUidAndFollowerUid(String followingUid, String followerUid);
}
