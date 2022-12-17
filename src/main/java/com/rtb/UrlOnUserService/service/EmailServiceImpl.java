package com.rtb.UrlOnUserService.service;

import com.rtb.UrlOnUserService.constantsAndEnums.Constants;
import com.rtb.UrlOnUserService.domain.ConfirmationToken;
import com.rtb.UrlOnUserService.domain.UserAccount;
import com.rtb.UrlOnUserService.repository.ConfirmationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final JavaMailSender javaMailSender;
    private final HttpServletRequest request;

    @Override
    public void sendConfirmationToken(UserAccount user) throws Exception {

        log.info("Creating confirmation toke url");

        ConfirmationToken confirmationToken;

        Optional<ConfirmationToken> tokenRepositoryByUserEmailId =
                confirmationTokenRepository.findByUserEmailId(user.getEmailId());

        if (tokenRepositoryByUserEmailId.isPresent()) {

            confirmationToken = tokenRepositoryByUserEmailId.get();
        } else {

            confirmationToken = new ConfirmationToken(user.getEmailId());
            confirmationTokenRepository.save(confirmationToken);
        }

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

        simpleMailMessage.setTo(user.getEmailId());
        simpleMailMessage.setSubject(Constants.CONFIRMATION_EMAIL_SUBJECT);
        simpleMailMessage.setFrom(Constants.EMAIL_FROM);
        simpleMailMessage.setText("To verify your account from UrlOn application please click on below link\n" +
                "http://localhost:8004" + "/urlon/api/users/account/verify?token=" + confirmationToken.getConfirmationToken());

        sendMail(simpleMailMessage);

    }

    @Override
    public SimpleMailMessage sendPasswordResetUrl(UserAccount user, String resetPasswordUrl) throws Exception {

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
        return simpleMailMessage;
    }

    @Override
    public void sendMail(SimpleMailMessage mailMessage) throws Exception {

        try {

            new Thread(() -> {

                log.info("Sending mail...");
                javaMailSender.send(mailMessage);
                log.info("Mail sent successfully...");
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            log.info("Mail not sent!!");
            throw e;
        }
    }
}
