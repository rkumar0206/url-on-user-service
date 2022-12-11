package com.rtb.UrlOnUserService.exceptions;

public class PageableException extends RuntimeException {

    public PageableException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
