package com.rtb.UrlOnUserService.service;

import com.rtb.UrlOnUserService.domain.UrlOnUser;
import org.springframework.mail.SimpleMailMessage;

public interface EmailService {

    void sendConfirmationToken(UrlOnUser user);

    void sendPasswordResetUrl(UrlOnUser user);
    void sendMail(SimpleMailMessage mailMessage);
}
