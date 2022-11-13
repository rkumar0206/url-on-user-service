package com.rtb.UrlOnUserService.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.util.StringUtils;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangeUserEmailIdRequest {

    private String previousEmailId;
    private String requestedEmailId;
    private String uid;

    @JsonIgnore
    public boolean isRequestValid() {

        return StringUtils.hasLength(previousEmailId.trim())
                && StringUtils.hasLength(requestedEmailId.trim())
                && !previousEmailId.trim().equals(requestedEmailId.trim())
                && StringUtils.hasLength(uid.trim());
    }

}
