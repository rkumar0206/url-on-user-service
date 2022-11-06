package com.rtb.UrlOnUserService.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtb.UrlOnUserService.constantsAndEnums.AccountVerificationMessage;
import com.rtb.UrlOnUserService.constantsAndEnums.Constants;
import com.rtb.UrlOnUserService.domain.Role;
import com.rtb.UrlOnUserService.domain.UrlOnUser;
import com.rtb.UrlOnUserService.models.CustomResponse;
import com.rtb.UrlOnUserService.models.UserRequest;
import com.rtb.UrlOnUserService.service.UserService;
import com.rtb.UrlOnUserService.util.JWT_Util;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.rtb.UrlOnUserService.constantsAndEnums.ErrorMessage.refreshTokenMissingError;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/urlon/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final Environment environment;

    @PostMapping("/create")
    public ResponseEntity<CustomResponse> createUser(@RequestBody UserRequest userRequest) {

        CustomResponse response = CustomResponse
                .builder()
                .code("" + HttpStatus.BAD_REQUEST.value())
                .message("Something went wrong")
                .build();

        if (userRequest.isUserDetailsValid()) {

            try {

                userService.saveUser(userRequest);

                response.setMessage("User created successfully. Please check your mail to verify your account.");
                response.setCode("" + HttpStatus.CREATED.value());

                return new ResponseEntity<>(response, HttpStatus.CREATED);

            } catch (RuntimeException exception) {

                response.setMessage(exception.getMessage());
                response.setCode("" + HttpStatus.BAD_REQUEST.value());

                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } else {

            response.setMessage("Invalid user details");
            response.setCode("" + HttpStatus.BAD_REQUEST.value());

            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/account/verify")
    public ResponseEntity<CustomResponse> verifyAccount(@RequestParam("token") String token) {

        CustomResponse response = CustomResponse
                .builder()
                .code(HttpStatus.BAD_REQUEST.toString())
                .message("Something went wrong")
                .build();

        AccountVerificationMessage accountVerificationMessage = userService.verifyAccount(token);

        switch (accountVerificationMessage) {

            case VERIFIED: {
                response.setMessage("Account verified.");
                response.setCode("" + HttpStatus.OK.value());
            }
            break;

            case ALREADY_VERIFIED: {

                response.setMessage("Account already verified. Please login.");
                response.setCode("" + HttpStatus.OK.value());
            }
            break;

            case INVALID: {

                response.setMessage("Invalid token.");
                response.setCode("" + HttpStatus.BAD_REQUEST.value());
            }
        }

        return new ResponseEntity<>(response, HttpStatus.valueOf(Integer.parseInt(response.getCode())));
    }

    @GetMapping("/token/refresh")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String authorizationHeader = request.getHeader(AUTHORIZATION);

        if (authorizationHeader != null && authorizationHeader.startsWith(Constants.BEARER)) {

            String token = authorizationHeader.substring(Constants.BEARER.length());

            try {

                Algorithm algorithm = Algorithm.HMAC256(environment.getProperty("token.secret").getBytes());
                JWTVerifier jwtVerifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = jwtVerifier.verify(token);

                String username = decodedJWT.getSubject();
                UrlOnUser appUser = userService.getUserByEmailIdOrByUsername(username);

                if (appUser != null && appUser.isAccountVerified()) {

                    Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    appUser.getRoles().stream().map(Role::getRoleName)
                            .forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));

                    User user = new User(appUser.getUsername(), "", authorities);

                    Map<String, String> tokens = new HashMap<>();
                    tokens.put("access_token", JWT_Util.generateAccessToken(user));
                    tokens.put("refresh_token", token);

                    response.setContentType(APPLICATION_JSON_VALUE);
                    new ObjectMapper().writeValue(response.getOutputStream(), tokens);
                } else {
                    throw new RuntimeException("Invalid user");
                }

            } catch (Exception e) {

                e.printStackTrace();

                response.setHeader("error", e.getMessage());
                response.setStatus(FORBIDDEN.value());

                Map<String, String> error = new HashMap<>();
                error.put("error", e.getMessage());

                response.setContentType(APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), error);
            }

        } else {
            throw new RuntimeException(refreshTokenMissingError);
        }

    }
}
