package com.rtb.UrlOnUserService.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangeUserEmailIdRequest {

    private String previousEmailId;
    private String requestedEmailId;
    private String uid;

    public boolean isRequestValid() {

        return previousEmailId != null && !previousEmailId.trim().equals("")
                && requestedEmailId != null && !requestedEmailId.trim().equals("")
                && !previousEmailId.trim().equals(requestedEmailId.trim())
                && uid != null && !uid.trim().equals("");
    }

}
