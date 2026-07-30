package com.springboot.razorpay.merchant.security;

import com.springboot.razorpay.merchant.entity.ApiKey;
import com.springboot.razorpay.merchant.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String BASIC_PREFIX = "Basic ";

    private final ApiKeyRepository apiKeyRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    private final MerchantContext merchantContext;

    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        log.info("Incoming request: {}", request.getRequestURI());

        try {
            String header = request.getHeader("Authorization");

            if (header == null || !header.startsWith(BASIC_PREFIX)) {
                filterChain.doFilter(request, response);
                return;
            }

            String apiKey = header.substring(BASIC_PREFIX.length()).trim();

            String[] credentials = decodeApiKey(header);

            if (credentials == null) {
                throw new BadCredentialsException("Malformed API Key Header");
            }

            String keyId = credentials[0];
            String rawSecret = credentials[1];

            ApiKey apiKeyFromDb = apiKeyRepository.findByKeyId(keyId)
                    .orElseThrow(() -> new BadCredentialsException("Invalid or missing API Key"));

            if (!apiKeyFromDb.isEnabled() || !isSecretKeyValid(rawSecret, apiKeyFromDb)) {
                throw new BadCredentialsException("Invalid or missing API Key");
            }

            var auth = new UsernamePasswordAuthenticationToken(keyId, null,
                    List.of(new SimpleGrantedAuthority("API_KEY_ROLE"))
            );

            SecurityContextHolder.getContext().setAuthentication(auth);

            merchantContext.setMerchantId(apiKeyFromDb.getMerchant().getId());
            merchantContext.setKeyId(apiKeyFromDb.getKeyId());

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        }

    }

    private boolean isSecretKeyValid(String rawSecret, ApiKey apiKey) {
        if (bCryptPasswordEncoder.matches(rawSecret, apiKey.getKeySecretHash()))
            return true;

        boolean isInGracePeriod = apiKey.getGracePeriodExpiresAt() != null &&
                LocalDateTime.now().isBefore(apiKey.getGracePeriodExpiresAt());

        return isInGracePeriod && apiKey.getPreviousKeySecretHash() != null &&
                bCryptPasswordEncoder.matches(rawSecret, apiKey.getPreviousKeySecretHash());
    }

    private String[] decodeApiKey(String header) {
        String encodedApiKey = header.substring(BASIC_PREFIX.length());

        String decodedApiKey = new String(Base64.getDecoder().decode(encodedApiKey), StandardCharsets.UTF_8);

        int colon = decodedApiKey.indexOf(':');

        if (colon < 1)
            return null;

        return new String[]{decodedApiKey.substring(0, colon), decodedApiKey.substring(colon + 1)};
    }
}