package com.rtb.UrlOnUserService.service;

import com.rtb.UrlOnUserService.domain.UrlOnUser;
import org.springframework.mail.SimpleMailMessage;

public interface EmailService {

    void sendConfirmationToken(UrlOnUser user) throws Exception;

    SimpleMailMessage sendPasswordResetUrl(UrlOnUser user, String resetPasswordUrl) throws Exception;

    void sendMail(SimpleMailMessage mailMessage) throws Exception;
}
