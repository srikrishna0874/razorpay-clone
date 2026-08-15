package com.springboot.razorpay.common.idempotency;

import com.springboot.razorpay.common.exception.IdempotencyConflictException;
import com.springboot.razorpay.merchant.security.MerchantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final Set<String> GUARDED_METHODS = Set.of("POST", "PUT", "PATCH");
    private static final Duration IN_PROGRESS_TTL = Duration.ofSeconds(30);
    private static final Duration COMPLETED_TTL = Duration.ofHours(24);
    private static final String SEPARATOR = "|";

    private final MerchantContext merchantContext;

    private final IdempotencyStore idempotencyStore;

    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!GUARDED_METHODS.contains(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String idempotencyKey = request.getHeader("X-Idempotency-Key");

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        UUID merchantId = merchantContext.getMerchantId();
        String uniqueIdempotencyKey = merchantId != null ? merchantId + ":" + idempotencyKey : idempotencyKey;

        boolean claimed = idempotencyStore.setIdempotencyKeyIfAbsent(uniqueIdempotencyKey, IN_PROGRESS_TTL);

        if (!claimed) {
            // Another thread might already claimed this key
            Optional<String> existingIdempotencyValue = idempotencyStore.getIdempotencyKeyValue(uniqueIdempotencyKey);

            if (existingIdempotencyValue.isPresent() &&
                    !IdempotencyStore.IN_PROGRESS.equals(existingIdempotencyValue.get())) {
                // If it's not in progress, but coming from the actual value stored in redis
                replay(request, response, existingIdempotencyValue.get());
            } else {
                // It's still in progress by another thread
                IdempotencyConflictException exception =
                        new IdempotencyConflictException("A request with this idempotency key is in progress");
                handlerExceptionResolver.resolveException(request, response, null, exception);
            }
            return;
        }

        // First time claim
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(request, responseWrapper);
        } finally {
            int status = responseWrapper.getStatus();
            byte[] bodyBytes = responseWrapper.getContentAsByteArray();

            String body = new String(bodyBytes, StandardCharsets.UTF_8);

            if (status < 400 && bodyBytes.length > 0) {
                // Success --> store the completed response for future replays
                String stored = status + SEPARATOR + body;
                idempotencyStore.storeIdempotencyKey(uniqueIdempotencyKey, stored, COMPLETED_TTL);

                log.debug("IdempotencyFilter: Stored response status = {}, key = {}", status, uniqueIdempotencyKey);
            } else {
                // Error or empty --> Delete placeholder so client can retry
                idempotencyStore.deleteIdempotencyKey(uniqueIdempotencyKey);
                log.debug("IdempotencyFilter: Deleted placeholder after error status = {}, key = {}", status,
                        uniqueIdempotencyKey);
            }

            // Always flush buffered body to the actual response
            // If this is skipped, the client receives ab empty body
            responseWrapper.copyBodyToResponse();
        }


    }

    private void replay(HttpServletRequest request, HttpServletResponse response, String idempotencyKeyValue)
            throws IOException {
        int separatorIndex = idempotencyKeyValue.indexOf(SEPARATOR);

        if (separatorIndex < 0) {
            var exception =
                    new IdempotencyConflictException("A request with this idempotency key is in progress");
            handlerExceptionResolver.resolveException(request, response, null, exception);
            return;
        }

        int status = Integer.parseInt(idempotencyKeyValue.substring(0, separatorIndex));

        String body = idempotencyKeyValue.substring(separatorIndex + 1);

        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
        response.getOutputStream().flush();
        log.debug("IdempotencyFilter: Replayed response status = {}", status);
    }
}
