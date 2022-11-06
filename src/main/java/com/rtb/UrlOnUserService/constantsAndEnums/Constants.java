package com.rtb.UrlOnUserService.constantsAndEnums;

public class Constants {

    public static final String CONFIRMATION_EMAIL_SUBJECT = "UrlOn : Verify your email";
    public static final String EMAIL_FROM = "noreply.urlon.com@gmail.com";
    public static final String CONFIRMATION_EMAIL_BASE_URL = "http://localhost:8001/urlon/api/users/account/verify?token=";
    public static final String PASSWORD_RESET_EMAIL_SUBJECT = "Password reset for UrlOn application";
    public static final String PASSWORD_RESET_EMAIL_BASE_URL = "http://localhost:8001/urlon/api/users/account/passwordReset?uid=";
    public static final Long ONE_DAY_MILLISECONDS = 86400000L;
    public static final String BEARER = "Bearer ";
}
