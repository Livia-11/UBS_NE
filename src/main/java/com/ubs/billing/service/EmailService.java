package com.ubs.billing.service;

import com.ubs.billing.enums.UserRole;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String loginUrl;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String fromAddress,
            @Value("${app.frontend.login-url}") String loginUrl
    ) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.loginUrl = loginUrl;
    }

    public void sendTemporaryCredentials(String to, String fullNames, String temporaryPassword, Set<UserRole> roles) {
        String roleText = rolesText(roles);
        send(
                to,
                "Utility Billing System Account Created",
                """
                        Dear %s,

                        Your Utility Billing System account has been created.

                        Username/Email: %s
                        Temporary Password: %s
                        Assigned Role(s): %s

                        Access the system here: %s

                        For security, you must change this temporary password immediately after your first successful login.

                        Role responsibilities:
                        %s

                        Regards,
                        Utility Billing System
                        """.formatted(fullNames, to, temporaryPassword, roleText, loginUrl, responsibilities(roles))
        );
    }

    public void sendRoleChanged(String to, String fullNames, Set<UserRole> roles) {
        send(
                to,
                "Utility Billing System Role Updated",
                """
                        Dear %s,

                        Your Utility Billing System access role has been updated.

                        New Role(s): %s

                        Associated responsibilities:
                        %s

                        If you did not expect this change, please contact the administrator.

                        Regards,
                        Utility Billing System
                        """.formatted(fullNames, rolesText(roles), responsibilities(roles))
        );
    }

    public void sendWelcomeCustomerSignup(String to, String fullNames) {
        send(
                to,
                "Welcome to Utility Billing System",
                """
                        Dear %s,

                        Your customer account has been created successfully.

                        Username/Email: %s
                        Assigned Role: ROLE_CUSTOMER
                        Access the system here: %s

                        You can now view bills, notifications, and payment history.

                        Regards,
                        Utility Billing System
                        """.formatted(fullNames, to, loginUrl)
        );
    }

    public void sendCustomerSignupOtp(String to, String fullNames, String otp) {
        send(
                to,
                "Verify Your Utility Billing System Account",
                """
                        Dear %s,

                        Thank you for registering as a Utility Billing System customer.

                        Your verification OTP is: %s

                        This OTP expires in 10 minutes. Verify your account before logging in.

                        Regards,
                        Utility Billing System
                        """.formatted(fullNames, otp)
        );
    }

    private void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    private String rolesText(Set<UserRole> roles) {
        return roles.stream().map(Enum::name).sorted().collect(Collectors.joining(", "));
    }

    private String responsibilities(Set<UserRole> roles) {
        return roles.stream()
                .map(this::responsibility)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private String responsibility(UserRole role) {
        return switch (role) {
            case ROLE_ADMIN -> "- ROLE_ADMIN: configure tariffs, manage users, and approve bills.";
            case ROLE_OPERATOR -> "- ROLE_OPERATOR: capture valid monthly meter readings.";
            case ROLE_FINANCE -> "- ROLE_FINANCE: approve bills and record customer payments.";
            case ROLE_CUSTOMER -> "- ROLE_CUSTOMER: view bills, notifications, and payment history.";
        };
    }
}
