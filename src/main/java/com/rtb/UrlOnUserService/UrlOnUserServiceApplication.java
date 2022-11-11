package com.rtb.UrlOnUserService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtb.UrlOnUserService.domain.UrlOnUser;
import com.rtb.UrlOnUserService.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.Date;

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

    @Bean
    CommandLineRunner run(UserRepository userRepository) {

        return args -> {

            UrlOnUser user = new UrlOnUser(
                    null,
                    "rkumar0206@gmail.com",
                    "rkumar0206",
                    "rohit",
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
        };
    }
}
