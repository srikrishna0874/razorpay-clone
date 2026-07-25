package com.springboot.razorpay.vault.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class VaultEncryptionConfig {

    @Value("${vault.master-key}")
    private String masterKey;

    public static BytesEncryptor panEncryptor(byte[] dek) {
        SecretKeySpec dekKey = new SecretKeySpec(dek, "AES");
        return new AesBytesEncryptor(dekKey, KeyGenerators.secureRandom(12),
                AesBytesEncryptor.CipherAlgorithm.GCM);
    }

    @Bean
    public BytesEncryptor dekEncryptor() {
        byte[] masterKeyBytes = Base64.getDecoder().decode(masterKey);

        SecretKeySpec masterDekKey = new SecretKeySpec(masterKeyBytes, "AES");

        return new AesBytesEncryptor(masterDekKey, KeyGenerators.secureRandom(12),
                AesBytesEncryptor.CipherAlgorithm.GCM);
    }
}
