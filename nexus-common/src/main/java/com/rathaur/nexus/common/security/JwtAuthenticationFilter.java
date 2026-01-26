package com.rathaur.nexus.common.security;

import com.rathaur.nexus.common.utils.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final HandlerExceptionResolver resolver;

    public JwtAuthenticationFilter(JwtUtils jwtUtils,
                                   @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.jwtUtils = jwtUtils;
        this.resolver = resolver;
    }

    /**
     * SUCCESSFUL OVERRIDE: Note the specific Jakarta HttpServletRequest.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        return path.startsWith("/auth/register") ||
                path.startsWith("/auth/token") ||
                path.startsWith("/auth/validate") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader(SecurityConstants.AUTH_HEADER);

        // If no token or not a bearer token, just continue the chain
        if (authHeader == null || !authHeader.startsWith(SecurityConstants.BEARER)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(SecurityConstants.BEARER.length());

        try {
            Claims claims = jwtUtils.parseAndValidate(token);
            String tokenType = (String) claims.get(SecurityConstants.CLAIM_TOKEN_TYPE);

            if (!SecurityConstants.TOKEN_TYPE_ACCESS.equals(tokenType)) {
                filterChain.doFilter(request, response);
                return;
            }

            String username = claims.getSubject();

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) claims.get(SecurityConstants.CLAIM_ROLES);

                var authorities = (roles == null ? List.<String>of() : roles).stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                var authToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException | SignatureException | io.jsonwebtoken.MalformedJwtException e) {
            log.error("NEXUS-SECURITY: JWT validation failed: {}", e.getMessage());
            // This sends it back to your @RestControllerAdvice
            resolver.resolveException(request, response, null, e);
        } catch (Exception e) {
            log.error("NEXUS-SECURITY: Unexpected filter error", e);
            resolver.resolveException(request, response, null, e);
        }
    }
}