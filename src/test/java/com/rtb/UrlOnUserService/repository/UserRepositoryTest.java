package com.rtb.UrlOnUserService.repository;

import com.rtb.UrlOnUserService.domain.UrlOnUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private UrlOnUser user;

    @BeforeEach
    void setup() {

        userRepository.deleteAll();

        user = new UrlOnUser(
                null,
                "test123@example.com",
                "test0206",
                "test@password",
                "ttttt",
                "TestFirstName",
                "TestLastName",
                null,
                null,
                new Date(),
                true,
                UUID.randomUUID().toString(),
                new ArrayList<>()
        );

        userRepository.save(user);
    }

    @Test
    void findByUsername() {

        Optional<UrlOnUser> expectedUser = userRepository.findByUsername(user.getUsername());

        assertThat(expectedUser).isPresent();
    }

    @Test
    void findByEmailId() {

        Optional<UrlOnUser> expectedUser = userRepository.findByEmailId(user.getEmailId());

        assertThat(expectedUser).isPresent();
    }

    @Test
    void findByUid() {

        Optional<UrlOnUser> expectedUser = userRepository.findByUid(user.getUid());
        assertThat(expectedUser).isPresent();
    }

    @Test
    void findByResetPasswordToken() {
        Optional<UrlOnUser> expectedUser = userRepository.findByResetPasswordToken(user.getResetPasswordToken());
        assertThat(expectedUser).isPresent();
    }
}