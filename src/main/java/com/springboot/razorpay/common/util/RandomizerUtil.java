package com.springboot.razorpay.common.util;

import java.security.SecureRandom;
import java.util.Base64;

public class RandomizerUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String randomBase64(int length) {

        byte[] buffer = new byte[length];
        SECURE_RANDOM.nextBytes(buffer); // creates random byte numbers ranges from [-128, 127]

        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer);

    }
}
