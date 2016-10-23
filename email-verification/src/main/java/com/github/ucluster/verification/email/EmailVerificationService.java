package com.github.ucluster.verification.email;

import com.github.ucluster.verification.VerificationException;
import com.github.ucluster.verification.VerificationService;

import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * EmailConfirmService
 * <p>
 * Send confirmation token, and verify it later
 * <p>
 * https://www.mkyong.com/java/javamail-api-sending-email-via-gmail-smtp-example/
 */
public class EmailVerificationService implements VerificationService {
    @Inject
    com.github.ucluster.session.Session session;

    private String username;
    private String password;
    private Properties properties;

    public EmailVerificationService() {

    }

    public EmailVerificationService(String username, String password, Properties properties) {
        this.username = username;
        this.password = password;
        this.properties = properties;
    }

    @Override
    public void send(String target, String token) {
        try {
            session.setex(getTargetKey(target), token, getExpireSeconds());
            final MimeMessage message = new MimeMessage(get());
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(target));
            message.setSubject("Confirmation Code");
            message.setText(token);
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void verify(String target, String token) {
        final Optional<Object> value = session.get(getTargetKey(target));

        if (!value.isPresent() || !Objects.equals(value.get(), token)) {
            throw new VerificationException();
        }
    }

    private Session get() {
        return Session.getDefaultInstance(properties,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
    }

    private int getExpireSeconds() {
        return 60 * 30;
    }

    private String getTargetKey(String target) {
        return "confirm:" + target;
    }
}
