package com.rtb.UrlOnUserService.constantsAndEnums;

public class ErrorMessage {

    public static final String userNotFoundError = "User not found in the database";
    public static final String accountNotVerified = "Account not verified";
    public static final String accountNotVerifiedError_forgotPassword = "This account is not verified. Please try signing-up using the same email id";
    public static final String duplicateEmailIdError = "User with this email id is already present. Please enter other email";
    public static final String duplicateUsernameError = "This username is not available. Please try other username";
    public static final String refreshTokenMissingError = "Please pass the refresh token as Authorization token in headers.";
    public static final String sendingMailError = "Some error occurred while sending mail. Please try again!!";
    public static final String linkExpiredOrInvalidError = "Link expired or invalid!!";
    public static final String invalidUserDetailsForCreateError = "User details invalid!!. email, username, firstName, password and dob is mandatory";
    public static final String invalidUserDetailsForUpdateError = "User details invalid!!. email, firstName and dob is mandatory.";

}
