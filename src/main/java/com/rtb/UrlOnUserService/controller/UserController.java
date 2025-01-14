package com.rtb.UrlOnUserService.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtb.UrlOnUserService.constantsAndEnums.AccountVerificationMessage;
import com.rtb.UrlOnUserService.constantsAndEnums.Constants;
import com.rtb.UrlOnUserService.domain.UserAccount;
import com.rtb.UrlOnUserService.exceptions.FollowerException;
import com.rtb.UrlOnUserService.exceptions.PageableException;
import com.rtb.UrlOnUserService.exceptions.UserException;
import com.rtb.UrlOnUserService.models.*;
import com.rtb.UrlOnUserService.service.UserService;
import com.rtb.UrlOnUserService.util.JWT_Util;
import com.rtb.UrlOnUserService.util.ModelMapper;
import com.rtb.UrlOnUserService.util.Utility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import static com.rtb.UrlOnUserService.constantsAndEnums.Constants.*;
import static com.rtb.UrlOnUserService.constantsAndEnums.ErrorMessage.*;
import static com.rtb.UrlOnUserService.util.Utility.getCustomResponseForException;
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
                response.setCode("" + HttpStatus.CREATED.value());

            } catch (RuntimeException exception) {

                response.setResponse(exception.getMessage());
                if (exception.getMessage().equals(sendingMailError)) {
                    response.setCode("" + HttpStatus.INTERNAL_SERVER_ERROR.value());
                } else {
                    response.setCode("" + HttpStatus.BAD_REQUEST.value());
                }
            }
        } else {

            response.setResponse(invalidUserDetailsForCreateError);
            response.setCode("" + HttpStatus.BAD_REQUEST.value());
        }
        return new ResponseEntity<>(response, HttpStatus.valueOf(Integer.parseInt(response.getCode())));
    }

    @PutMapping("/update")
    public ResponseEntity<CustomResponse<String>> updateUser(@RequestBody UpdateUserDetailsRequest updateUserDetailsRequest) {

        CustomResponse<String> response = new CustomResponse<>();

        if (updateUserDetailsRequest.isUserDetailsValidForUpdate()) {

            try {

                userService.updateUserDetails(updateUserDetailsRequest);

                response.setResponse(USER_DETAILS_UPDATED_SUCCESSFULLY);
                response.setCode("" + HttpStatus.OK.value());
                log.info(USER_DETAILS_UPDATED_SUCCESSFULLY);
            } catch (RuntimeException exception) {

                response.setResponse(exception.getMessage());
                response.setCode("" + HttpStatus.BAD_REQUEST.value());
            }
        } else {

            response.setResponse(invalidUserDetailsForUpdateError);
            response.setCode("" + HttpStatus.BAD_REQUEST.value());
            log.error(invalidUserDetailsForUpdateError);
        }

        return new ResponseEntity<>(response, HttpStatus.valueOf(Integer.parseInt(response.getCode())));
    }

    @PutMapping("/update/emailId")
    public ResponseEntity<CustomResponse<String>> changeUserEmailID(@RequestBody ChangeUserEmailIdRequest changeUserEmailIdRequest) {

        CustomResponse<String> response = new CustomResponse<>();

        if (changeUserEmailIdRequest.isRequestValid()) {

            try {

                userService.changeUserEmailId(changeUserEmailIdRequest);

                response.setResponse(USER_EMAIL_ID_UPDATED_SUCCESSFULLY);
                response.setCode("" + HttpStatus.OK.value());

                log.info(USER_EMAIL_ID_UPDATED_SUCCESSFULLY);

            } catch (RuntimeException exception) {

                response.setResponse(exception.getMessage());

                if (exception.getMessage().equals(sendingMailError)) {
                    response.setCode("" + HttpStatus.INTERNAL_SERVER_ERROR.value());
                } else {
                    response.setCode("" + HttpStatus.BAD_REQUEST.value());
                }
            }

        } else {

            response.setResponse(invalidDetailsFoundForChangingEmailIDError);
            response.setCode("" + HttpStatus.BAD_REQUEST.value());
            log.error(invalidDetailsFoundForChangingEmailIDError);
        }

        return new ResponseEntity<>(response, HttpStatus.valueOf(Integer.parseInt(response.getCode())));
    }

    @PutMapping("/follower/add")
    public ResponseEntity<CustomResponse<String>> addFollower(@RequestBody FollowAndUnfollowRequest followAndUnfollowRequest) {

        CustomResponse<String> response = new CustomResponse<>();

        try {

            if (!followAndUnfollowRequest.isRequestValid())
                throw new FollowerException(requestBodyError);

            userService.followUser(followAndUnfollowRequest);
            response.setResponse(String.format(FOLLOWER_ADDED, followAndUnfollowRequest.getFollowingUid()));
            response.setCode("" + HttpStatus.OK.value());
            log.info(String.format(FOLLOWER_ADDED, followAndUnfollowRequest.getFollowingUid()));

        } catch (Exception e) {
            response = getCustomResponseForException(e);
        }

        return new ResponseEntity<>(response, HttpStatus.valueOf(Integer.parseInt(response.getCode())));
    }

    @PutMapping("/follower/delete")
    public ResponseEntity<CustomResponse<String>> deleteFollower(@RequestBody FollowAndUnfollowRequest followAndUnfollowRequest) {

        CustomResponse<String> response = new CustomResponse<>();

        try {

            if (!followAndUnfollowRequest.isRequestValid())
                throw new FollowerException(requestBodyError);

            userService.unfollowUser(followAndUnfollowRequest);
            response.setResponse(String.format(FOLLOWER_DELETED, followAndUnfollowRequest.getFollowingUid()));
            response.setCode("" + HttpStatus.OK.value());
            log.info(String.format(FOLLOWER_DELETED, followAndUnfollowRequest.getFollowingUid()));

        } catch (Exception e) {
            response = getCustomResponseForException(e);
        }

        return new ResponseEntity<>(response, HttpStatus.valueOf(Integer.parseInt(response.getCode())));
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

                response.setCode("" + HttpStatus.OK.value());
                log.info(USER_USERNAME_UPDATED_SUCCESSFULLY);

            } catch (RuntimeException exception) {

                log.error(exception.getMessage());
                returnDetails.put("message", exception.getMessage());
                response.setCode("" + HttpStatus.BAD_REQUEST.value());
            }
        } else {

            returnDetails.put("message", invalidDetailsFoundForChangingUsernameError);
            response.setCode("" + HttpStatus.BAD_REQUEST.value());
            log.error(invalidDetailsFoundForChangingUsernameError);
        }

        response.setResponse(returnDetails);
        return new ResponseEntity<>(response, HttpStatus.valueOf(Integer.parseInt(response.getCode())));
    }

    @GetMapping("/checkUsernameExists")
    public ResponseEntity<CustomResponse<String>> isUsernameAlreadyPresent(@RequestParam("username") String username) {

        CustomResponse<String> response = new CustomResponse<>();

        UserAccount user = userService.getUserByUserName(username.trim());

        if (user != null) {

            response.setCode("" + HttpStatus.OK.value());
            response.setResponse("Username already taken.");
        } else {

            response.setCode("" + HttpStatus.NO_CONTENT.value());
            response.setResponse("User not found with this username.");
        }

        return new ResponseEntity<>(response, HttpStatus.valueOf(Integer.parseInt(response.getCode())));
    }

    @GetMapping("/checkEmailExists")
    public ResponseEntity<CustomResponse<String>> isEmailIDAlreadyPresent(@RequestParam("email") String email) {

        CustomResponse<String> response = new CustomResponse<>();

        UserAccount user = userService.getUserByEmailId(email.trim());

        if (user != null) {

            response.setCode("" + HttpStatus.OK.value());
            response.setResponse("Account already present with this email id.");
        } else {

            response.setCode("" + HttpStatus.NO_CONTENT.value());
            response.setResponse("User not found with this Email Id.");
        }

        return new ResponseEntity<>(response, HttpStatus.valueOf(Integer.parseInt(response.getCode())));
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
                customResponse.setCode("" + HttpStatus.UNAUTHORIZED.value());
            } else {
                customResponse.setCode("" + HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
            customResponse.setResponse(e.getMessage());
            return new ResponseEntity<>(customResponse, HttpStatus.valueOf(Integer.parseInt(customResponse.getCode())));
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
                customResponse.setCode("" + HttpStatus.UNAUTHORIZED.value());
            } else {
                customResponse.setCode("" + HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
            customResponse.setResponse(e.getMessage());
            return new ResponseEntity<>(customResponse, HttpStatus.valueOf(Integer.parseInt(customResponse.getCode())));
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
                customResponse.setCode("" + HttpStatus.UNAUTHORIZED.value());
            } else {
                customResponse.setCode("" + HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
            customResponse.setResponse(e.getMessage());
            return new ResponseEntity<>(customResponse, HttpStatus.valueOf(Integer.parseInt(customResponse.getCode())));
        }
    }

    @GetMapping("/usernameToUid")
    public ResponseEntity<String> getUidFromUsername(@RequestParam("username") String username) {

        // this url should not require any authentication
        try {
            if (username != null && StringUtils.hasLength(username.trim())) {

                UserAccount user = userService.getUserByUserName(username);
                if (user == null) {
                    return ResponseEntity.noContent().build();
                } else {
                    return ResponseEntity.ok(user.getUid());
                }
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/uidToUsername")
    public ResponseEntity<String> getUsernameFromUid(@RequestParam("uid") String uid) {

        // this url should not require any authentication
        try {
            if (uid != null && StringUtils.hasLength(uid.trim())) {

                UserAccount user = userService.getUserByUid(uid);
                if (user == null) {
                    return ResponseEntity.noContent().build();
                } else {
                    return ResponseEntity.ok(user.getUsername());
                }
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }


    @GetMapping("/follower/user/{uid}")
    public ResponseEntity<CustomResponse> getAllFollowersOfUser(@PathVariable("uid") String uid, Pageable pageable) {

        try {
            if (pageable.getPageSize() > 50) {
                throw new PageableException(pageableError);
            }

            Page<UserAccount> accounts = userService.getAllFollowersOfUser(uid, pageable);
            CustomResponse<Page<UserDetailResponse>> customResponse =
                    CustomResponse.<Page<UserDetailResponse>>builder()
                            .code("" + HttpStatus.OK.value())
                            .response(accounts.map(ModelMapper::buildUserDetailResponse))
                            .build();

            return new ResponseEntity<>(customResponse, HttpStatus.OK);
        } catch (Exception e) {

            CustomResponse<String> customResponse = getCustomResponseForException(e);
            return new ResponseEntity<>(customResponse, HttpStatus.valueOf(Integer.parseInt(customResponse.getCode())));
        }

    }

    @GetMapping("/following/user/{uid}")
    public ResponseEntity<CustomResponse> getAllFollowingsOfUser(@PathVariable("uid") String uid, Pageable pageable) {

        try {
            if (pageable.getPageSize() > 50) {
                throw new PageableException(pageableError);
            }

            Page<UserAccount> accounts = userService.getAllFollowingsOfUser(uid, pageable);
            CustomResponse<Page<UserDetailResponse>> customResponse =
                    CustomResponse.<Page<UserDetailResponse>>builder()
                            .code("" + HttpStatus.OK.value())
                            .response(accounts.map(ModelMapper::buildUserDetailResponse))
                            .build();

            return new ResponseEntity<>(customResponse, HttpStatus.OK);
        } catch (Exception e) {

            CustomResponse<String> customResponse = getCustomResponseForException(e);
            return new ResponseEntity<>(customResponse, HttpStatus.valueOf(Integer.parseInt(customResponse.getCode())));
        }
    }

    @GetMapping("/account/verify")
    public ResponseEntity<CustomResponse<String>> verifyAccount(@RequestParam("token") String token) {

        CustomResponse<String> response = CustomResponse.<String>builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.toString())
                .response("Something went wrong")
                .build();

        AccountVerificationMessage accountVerificationMessage = userService.verifyAccount(token);

        switch (accountVerificationMessage) {

            case VERIFIED: {
                response.setResponse("Account verified.");
                response.setCode("" + HttpStatus.OK.value());
            }
            break;

            case ALREADY_VERIFIED: {

                response.setResponse("Account already verified. Please login.");
                response.setCode("" + HttpStatus.OK.value());
            }
            break;

            case INVALID: {

                response.setResponse("Invalid token.");
                response.setCode("" + HttpStatus.BAD_REQUEST.value());
            }
        }

        return new ResponseEntity<>(response, HttpStatus.valueOf(Integer.parseInt(response.getCode())));
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

        UserDetailResponse userSelfDetailsResponse = ModelMapper.buildUserDetailResponse(user);
        CustomResponse<UserDetailResponse> customResponse = new CustomResponse<>();
        customResponse.setCode("" + HttpStatus.OK.value());
        customResponse.setResponse(userSelfDetailsResponse);
        return customResponse;
    }

}
