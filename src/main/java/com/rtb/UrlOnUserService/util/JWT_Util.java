package com.rtb.UrlOnUserService.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.rtb.UrlOnUserService.constantsAndEnums.Constants;
import com.rtb.UrlOnUserService.domain.Role;
import com.rtb.UrlOnUserService.domain.UserAccount;
import com.rtb.UrlOnUserService.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JWT_Util {

    private static Integer access_token_expiration_time_day;
    private static Integer refresh_token_expiration_time_day;
    private static String issuer;
    private static Environment environment;

    private static Algorithm algorithm;

    private static UserService userService;

    @Autowired
    public JWT_Util(Environment environment, UserService userService) {
        JWT_Util.environment = environment;

        access_token_expiration_time_day = Integer.parseInt(environment.getProperty("token.access_expiration_time_day", "1"));
        refresh_token_expiration_time_day = Integer.parseInt(environment.getProperty("token.refresh_expiration_time_day", "30"));
        issuer = environment.getProperty("token.issuer");
        String secret = environment.getProperty("token.secret");
        algorithm = Algorithm.HMAC256(secret.getBytes());
        JWT_Util.userService = userService;
    }

    public static String generateAccessToken(User user) {

        UserAccount userAccount = userService.getUserByUserName(user.getUsername());
        return generateAccessToken(userAccount);
    }

    public static String generateAccessToken(UserAccount userAccount) {

        long expiry = System.currentTimeMillis() + Constants.ONE_DAY_MILLISECONDS * access_token_expiration_time_day;
        //long expiry = System.currentTimeMillis() + 2 * 60 * 1000;

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("firstName", userAccount.getFirstName());
        userInfo.put("lastName", userAccount.getLastName());

        String[] roles = userAccount.getRoles().stream().map(Role::getRoleName).map(Enum::toString).toArray(String[]::new);

        return JWT.create()
                .withSubject(userAccount.getUsername())
                .withIssuedAt(new Date(System.currentTimeMillis()))
                .withKeyId(UUID.randomUUID().toString())
                .withExpiresAt(new Date(expiry))
                .withIssuer(issuer)
                .withArrayClaim("roles", roles)
                .withClaim("uid", userAccount.getUid())
                .withPayload(userInfo)
                .sign(algorithm);
    }

    public static String generateRefreshToken(User user) {

        UserAccount userAccount = userService.getUserByUserName(user.getUsername());
        return generateRefreshToken(userAccount);
    }

    public static String generateRefreshToken(UserAccount userAccount) {

        long expiry = System.currentTimeMillis() + (Constants.ONE_DAY_MILLISECONDS * refresh_token_expiration_time_day);
        //long expiry = System.currentTimeMillis() + 4 * 60 * 1000;

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("firstName", userAccount.getFirstName());
        userInfo.put("lastName", userAccount.getLastName());

        return JWT.create().withSubject(userAccount.getUsername())
                .withExpiresAt(new Date(expiry))
                .withIssuedAt(new Date(System.currentTimeMillis()))
                .withIssuer(issuer)
                .withPayload(userInfo)
                .sign(algorithm);
    }


    public static String generateTokenWithExpiry(String subject, long expiryTimeInMillis) {

        return JWT.create()
                .withSubject(subject)
                .withExpiresAt(new Date(expiryTimeInMillis))
                .withIssuer(issuer)
                .sign(algorithm);
    }

    public static boolean isTokenValid(String token) {

        JWTVerifier jwtVerifier = JWT.require(algorithm).build();

        try {
            jwtVerifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {

            return false;
        }
    }

    public static String getUsername(String token) {

        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = jwtVerifier.verify(token);
        return decodedJWT.getSubject();
    }

}
