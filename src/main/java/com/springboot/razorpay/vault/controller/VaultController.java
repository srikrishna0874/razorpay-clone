package com.springboot.razorpay.vault.controller;

import com.springboot.razorpay.vault.dto.request.TokenizeRequest;
import com.springboot.razorpay.vault.dto.response.TokenizeResponse;
import com.springboot.razorpay.vault.service.VaultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/v1/vault")
public class VaultController {

    UUID merchantId = UUID.fromString("bf141135-65eb-4276-9226-f1d38ac77baf"); // TODO : Replace with MerchantContext

    private final VaultService vaultService;

    @PostMapping("/tokenize")
    public ResponseEntity<TokenizeResponse> tokenize(@Valid @RequestBody TokenizeRequest tokenizeRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vaultService.tokenize(tokenizeRequest, merchantId));
    }
}
