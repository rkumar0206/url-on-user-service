package com.rtb.UrlOnUserService.repository;

import com.rtb.UrlOnUserService.domain.Role;
import com.rtb.UrlOnUserService.domain.RoleNames;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    private Role role;

    @BeforeEach
    void setUp() {

        roleRepository.deleteAll();

        role = new Role(
                null,
                RoleNames.ADMIN
        );

        roleRepository.save(role);
    }

    @Test
    void findByRoleName() {

        Optional<Role> expectedRole = roleRepository.findByRoleName(role.getRoleName());

        assertThat(expectedRole).isPresent();

    }
}