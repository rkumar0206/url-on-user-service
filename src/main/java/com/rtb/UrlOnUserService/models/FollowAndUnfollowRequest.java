package com.rtb.UrlOnUserService.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.util.StringUtils;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FollowAndUnfollowRequest {

    private String followingUid;
    private Long followedOn;

    @JsonIgnore
    public boolean isRequestValid() {

        return followingUid != null && StringUtils.hasLength(followingUid.trim());
    }
}
