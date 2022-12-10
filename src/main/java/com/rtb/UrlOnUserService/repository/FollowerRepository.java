package com.rtb.UrlOnUserService.repository;

import com.rtb.UrlOnUserService.domain.Follower;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FollowerRepository extends JpaRepository<Follower, Long> {

    Optional<Follower> findByUserUidAndFollowerUid(String userUid, String followerUid);

    void deleteByUserUidAndFollowerUid(String userUid, String followerUid);
}
