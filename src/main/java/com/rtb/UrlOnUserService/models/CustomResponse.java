package com.rtb.UrlOnUserService.models;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CustomResponse {
    String code;
    String message;
}
