package com.springboot.razorpay.merchant.service.impl;

import com.springboot.razorpay.common.enums.MerchantStatus;
import com.springboot.razorpay.common.enums.UserRole;
import com.springboot.razorpay.common.exception.DuplicateResourceException;
import com.springboot.razorpay.merchant.dto.request.MerchantSignupRequest;
import com.springboot.razorpay.merchant.dto.response.MerchantResponse;
import com.springboot.razorpay.merchant.entity.AppUser;
import com.springboot.razorpay.merchant.entity.Merchant;
import com.springboot.razorpay.merchant.mapper.MerchantMapper;
import com.springboot.razorpay.merchant.repository.AppUserRepository;
import com.springboot.razorpay.merchant.repository.MerchantRepository;
import com.springboot.razorpay.merchant.service.AuthService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;

    private final MerchantRepository merchantRepository;

    private final MerchantMapper merchantMapper;

    @Override
    @Transactional
    public MerchantResponse signUp(MerchantSignupRequest merchantSignupRequest) {
        if (merchantRepository.existsByEmail(merchantSignupRequest.email())) {
            throw new DuplicateResourceException("DUPLICATE_MERCHANT",
                    "Merchant with email already exists!" + merchantSignupRequest.email());
        }

        Merchant merchant = merchantMapper.toEntityFromSignUpRequest(merchantSignupRequest);
        merchant.setStatus(MerchantStatus.PENDING_KYC);

        merchant = merchantRepository.save(merchant);

        AppUser appUser = AppUser.builder()
                .email(merchantSignupRequest.email())
                .merchant(merchant)
                .passwordHash(merchantSignupRequest.password()) // TODO: Encrypt using BCrypt
                .role(UserRole.OWNER)
                .build();

        appUserRepository.save(appUser);

        return merchantMapper.toMerchantResponse(merchant);
    }
}
