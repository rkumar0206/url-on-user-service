package com.rtb.UrlOnUserService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtb.UrlOnUserService.domain.UserAccount;
import com.rtb.UrlOnUserService.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

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
    CommandLineRunner run(UserRepository userRepository) {

        return args -> {

            UserAccount user = new UserAccount(
                    null,
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
                    null,
                    new ArrayList<>()
            );

            userRepository.save(user);

            List<String> userIds = Arrays.asList("rk0001", "rk0002", "rk0003", "rk0004", "rk0005");

            userIds.forEach(uid -> {

                int k = new Random().nextInt(100);

                UserAccount userTemp = new UserAccount(
                        null,
                        "user" + k + "@gmail.com",
                        Long.toHexString(new Random().nextLong()),
                        getPasswordEncoder().encode("rohit"),
                        uid,
                        "User" + k,
                        "lastName",
                        null,
                        null,
                        new Date(),
                        true,
                        null,
                        new ArrayList<>()
                );

                userRepository.save(userTemp);
            });

        };
    }
}
