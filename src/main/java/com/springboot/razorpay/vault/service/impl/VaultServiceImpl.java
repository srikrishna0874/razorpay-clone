package com.springboot.razorpay.vault.service.impl;

import com.springboot.razorpay.common.entity.Money;
import com.springboot.razorpay.common.enums.CardBrand;
import com.springboot.razorpay.common.exception.ResourceNotFoundException;
import com.springboot.razorpay.common.util.RandomizerUtil;
import com.springboot.razorpay.payment.processor.PaymentProcessor;
import com.springboot.razorpay.payment.processor.PaymentProcessorRouter;
import com.springboot.razorpay.payment.processor.dto.PaymentProcessorRequest;
import com.springboot.razorpay.payment.processor.dto.PaymentProcessorResponse;
import com.springboot.razorpay.vault.config.VaultEncryptionConfig;
import com.springboot.razorpay.vault.dto.request.TokenizeRequest;
import com.springboot.razorpay.vault.dto.response.TokenizeResponse;
import com.springboot.razorpay.vault.entity.CardToken;
import com.springboot.razorpay.vault.entity.VaultCard;
import com.springboot.razorpay.vault.repository.CardTokenRepository;
import com.springboot.razorpay.vault.repository.VaultCardRepository;
import com.springboot.razorpay.vault.service.VaultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class VaultServiceImpl implements VaultService {

    private final CardTokenRepository cardTokenRepository;

    private final VaultCardRepository vaultCardRepository;

    private final BytesEncryptor dekEncryptor;

    private final PaymentProcessorRouter paymentProcessorRouter;

    @Override
    @Transactional
    public TokenizeResponse tokenize(TokenizeRequest tokenizeRequest, UUID merchantId) {

        String lastFour = tokenizeRequest.pan().substring(tokenizeRequest.pan().length() - 4);

        String bin = tokenizeRequest.pan().substring(0, 6);

        CardBrand cardBrand = detectBrand(tokenizeRequest.pan());

        byte[] dek = KeyGenerators.secureRandom(32).generateKey();
        byte[] encryptedPan = VaultEncryptionConfig.panEncryptor(dek)
                .encrypt(tokenizeRequest.pan().getBytes(StandardCharsets.UTF_8));

        byte[] encryptedDek = dekEncryptor.encrypt(dek);

        VaultCard savedVaultCard = vaultCardRepository.save(VaultCard.builder()
                .brand(cardBrand)
                .expiryYear(tokenizeRequest.expiryYear().toString())
                .expiryMonth(tokenizeRequest.expiryMonth().toString())
                .lastFour(lastFour)
                .bin(bin)
                .encryptedPan(encryptedPan)
                .encryptedDek(encryptedDek)
                .cardHolderName(tokenizeRequest.cardHolderName())
                .build());

        String token = "tck_" + RandomizerUtil.randomBase64(32);

        cardTokenRepository.save(CardToken.builder()
                .token(token)
                .card(savedVaultCard)
                .customer(tokenizeRequest.customerId())
                .merchant(merchantId)
                .build());

        return new TokenizeResponse(
                token,
                lastFour,
                cardBrand,
                tokenizeRequest.expiryMonth(),
                tokenizeRequest.expiryYear()
        );
    }

    @Override
    @Transactional
    public PaymentProcessorResponse charge(UUID paymentId, String token, Money amount,
                                           Map<String, Object> methodDetails) {

        CardToken cardToken = cardTokenRepository.findByTokenAndRevokedAtIsNull(token)
                .orElseThrow(() -> new ResourceNotFoundException("CardToken", token));

        VaultCard vaultCard = cardToken.getCard();

        byte[] panBytes = null;

        try {
            byte[] dek = dekEncryptor.decrypt(vaultCard.getEncryptedDek());

            panBytes = VaultEncryptionConfig.panEncryptor(dek).decrypt(vaultCard.getEncryptedPan());

            String pan = new String(panBytes, StandardCharsets.UTF_8);
            String expiry = vaultCard.getExpiryMonth() + "/" + vaultCard.getExpiryYear();

            PaymentProcessorRequest paymentProcessorRequest = PaymentProcessorRequest
                    .card(paymentId, pan, expiry, amount, methodDetails);

            log.info("Vault charge registered, token = {}****", token.substring(0, 4));


            return paymentProcessorRouter.charge(paymentProcessorRequest);

        } catch (Exception e) {
            log.warn("Vault charge failed, token = {}****", token.substring(0, 4));
            return new PaymentProcessorResponse.Failure("VAULT_CHARGE_FAILED", e.getMessage());
        } finally {
            if (panBytes != null)
                Arrays.fill(panBytes, (byte) 0);
        }

    }

    // Got from Google
    private CardBrand detectBrand(String pan) {
        if (pan.startsWith("4"))
            return CardBrand.VISA;
        if (pan.startsWith("5") || pan.startsWith("2"))
            return CardBrand.MASTERCARD;
        if (pan.startsWith("34") || pan.startsWith("37"))
            return CardBrand.AMEX;
        return CardBrand.RUPAY;
    }


}
