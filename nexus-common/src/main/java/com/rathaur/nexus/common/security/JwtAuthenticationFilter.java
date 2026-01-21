package com.rathaur.nexus.common.security;

import com.rathaur.nexus.common.utils.SecurityConstants;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.List;

/**
 * @author Tanuj Singh Rathaur
 * @date 1/17/2026
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader(SecurityConstants.AUTH_HEADER);

        if (authHeader == null || !authHeader.startsWith(SecurityConstants.BEARER)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(SecurityConstants.BEARER.length());

        try {
            /* 1. Validate and Parse Claims */
            Claims claims = jwtUtils.parseAndValidate(token);

            /* 2. SaaS Best Practice: Ensure this is an ACCESS token, not a REFRESH token */
            String tokenType = (String) claims.get(SecurityConstants.CLAIM_TOKEN_TYPE);
            if (!SecurityConstants.TOKEN_TYPE_ACCESS.equals(tokenType)) {
                filterChain.doFilter(request, response);
                return;
            }

            String username = claims.getSubject();

            /* 3. Authenticate if not already authenticated in this thread */
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) claims.get(SecurityConstants.CLAIM_ROLES);

                var authorities = (roles == null ? List.<String>of() : roles).stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                /* Use a proper Authentication object with authorities */
                var authToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            /* Production Tip: In a SaaS, you might want to clear the context
               if a malformed token is provided.
            */
            SecurityContextHolder.clearContext();
            logger.error("Could not set user authentication in security context", e);
        }

        filterChain.doFilter(request, response);
    }
}