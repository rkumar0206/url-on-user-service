package com.rtb.UrlOnUserService.repository;

import com.rtb.UrlOnUserService.domain.Follower;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowerRepository extends JpaRepository<Follower, Long> {
}
