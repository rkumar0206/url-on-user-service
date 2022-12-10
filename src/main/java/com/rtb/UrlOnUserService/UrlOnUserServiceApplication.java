package com.rtb.UrlOnUserService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtb.UrlOnUserService.domain.Follower;
import com.rtb.UrlOnUserService.domain.Role;
import com.rtb.UrlOnUserService.domain.RoleNames;
import com.rtb.UrlOnUserService.domain.UserAccount;
import com.rtb.UrlOnUserService.repository.RoleRepository;
import com.rtb.UrlOnUserService.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@SpringBootApplication
@EnableEurekaClient
public class UrlOnUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrlOnUserServiceApplication.class, args);
    }

    @Bean
    public BCryptPasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

    //@Bean
    CommandLineRunner run(UserRepository userRepository, RoleRepository roleRepository) {

        return args -> {

            Role role = new Role(null, RoleNames.ADMIN);
            roleRepository.save(role);

            UserAccount user = new UserAccount(
                    "rkumar0206@gmail.com",
                    "rkumar0206",
                    getPasswordEncoder().encode("rohit"),
                    "rrrrr",
                    "Rohit",
                    "Kumar",
                    null,
                    null,
                    new Date(),
                    true,
                    null
            );

            user.getRoles().add(role);

            userRepository.save(user);

            List<String> userIds = Arrays.asList("rk0001", "rk0002", "rk0003", "rk0004", "rk0005");

            for (int i = 0; i < userIds.size(); i++) {

                UserAccount userTemp = new UserAccount(
                        "user" + (i + 1) + "@gmail.com",
                        "rkumar020" + (i + 1),
                        getPasswordEncoder().encode("rohit"),
                        userIds.get(i),
                        "User" + (i + 1),
                        "lastName",
                        null,
                        null,
                        new Date(),
                        true,
                        null
                );

                userTemp.getFollowers()
                        .add(new Follower(i == 0 ? userIds.get(i + 1) : userIds.get(i - 1), System.currentTimeMillis()));

                if (i % 2 == 0) userTemp.getFollowers().add(new Follower(user.getUid(), System.currentTimeMillis()));

                userRepository.save(userTemp);
            }
        };
    }
}
