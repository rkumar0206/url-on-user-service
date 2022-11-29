package com.rtb.UrlOnUserService.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtb.UrlOnUserService.constantsAndEnums.AccountVerificationMessage;
import com.rtb.UrlOnUserService.constantsAndEnums.Constants;
import com.rtb.UrlOnUserService.domain.UserAccount;
import com.rtb.UrlOnUserService.exceptions.UserException;
import com.rtb.UrlOnUserService.models.*;
import com.rtb.UrlOnUserService.service.UserService;
import com.rtb.UrlOnUserService.util.JWT_Util;
import com.rtb.UrlOnUserService.util.Utility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.rmi.RemoteException;
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

    @PostMapping("/create")
    public ResponseEntity<CustomResponse<String>> createUser(@RequestBody UserCreateRequest userCreateRequest) {

        CustomResponse<String> response = new CustomResponse<>();

        if (userCreateRequest.isUserDetailsValidForCreate()) {

            try {
                userService.saveUser(userCreateRequest);

                response.setResponse(USER_CREATED_SUCCESSFULLY);
                response.setStatus("" + HttpStatus.CREATED.value());

            } catch (RuntimeException exception) {

                response.setResponse(exception.getMessage());
                if (exception.getMessage().equals(sendingMailError)) {
                    response.setStatus("" + HttpStatus.INTERNAL_SERVER_ERROR.value());
                } else {
                    response.setStatus("" + HttpStatus.BAD_REQUEST.value());
                }
            }
        } else {

            response.setResponse(invalidUserDetailsForCreateError);
            response.setStatus("" + HttpStatus.BAD_REQUEST.value());
        }
        return new ResponseEntity<>(response, HttpStatus.valueOf(Integer.parseInt(response.getStatus())));
    }

    @PutMapping("/update")
    public ResponseEntity<CustomResponse<String>> updateUser(@RequestBody UpdateUserDetailsRequest updateUserDetailsRequest) {

        CustomResponse<String> response = new CustomResponse<>();

        if (updateUserDetailsRequest.isUserDetailsValidForUpdate()) {

            try {

                userService.updateUserDetails(updateUserDetailsRequest);

                response.setResponse(USER_DETAILS_UPDATED_SUCCESSFULLY);
                response.setStatus("" + HttpStatus.OK.value());
                log.info(USER_DETAILS_UPDATED_SUCCESSFULLY);
            } catch (RuntimeException exception) {

                response.setResponse(exception.getMessage());
                response.setStatus("" + HttpStatus.BAD_REQUEST.value());
            }
        } else {

            response.setResponse(invalidUserDetailsForUpdateError);
            response.setStatus("" + HttpStatus.BAD_REQUEST.value());
            log.error(invalidUserDetailsForUpdateError);
        }

        return new ResponseEntity<>(response, HttpStatus.valueOf(Integer.parseInt(response.getStatus())));
    }

    @PutMapping("/update/emailId")
    public ResponseEntity<CustomResponse<String>> changeUserEmailID(@RequestBody ChangeUserEmailIdRequest changeUserEmailIdRequest) {

        CustomResponse<String> response = new CustomResponse<>();

        if (changeUserEmailIdRequest.isRequestValid()) {

            try {

                userService.changeUserEmailId(changeUserEmailIdRequest);

                response.setResponse(USER_EMAIL_ID_UPDATED_SUCCESSFULLY);
                response.setStatus("" + HttpStatus.OK.value());

                log.info(USER_EMAIL_ID_UPDATED_SUCCESSFULLY);

            } catch (RuntimeException exception) {

                response.setResponse(exception.getMessage());

                if (exception.getMessage().equals(sendingMailError)) {
                    response.setStatus("" + HttpStatus.INTERNAL_SERVER_ERROR.value());
                } else {
                    response.setStatus("" + HttpStatus.BAD_REQUEST.value());
                }
            }

        } else {

            response.setResponse(invalidDetailsFoundForChangingEmailIDError);
            response.setStatus("" + HttpStatus.BAD_REQUEST.value());
            log.error(invalidDetailsFoundForChangingEmailIDError);
        }

        return new ResponseEntity<>(response, HttpStatus.valueOf(Integer.parseInt(response.getStatus())));
    }

    @PutMapping("/update/username")
    public ResponseEntity<CustomResponse<Map<String, String>>> changeUserUsername(@RequestBody ChangeUserUsernameRequest changeUserUsernameRequest) {

        CustomResponse<Map<String, String>> response = new CustomResponse<>();
        Map<String, String> returnDetails = new HashMap<>();

        if (changeUserUsernameRequest.isRequestValid()) {

            try {
                // success
                UserAccount userAccount = userService.changeUserUsername(changeUserUsernameRequest);

                returnDetails.put("message", USER_USERNAME_UPDATED_SUCCESSFULLY);
                returnDetails.put(ACCESS_TOKEN, JWT_Util.generateAccessToken(userAccount));  // new access token
                returnDetails.put(REFRESH_TOKEN, JWT_Util.generateRefreshToken(userAccount)); // new refresh token

                response.setStatus("" + HttpStatus.OK.value());
                log.info(USER_USERNAME_UPDATED_SUCCESSFULLY);

            } catch (RuntimeException exception) {

                log.error(exception.getMessage());
                returnDetails.put("message", exception.getMessage());
                response.setStatus("" + HttpStatus.BAD_REQUEST.value());
            }
        } else {

            returnDetails.put("message", invalidDetailsFoundForChangingUsernameError);
            response.setStatus("" + HttpStatus.BAD_REQUEST.value());
            log.error(invalidDetailsFoundForChangingUsernameError);
        }

        response.setResponse(returnDetails);
        return new ResponseEntity<>(response, HttpStatus.valueOf(Integer.parseInt(response.getStatus())));
    }

    @GetMapping("/checkUsernameExists")
    public ResponseEntity<CustomResponse<String>> isUsernameAlreadyPresent(@RequestParam("username") String username) {

        CustomResponse<String> response = new CustomResponse<>();

        UserAccount user = userService.getUserByUserName(username.trim());

        if (user != null) {

            response.setStatus("" + HttpStatus.OK.value());
            response.setResponse("Username already taken.");
        } else {

            response.setStatus("" + HttpStatus.NO_CONTENT.value());
            response.setResponse("User not found with this username.");
        }

        return new ResponseEntity<>(response, HttpStatus.valueOf(Integer.parseInt(response.getStatus())));
    }

    @GetMapping("/checkEmailExists")
    public ResponseEntity<CustomResponse<String>> isEmailIDAlreadyPresent(@RequestParam("email") String email) {

        CustomResponse<String> response = new CustomResponse<>();

        UserAccount user = userService.getUserByEmailId(email.trim());

        if (user != null) {

            response.setStatus("" + HttpStatus.OK.value());
            response.setResponse("Account already present with this email id.");
        } else {

            response.setStatus("" + HttpStatus.NO_CONTENT.value());
            response.setResponse("User not found with this Email Id.");
        }

        return new ResponseEntity<>(response, HttpStatus.valueOf(Integer.parseInt(response.getStatus())));
    }

    @GetMapping("/myDetails")
    public ResponseEntity<CustomResponse> getAuthenticatedUserDetails() {

        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserAccount user = userService.getUserByUserName(authentication.getPrincipal().toString());

            if (user == null) return ResponseEntity.noContent().build();
            if (!user.isAccountVerified()) throw new UserException(accountNotVerifiedError);
            return new ResponseEntity<>(buildCustomResponseWithUserDetails(user), HttpStatus.OK);

        } catch (Exception e) {

            CustomResponse<String> customResponse = new CustomResponse<>();
            if (e instanceof UserException) {
                customResponse.setStatus("" + HttpStatus.UNAUTHORIZED.value());
            } else {
                customResponse.setStatus("" + HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
            customResponse.setResponse(e.getMessage());
            return new ResponseEntity<>(customResponse, HttpStatus.valueOf(Integer.parseInt(customResponse.getStatus())));
        }
    }


    @GetMapping("/detail/uid")
    public ResponseEntity<CustomResponse> getUserDetailsByUid(@RequestParam("uid") String uid) {

        try {

            UserAccount user = userService.getUserByUid(uid);

            if (user == null) return ResponseEntity.noContent().build();
            if (!user.isAccountVerified()) throw new UserException(accountNotVerifiedError);
            return new ResponseEntity<>(buildCustomResponseWithUserDetails(user), HttpStatus.OK);

        } catch (Exception e) {

            CustomResponse<String> customResponse = new CustomResponse<>();
            if (e instanceof UserException) {
                customResponse.setStatus("" + HttpStatus.UNAUTHORIZED.value());
            } else {
                customResponse.setStatus("" + HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
            customResponse.setResponse(e.getMessage());
            return new ResponseEntity<>(customResponse, HttpStatus.valueOf(Integer.parseInt(customResponse.getStatus())));
        }
    }

    @GetMapping("/detail/username")
    public ResponseEntity<CustomResponse> getAnyUserDetailsUsingUsername(@RequestParam("username") String username) {

        try {

            UserAccount user = userService.getUserByUserName(username);

            if (user == null) return ResponseEntity.noContent().build();
            if (!user.isAccountVerified()) throw new UserException(accountNotVerifiedError);
            return new ResponseEntity<>(buildCustomResponseWithUserDetails(user), HttpStatus.OK);

        } catch (Exception e) {

            CustomResponse<String> customResponse = new CustomResponse<>();
            if (e instanceof UserException) {
                customResponse.setStatus("" + HttpStatus.UNAUTHORIZED.value());
            } else {
                customResponse.setStatus("" + HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
            customResponse.setResponse(e.getMessage());
            return new ResponseEntity<>(customResponse, HttpStatus.valueOf(Integer.parseInt(customResponse.getStatus())));
        }
    }

    @GetMapping("/account/verify")
    public ResponseEntity<CustomResponse<String>> verifyAccount(@RequestParam("token") String token) {

        CustomResponse<String> response = CustomResponse.<String>builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.toString())
                .response("Something went wrong")
                .build();

        AccountVerificationMessage accountVerificationMessage = userService.verifyAccount(token);

        switch (accountVerificationMessage) {

            case VERIFIED: {
                response.setResponse("Account verified.");
                response.setStatus("" + HttpStatus.OK.value());
            }
            break;

            case ALREADY_VERIFIED: {

                response.setResponse("Account already verified. Please login.");
                response.setStatus("" + HttpStatus.OK.value());
            }
            break;

            case INVALID: {

                response.setResponse("Invalid token.");
                response.setStatus("" + HttpStatus.BAD_REQUEST.value());
            }
        }

        return new ResponseEntity<>(response, HttpStatus.valueOf(Integer.parseInt(response.getStatus())));
    }

    @GetMapping("/token/refresh")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response, @RequestParam("uid") String uid) throws IOException {

        String authorizationHeader = request.getHeader(AUTHORIZATION);

        if (authorizationHeader != null && authorizationHeader.startsWith(Constants.BEARER)) {

            String token = Utility.getTokenFromAuthorizationHeader(authorizationHeader);

            try {

                Algorithm algorithm = Algorithm.HMAC256(environment.getProperty("token.secret").getBytes());
                JWTVerifier jwtVerifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = jwtVerifier.verify(token);

                String username = decodedJWT.getSubject();
                UserAccount userAccount = userService.getUserByEmailIdOrByUsername(username);

                if (userAccount == null)
                    throw new RemoteException(userNotFoundError);

                if (!userAccount.getUid().equals(uid)) {
                    throw new RuntimeException("Permission denied");
                }

                if (!userAccount.isAccountVerified())
                    throw new RuntimeException(accountNotVerifiedError);

                Map<String, String> tokens = new HashMap<>();
                tokens.put(ACCESS_TOKEN, JWT_Util.generateAccessToken(userAccount));
                tokens.put(REFRESH_TOKEN, token);

                response.setContentType(APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), tokens);

            } catch (Exception e) {

                e.printStackTrace();

                response.setHeader("error", e.getMessage());
                response.setStatus(FORBIDDEN.value());

                Map<String, String> error = new HashMap<>();
                error.put(ERROR, e.getMessage());

                response.setContentType(APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), error);
            }

        } else {
            throw new RuntimeException(refreshTokenMissingError);
        }
    }

    private CustomResponse<UserDetailResponse> buildCustomResponseWithUserDetails(UserAccount user) {

        UserDetailResponse userSelfDetailsResponse = UserDetailResponse.builder()
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

        CustomResponse<UserDetailResponse> customResponse = new CustomResponse<>();

        customResponse.setStatus("" + HttpStatus.OK.value());
        customResponse.setResponse(userSelfDetailsResponse);

        return customResponse;
    }

}
