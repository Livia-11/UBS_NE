package com.ubs.billing.service;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class OtpGenerator {
    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        return String.valueOf(100000 + secureRandom.nextInt(900000));
    }
}
