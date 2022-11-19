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
import com.rtb.UrlOnUserService.models.*;
import com.rtb.UrlOnUserService.service.UserService;
import com.rtb.UrlOnUserService.util.JWT_Util;
import com.rtb.UrlOnUserService.util.Utility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import static com.rtb.UrlOnUserService.constantsAndEnums.Constants.*;
import static com.rtb.UrlOnUserService.constantsAndEnums.ErrorMessage.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/urlon/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final Environment environment;
    private final ObjectMapper objectMapper;

    @PostMapping("/create")
    public ResponseEntity<CustomResponse> createUser(@RequestBody UserCreateRequest userCreateRequest) {

        CustomResponse response = new CustomResponse();

        if (userCreateRequest.isUserDetailsValidForCreate()) {

            try {
                userService.saveUser(userCreateRequest);

                response.setMessage(USER_CREATED_SUCCESSFULLY);
                response.setCode("" + HttpStatus.CREATED.value());

            } catch (RuntimeException exception) {

                response.setMessage(exception.getMessage());
                if (exception.getMessage().equals(sendingMailError)) {
                    response.setCode("" + HttpStatus.INTERNAL_SERVER_ERROR.value());
                } else {
                    response.setCode("" + HttpStatus.BAD_REQUEST.value());
                }
            }
        } else {

            response.setMessage(invalidUserDetailsForCreateError);
            response.setCode("" + HttpStatus.BAD_REQUEST.value());
        }
        return new ResponseEntity<>(response, HttpStatus.valueOf(Integer.parseInt(response.getCode())));
    }

    @PutMapping("/update")
    public ResponseEntity<CustomResponse> updateUser(@RequestBody UpdateUserDetailsRequest updateUserDetailsRequest) {

        CustomResponse response = new CustomResponse();

        if (updateUserDetailsRequest.isUserDetailsValidForUpdate()) {

            try {

                userService.updateUserDetails(updateUserDetailsRequest);

                response.setMessage(USER_DETAILS_UPDATED_SUCCESSFULLY);
                response.setCode("" + HttpStatus.OK.value());
                log.info(USER_DETAILS_UPDATED_SUCCESSFULLY);
            } catch (RuntimeException exception) {

                response.setMessage(exception.getMessage());
                response.setCode("" + HttpStatus.BAD_REQUEST.value());
            }
        } else {

            response.setMessage(invalidUserDetailsForUpdateError);
            response.setCode("" + HttpStatus.BAD_REQUEST.value());
            log.error(invalidUserDetailsForUpdateError);
        }

        return new ResponseEntity<>(response, HttpStatus.valueOf(Integer.parseInt(response.getCode())));
    }

    @PutMapping("/update/emailId")
    public ResponseEntity<CustomResponse> changeUserEmailID(@RequestBody ChangeUserEmailIdRequest changeUserEmailIdRequest) {

        CustomResponse response = new CustomResponse();

        if (changeUserEmailIdRequest.isRequestValid()) {

            try {

                userService.changeUserEmailId(changeUserEmailIdRequest);

                response.setMessage(USER_EMAIL_ID_UPDATED_SUCCESSFULLY);
                response.setCode("" + HttpStatus.OK.value());

                log.info(USER_EMAIL_ID_UPDATED_SUCCESSFULLY);

            } catch (RuntimeException exception) {

                response.setMessage(exception.getMessage());

                if (exception.getMessage().equals(sendingMailError)) {
                    response.setCode("" + HttpStatus.INTERNAL_SERVER_ERROR.value());
                } else {
                    response.setCode("" + HttpStatus.BAD_REQUEST.value());
                }
            }

        } else {

            response.setMessage(invalidDetailsFoundForChangingEmailIDError);
            response.setCode("" + HttpStatus.BAD_REQUEST.value());
            log.error(invalidDetailsFoundForChangingEmailIDError);
        }

        return new ResponseEntity<>(response, HttpStatus.valueOf(Integer.parseInt(response.getCode())));
    }

    @PutMapping("/update/username")
    public ResponseEntity<CustomResponse> changeUserUsername(@RequestBody ChangeUserUsernameRequest changeUserUsernameRequest) {

        CustomResponse response = new CustomResponse();

        if (changeUserUsernameRequest.isRequestValid()) {

            try {
                userService.changeUserUsername(changeUserUsernameRequest);

                response.setMessage(USER_USERNAME_UPDATED_SUCCESSFULLY);
                response.setCode("" + HttpStatus.OK.value());

                log.info(USER_USERNAME_UPDATED_SUCCESSFULLY);

            } catch (RuntimeException exception) {

                response.setMessage(exception.getMessage());
                response.setCode("" + HttpStatus.BAD_REQUEST.value());
            }
        } else {

            response.setMessage(invalidDetailsFoundForChangingUsernameError);
            response.setCode("" + HttpStatus.BAD_REQUEST.value());
            log.error(invalidDetailsFoundForChangingUsernameError);
        }

        return new ResponseEntity<>(response, HttpStatus.valueOf(Integer.parseInt(response.getCode())));
    }

    @GetMapping("/checkUsernameExists/{username}")
    public ResponseEntity<CustomResponse> isUsernameAlreadyPresent(@PathVariable("username") String username) {

        CustomResponse response = new CustomResponse();

        UrlOnUser user = userService.getUserByUserName(username.trim());

        if (user != null) {

            response.setCode("" + HttpStatus.OK.value());
            response.setMessage("Username already taken.");
        } else {

            response.setCode("" + HttpStatus.NO_CONTENT.value());
            response.setMessage("User not found with this username.");
        }

        return new ResponseEntity<>(response, HttpStatus.valueOf(Integer.parseInt(response.getCode())));
    }

    @GetMapping("/checkEmailExists/{email}")
    public ResponseEntity<CustomResponse> isEmailIDAlreadyPresent(@PathVariable("email") String email) {

        CustomResponse response = new CustomResponse();

        UrlOnUser user = userService.getUserByEmailId(email.trim());

        if (user != null) {

            response.setCode("" + HttpStatus.OK.value());
            response.setMessage("Account already present with this email id.");
        } else {

            response.setCode("" + HttpStatus.NO_CONTENT.value());
            response.setMessage("User not found with this Email Id.");
        }

        return new ResponseEntity<>(response, HttpStatus.valueOf(Integer.parseInt(response.getCode())));
    }

    @GetMapping("/detail/self/{uid}")
    public ResponseEntity<UserSelfDetailsResponse> getUserDetailsOfRequestingUser(HttpServletRequest request, @PathVariable("uid") String uid) {

        String authorizationHeader = request.getHeader(AUTHORIZATION);

        if (authorizationHeader != null && authorizationHeader.startsWith(Constants.BEARER)) {

            String token = Utility.getTokenFromAuthorizationHeader(authorizationHeader);
            String username = JWT_Util.getUsername(token);

            UrlOnUser user = userService.getUserByEmailIdOrByUsername(username);

            if (!user.getUid().equals(uid.trim())) {
                throw new RuntimeException("User verification failed for this user.");
            }

            UserSelfDetailsResponse userSelfDetailsResponse = UserSelfDetailsResponse.builder()
                    .emailId(user.getEmailId())
                    .username(user.getUsername())
                    .uid(user.getUid())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .profileImage(user.getProfileImage())
                    .phoneNumber(user.getPhoneNumber())
                    .dob(user.getDob())
                    .roles(user.getRoles())
                    .build();

            return new ResponseEntity<>(userSelfDetailsResponse, HttpStatus.OK);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/detail/{username}")
    public ResponseEntity<UserDetailResponse> getUserDetailsUsingUsername(HttpServletRequest request, @PathVariable("username") String username) {

        UrlOnUser user = userService.getUserByUserName(username);

        if (user != null) {
            UserDetailResponse userDetailResponse = UserDetailResponse
                    .builder()
                    .username(user.getUsername())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .profileImage(user.getProfileImage())
                    .phoneNumber(user.getPhoneNumber())
                    .dob(user.getDob())
                    .build();

            return new ResponseEntity<>(userDetailResponse, HttpStatus.OK);
        } else {

            return ResponseEntity.noContent().build();
        }
    }


    @GetMapping("/account/verify")
    public ResponseEntity<CustomResponse> verifyAccount(@RequestParam("token") String token) {

        CustomResponse response = CustomResponse
                .builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.toString())
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

            String token = Utility.getTokenFromAuthorizationHeader(authorizationHeader);

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
                    tokens.put(ACCESS_TOKEN, JWT_Util.generateAccessToken(user));
                    tokens.put(REFRESH_TOKEN, token);

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
