package com.springboot.razorpay.merchant.security;

import com.springboot.razorpay.common.exception.RateLimitException;
import com.springboot.razorpay.common.ratelimit.RateLimitResult;
import com.springboot.razorpay.common.ratelimit.RateLimiter;
import com.springboot.razorpay.merchant.cache.ApiKeyCache;
import com.springboot.razorpay.merchant.cache.ApiKeyCacheEntry;
import com.springboot.razorpay.merchant.entity.ApiKey;
import com.springboot.razorpay.merchant.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String BASIC_PREFIX = "Basic ";

    private final ApiKeyRepository apiKeyRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    private final MerchantContext merchantContext;

    private final HandlerExceptionResolver handlerExceptionResolver;

    private final ApiKeyCache apiKeyCache;

    private final RateLimiter rateLimiter;

    @Value("${app.rate-limit.use-case.api-key.requests-per-minute:60}")
    private Integer requestsPerMinute;

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

            String[] credentials = decodeApiKey(header);

            if (credentials == null) {
                throw new BadCredentialsException("Malformed API Key Header");
            }

            String keyId = credentials[0];
            String rawSecret = credentials[1];

            ApiKeyCacheEntry apiKeyCacheEntry =
                    apiKeyCache.getApiKeyByKeyId(keyId).orElseGet(() -> loadAndCache(keyId));

            if (apiKeyCacheEntry == null || !apiKeyCacheEntry.enabled() ||
                    !isSecretKeyValid(rawSecret, apiKeyCacheEntry)) {
                throw new BadCredentialsException("Invalid or missing API Key");
            }

            RateLimitResult rateLimitResult = rateLimiter.checkRateLimit("apikey:" + keyId, requestsPerMinute, 60);

            if (!rateLimitResult.isAllowed()) {
                throw new RateLimitException("Too many requests", rateLimitResult.retryAfterSeconds());
            }

            response.setHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(rateLimitResult.remaining()));

            var auth = new UsernamePasswordAuthenticationToken(keyId, null,
                    List.of(new SimpleGrantedAuthority("API_KEY_ROLE"))
            );

            SecurityContextHolder.getContext().setAuthentication(auth);

            merchantContext.setMerchantId(apiKeyCacheEntry.merchantId());
            merchantContext.setKeyId(apiKeyCacheEntry.keyId());

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        }

    }

    private ApiKeyCacheEntry loadAndCache(String keyId) {
        ApiKey apiKey = apiKeyRepository.findByKeyId(keyId).orElse(null);

        if (apiKey == null) {
            return null;
        } else {
            ApiKeyCacheEntry apiKeyCacheEntry = new ApiKeyCacheEntry(
                    keyId,
                    apiKey.getKeySecretHash(),
                    apiKey.getPreviousKeySecretHash(),
                    apiKey.getGracePeriodExpiresAt(),
                    apiKey.getMerchant().getId(),
                    apiKey.getEnvironment(),
                    apiKey.isEnabled()
            );

            apiKeyCache.putApiKeyToCache(keyId, apiKeyCacheEntry);

            return apiKeyCacheEntry;
        }
    }

    private boolean isSecretKeyValid(String rawSecret, ApiKeyCacheEntry apiKey) {
        if (bCryptPasswordEncoder.matches(rawSecret, apiKey.keySecretHash()))
            return true;

        return apiKey.isInGracePeriod() && apiKey.previousKeySecretKeyHash() != null &&
                bCryptPasswordEncoder.matches(rawSecret, apiKey.previousKeySecretKeyHash());
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