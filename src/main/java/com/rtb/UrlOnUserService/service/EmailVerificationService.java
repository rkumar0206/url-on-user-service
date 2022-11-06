package com.rtb.UrlOnUserService.service;

import com.rtb.UrlOnUserService.constantsAndEnums.AccountVerificationMessage;
import com.rtb.UrlOnUserService.domain.UrlOnUser;
import org.springframework.mail.SimpleMailMessage;

public interface EmailVerificationService {

    void sendConfirmationToken(UrlOnUser user);
    void sendMail(SimpleMailMessage mailMessage);
}
