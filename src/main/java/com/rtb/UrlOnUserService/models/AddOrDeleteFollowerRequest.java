package com.rtb.UrlOnUserService.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.util.StringUtils;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddOrDeleteFollowerRequest {

    private String userUid;
    private String followerUid;
    private Long followedOn;

    @JsonIgnore
    public boolean isRequestValid() {

        return userUid != null && StringUtils.hasLength(userUid.trim())
                && followerUid != null && StringUtils.hasLength(followerUid.trim());
    }
}
