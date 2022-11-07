package com.rtb.UrlOnUserService.service;

import com.rtb.UrlOnUserService.constantsAndEnums.Constants;
import com.rtb.UrlOnUserService.domain.ConfirmationToken;
import com.rtb.UrlOnUserService.domain.UrlOnUser;
import com.rtb.UrlOnUserService.repository.ConfirmationTokenRepository;
import com.rtb.UrlOnUserService.util.Utility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final JavaMailSender javaMailSender;
    private final HttpServletRequest request;

    @Override
    public void sendConfirmationToken(UrlOnUser user) throws RuntimeException {

        log.info("Creating confirmation toke url");

        ConfirmationToken confirmationToken = new ConfirmationToken(user.getEmailId());

        if (confirmationTokenRepository.findByUserEmailId(user.getEmailId()).isPresent()) {

            confirmationToken = confirmationTokenRepository.findByUserEmailId(user.getEmailId()).get();
        } else {

            confirmationTokenRepository.save(confirmationToken);
        }

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

        simpleMailMessage.setTo(user.getEmailId());
        simpleMailMessage.setSubject(Constants.CONFIRMATION_EMAIL_SUBJECT);
        simpleMailMessage.setFrom(Constants.EMAIL_FROM);
        simpleMailMessage.setText("To verify your account from UrlOn application please click on below link\n" +
                Utility.getSiteUrl(request) + "/urlon/api/users/account/verify?token=" + confirmationToken.getConfirmationToken());

        sendMail(simpleMailMessage);

    }

    @Override
    public void sendPasswordResetUrl(UrlOnUser user, String resetPasswordUrl) throws RuntimeException {


        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

        simpleMailMessage.setTo(user.getEmailId());
        simpleMailMessage.setSubject(Constants.PASSWORD_RESET_EMAIL_SUBJECT);
        simpleMailMessage.setFrom(Constants.EMAIL_FROM);

        simpleMailMessage.setText("Hello\n\n" +
                "You have requested to reset your password.\n\n" +
                "To reset your password please follow the below link\n\n" +
                resetPasswordUrl + "\n\n" +
                "Link is valid for 10 minutes\n\n" +
                "Ignore this email if you do remember your password, or you have not made the request.");

        sendMail(simpleMailMessage);
    }

    @Override
    public void sendMail(SimpleMailMessage mailMessage) throws RuntimeException {

        try {

            log.info("Sending mail...");

            javaMailSender.send(mailMessage);

            log.info("Mail sent successfully...");

        } catch (Exception e) {
            e.printStackTrace();
            log.info("Mail not sent!!");
            throw new RuntimeException(e);
        }
    }
}
