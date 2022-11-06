package com.rtb.UrlOnUserService.repository;

import com.rtb.UrlOnUserService.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findByRoleName(String roleName);
}
