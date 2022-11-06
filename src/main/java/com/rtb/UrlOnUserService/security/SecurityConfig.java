package com.rtb.UrlOnUserService.security;

import com.rtb.UrlOnUserService.security.filter.CustomAuthenticationFilter;
import com.rtb.UrlOnUserService.security.filter.CustomAuthorizationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final Environment environment;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

        CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(getAuthenticationManagerBean());
        customAuthenticationFilter.setFilterProcessesUrl("/urlon/app/users/login");

        return httpSecurity.csrf()
                .disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests(auth -> {
                    auth.antMatchers(
                            "/urlon/app/users/login",
                            "/urlon/api/users/create",
                            "/urlon/api/users/account/verify",
                            "/urlon/api/users/token/refresh"
                            ).permitAll();
                    auth.anyRequest().authenticated();
                })
                .addFilterAfter(new CustomAuthorizationFilter(environment), UsernamePasswordAuthenticationFilter.class)
                .addFilter(customAuthenticationFilter)
                .build();
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity httpSecurity) throws Exception {

        return httpSecurity.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService)
                .passwordEncoder(bCryptPasswordEncoder)
                .and()
                .build();

    }

    @Bean
    public AuthenticationManager getAuthenticationManagerBean() throws Exception {

        return authenticationConfiguration.getAuthenticationManager();
    }

}
