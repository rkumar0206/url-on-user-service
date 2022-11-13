package com.rtb.UrlOnUserService.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.util.StringUtils;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangeUserUsernameRequest {

    private String previousUsername;
    private String requestedUsername;
    private String uid;

    @JsonIgnore
    public boolean isRequestValid() {

        return StringUtils.hasLength(previousUsername.trim())
                && StringUtils.hasLength(requestedUsername.trim())
                && !previousUsername.trim().equals(requestedUsername.trim())
                && StringUtils.hasLength(uid.trim());

    }

}
