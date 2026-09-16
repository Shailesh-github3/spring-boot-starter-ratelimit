package com.shailesh.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet filter that enforces rate limits per identity.
 * Requests without the configured identity header are passed through untouched.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String ERROR_BODY = "{\"error\": \"Rate limit exceeded\"}";
    private static final String HEADER_LIMIT = "X-RateLimit-Limit";
    private static final String HEADER_REMAINING = "X-RateLimit-Remaining";
    private static final String HEADER_RETRY_AFTER = "Retry-After";

    private final RateLimitService rateLimitService;
    private final String headerName;

    public RateLimitFilter(RateLimitService rateLimitService, String headerName) {
        this.rateLimitService = rateLimitService;
        this.headerName = headerName;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String identity = request.getHeader(headerName);
        if (identity == null || identity.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        RateLimitResult result = rateLimitService.tryConsume(request, identity);

        response.setHeader(HEADER_LIMIT, String.valueOf(result.getLimit()));
        response.setHeader(HEADER_REMAINING, String.valueOf(result.getRemaining()));

        if (result.isAllowed()) {
            filterChain.doFilter(request, response);
        } else {
            response.setHeader(HEADER_RETRY_AFTER, String.valueOf(result.getRetryAfterSeconds()));
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(ERROR_BODY);
        }
    }
}