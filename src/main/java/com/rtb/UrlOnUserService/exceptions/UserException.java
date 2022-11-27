package com.rtb.UrlOnUserService.exceptions;

public class UserException extends RuntimeException {

    public UserException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
