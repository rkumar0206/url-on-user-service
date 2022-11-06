package com.rtb.UrlOnUserService.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.rtb.UrlOnUserService.constantsAndEnums.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JWT_Util {

    private static Integer access_token_expiration_time_day;
    private static Integer refresh_token_expiration_time_day;
    private static String issuer;
    private static String secret;
    private static Environment environment;

    @Autowired
    public JWT_Util(Environment environment) {
        JWT_Util.environment = environment;

        access_token_expiration_time_day = Integer.parseInt(environment.getProperty("token.access_expiration_time_day", "1"));
        refresh_token_expiration_time_day = Integer.parseInt(environment.getProperty("token.refresh_expiration_time_day", "30"));
        issuer = environment.getProperty("token.issuer");
        secret = environment.getProperty("token.secret");
    }

    public static String generateAccessToken(User user) {

        Algorithm algorithm = Algorithm.HMAC256(secret.getBytes());

        //long expiry = System.currentTimeMillis() + Constants.ONE_DAY_MILLISECONDS * access_token_expiration_time_day;
        long expiry = System.currentTimeMillis() + 2 * 60 * 1000;

        System.out.println(expiry);

        return JWT.create()
                .withSubject(user.getUsername())
                .withIssuedAt(new Date(System.currentTimeMillis()))
                .withKeyId(UUID.randomUUID().toString())
                .withExpiresAt(new Date(expiry))
                .withIssuer(issuer)
                .withClaim("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .sign(algorithm);

    }

    public static String generateRefreshToken(User user) {

        Algorithm algorithm = Algorithm.HMAC256(secret.getBytes());

        //long expiry = System.currentTimeMillis() + (Constants.ONE_DAY_MILLISECONDS * refresh_token_expiration_time_day);
        long expiry = System.currentTimeMillis() + 4 * 60 * 1000;

        return JWT.create().withSubject(user.getUsername())
                .withExpiresAt(new Date(expiry))
                .withIssuedAt(new Date(System.currentTimeMillis()))
                .withIssuer(issuer)
                .sign(algorithm);

    }

    public static DecodedJWT verifyTokenAndGetDecodedToken(String token) {

        try {

            Algorithm algorithm = Algorithm.HMAC256(secret.getBytes());

            JWTVerifier jwtVerifier = JWT.require(algorithm).build();

            return jwtVerifier.verify(token);

        } catch (Exception e) {

            return null;
        }

    }

}
