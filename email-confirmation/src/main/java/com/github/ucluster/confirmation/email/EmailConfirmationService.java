package com.github.ucluster.confirmation.email;


import com.github.ucluster.confirmation.ConfirmationException;
import com.github.ucluster.confirmation.ConfirmationService;

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
public class EmailConfirmationService implements ConfirmationService {
    @Inject
    com.github.ucluster.session.Session session;

    private String username;
    private String password;
    private Properties properties;

    public EmailConfirmationService() {

    }

    public EmailConfirmationService(String username, String password, Properties properties) {
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
    public void confirm(String target, String token) {
        final Optional<Object> value = session.get(getTargetKey(target));

        if (!value.isPresent() || !Objects.equals(value.get(), token)) {
            throw new ConfirmationException();
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
