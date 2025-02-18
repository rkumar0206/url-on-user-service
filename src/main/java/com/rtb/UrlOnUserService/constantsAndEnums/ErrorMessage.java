package com.rtb.UrlOnUserService.constantsAndEnums;

public class ErrorMessage {

    public static final String userNotFoundError = "User not found in the database";
    public static final String userWithUidNotFoundError = "User with uid %s not found in the database";
    public static final String userNotAuthenticatedError = "User not authenticated";
    public static final String accountNotVerifiedError = "This account is not verified";
    public static final String accountNotVerifiedForUidError = "Account is not verified for user uid %s";
    public static final String accountNotVerifiedError_forgotPassword = "This account is not verified. Please try signing-up using the same email id";
    public static final String duplicateEmailIdError = "User with this email id is already present. Please enter other email address";
    public static final String duplicateUsernameError = "This username is not available. Please try other username";
    public static final String refreshTokenMissingError = "Please pass the refresh token as Authorization token in headers.";
    public static final String sendingMailError = "Some error occurred while sending mail. Please try again!!";
    public static final String linkExpiredOrInvalidError = "Link expired or invalid!!";
    public static final String invalidUserDetailsForCreateError = "User details invalid!!. email, username, firstName, password and dob is mandatory";
    public static final String invalidUserDetailsForUpdateError = "User details invalid!!. email, uid, firstName and dob is mandatory.";
    public static final String invalidDetailsFoundForChangingEmailIDError = "Invalid details received for changing user email id!!. Please send correct previousEmailId, requestedEmailId and uid";
    public static final String invalidDetailsFoundForChangingUsernameError = "Invalid details received for changing user username!!. Please send correct previousUsername, requestedUsername, and uid";
    public static final String invalidUserAndUIDError = "You cannot make changes to this account";
    public static final String pageableError = "Max page size should be less than 50";
    public static final String requestBodyError = "Please send all the mandatory fields in request body";
    public static final String userAlreadyFollowingErrorMessage = "User %s is already followed by user %s";
}
