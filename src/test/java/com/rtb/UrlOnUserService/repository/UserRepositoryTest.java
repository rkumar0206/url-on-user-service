package com.rtb.UrlOnUserService.repository;

import com.rtb.UrlOnUserService.domain.UserAccount;
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

    private UserAccount user;

    @BeforeEach
    void setup() {

        userRepository.deleteAll();

        user = new UserAccount(
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

        Optional<UserAccount> expectedUser = userRepository.findByUsername(user.getUsername());

        assertThat(expectedUser).isPresent();
    }

    @Test
    void findByEmailId() {

        Optional<UserAccount> expectedUser = userRepository.findByEmailId(user.getEmailId());

        assertThat(expectedUser).isPresent();
    }

    @Test
    void findByUid() {

        Optional<UserAccount> expectedUser = userRepository.findByUid(user.getUid());
        assertThat(expectedUser).isPresent();
    }

    @Test
    void findByResetPasswordToken() {
        Optional<UserAccount> expectedUser = userRepository.findByResetPasswordToken(user.getResetPasswordToken());
        assertThat(expectedUser).isPresent();
    }
}