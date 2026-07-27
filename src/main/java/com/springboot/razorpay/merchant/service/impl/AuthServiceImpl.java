package com.springboot.razorpay.merchant.service.impl;

import com.springboot.razorpay.common.enums.MerchantStatus;
import com.springboot.razorpay.common.enums.UserRole;
import com.springboot.razorpay.common.exception.DuplicateResourceException;
import com.springboot.razorpay.common.exception.ResourceNotFoundException;
import com.springboot.razorpay.merchant.dto.request.LoginRequest;
import com.springboot.razorpay.merchant.dto.request.MerchantSignupRequest;
import com.springboot.razorpay.merchant.dto.response.LoginResponse;
import com.springboot.razorpay.merchant.dto.response.MerchantResponse;
import com.springboot.razorpay.merchant.entity.AppUser;
import com.springboot.razorpay.merchant.entity.Merchant;
import com.springboot.razorpay.merchant.mapper.MerchantMapper;
import com.springboot.razorpay.merchant.repository.AppUserRepository;
import com.springboot.razorpay.merchant.repository.MerchantRepository;
import com.springboot.razorpay.merchant.security.JwtUtil;
import com.springboot.razorpay.merchant.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

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
                .passwordHash(passwordEncoder.encode(merchantSignupRequest.password()))
                .role(UserRole.OWNER)
                .build();

        appUserRepository.save(appUser);

        return merchantMapper.toMerchantResponse(merchant);
    }

    @Override
    public LoginResponse login(LoginRequest merchantLoginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        merchantLoginRequest.email(),
                        merchantLoginRequest.password())
        );

        AppUser appUser = appUserRepository.findByEmail(merchantLoginRequest.email())
                .orElseThrow(() -> new ResourceNotFoundException("User", merchantLoginRequest.email()));

        String token = jwtUtil.generateAccessToken(appUser.getEmail(), appUser.getMerchant().getId(),
                appUser.getRole().toString());

        return new LoginResponse(token);

    }
}
