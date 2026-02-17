package com.planotech.plano.helper;

import com.planotech.plano.enums.EmailType;
import com.planotech.plano.exception.MailServerException;
import com.planotech.plano.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class EmailSender {

    @Autowired
    JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    SpringTemplateEngine templateEngine;
//
//    @Async
//    public CompletableFuture<Boolean> sendVerificationEmail(
//            User user,
//            EmailType emailType,
//            Map<String, Object> variables) {
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper =
//                    new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
//
//            helper.setFrom(fromEmail, "Registration");
//            helper.setTo(user.getEmail());
//            helper.setSubject(emailType.getSubject());
//
//            // Thymeleaf context
//            org.thymeleaf.context.Context context =
//                    new org.thymeleaf.context.Context();
//            context.setVariables(variables);
//
//            // Process template
//            String htmlBody = templateEngine.process(
//                    emailType.getTemplate().replace(".html", ""),
//                    context
//            );
//
//            helper.setText(htmlBody, true);
//
//            mailSender.send(message);
//            return CompletableFuture.completedFuture(true);
//
//        } catch (Exception e) {
//            return CompletableFuture.completedFuture(false);
//        }
//    }

    @Async
    public CompletableFuture<Boolean> sendVerificationEmail(
            User user,
            EmailType emailType,
            Map<String, Object> variables,
            Map<String, byte[]> inlineImages) {

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setFrom(fromEmail, "Registration");
            helper.setTo(user.getEmail());
            helper.setSubject(emailType.getSubject());

            // Thymeleaf context
            Context context = new Context();
            context.setVariables(variables);

            String htmlBody = templateEngine.process(
                    emailType.getTemplate().replace(".html", ""),
                    context
            );

            helper.setText(htmlBody, true);

            if (inlineImages != null) {
                for (Map.Entry<String, byte[]> entry : inlineImages.entrySet()) {
                    helper.addInline(
                            entry.getKey(),
                            new ByteArrayResource(entry.getValue()),
                            "image/png"
                    );
                }
            }

            mailSender.send(message);
            return CompletableFuture.completedFuture(true);

        } catch (Exception e) {
            return CompletableFuture.completedFuture(false);
        }
    }


}
