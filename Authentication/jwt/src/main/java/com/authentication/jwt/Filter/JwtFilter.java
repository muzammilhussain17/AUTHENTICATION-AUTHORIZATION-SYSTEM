package com.authentication.jwt.Filter;


import com.authentication.jwt.Service.AppUserDetailService;
import com.authentication.jwt.Service.TokenBlacklistService;
import com.authentication.jwt.Util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final AppUserDetailService appUserDetailService;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    private static final List<String> publicUrls = List.of(
            "/login", "/profile/register", "/send-reset-otp", "/reset-password", "/refresh"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getServletPath();
        if (publicUrls.contains(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = null;
        String email = null;

        // Check the Authorization header
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            email = jwtUtil.extractEmail(jwt);
        }

        // If not found in header, check the cookie
        if (jwt == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("token".equals(cookie.getName())) {
                        jwt = cookie.getValue();
                        break;
                    }
                }
            }
        }

        // Check if token is blacklisted (logged out)
        if (jwt != null && tokenBlacklistService.isBlacklisted(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Validate the token and set security context
        if (jwt != null) {
            email = jwtUtil.extractEmail(jwt);
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = appUserDetailService.loadUserByUsername(email);
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
