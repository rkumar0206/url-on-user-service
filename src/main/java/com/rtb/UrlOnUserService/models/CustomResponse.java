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
public class CustomResponse<T> {
    String code;
    T response;

    public CustomResponse() {

        this.code = "" + HttpStatus.INTERNAL_SERVER_ERROR.value();
        this.response = null;
    }
}
