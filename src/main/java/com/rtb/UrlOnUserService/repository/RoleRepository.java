package com.rtb.UrlOnUserService.repository;

import com.rtb.UrlOnUserService.domain.Role;
import com.rtb.UrlOnUserService.domain.RoleNames;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleName(RoleNames roleName);
}
