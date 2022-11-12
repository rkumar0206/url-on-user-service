package com.rtb.UrlOnUserService.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class CustomResponse {
    String code;
    String message;

    public CustomResponse() {

        this.code = "" + HttpStatus.INTERNAL_SERVER_ERROR.value();
        this.message = "Something went wrong";
    }
}
