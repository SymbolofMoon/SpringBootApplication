package com.practice.project.SpringBootApplication.config;


import com.practice.project.SpringBootApplication.entity.User;
import com.practice.project.SpringBootApplication.repository.UserRepository;
import com.practice.project.SpringBootApplication.service.UserService;
import com.practice.project.SpringBootApplication.utility.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.http.Cookie;

import java.util.Arrays;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws java.io.IOException, jakarta.servlet.ServletException {
//        String authHeader = request.getHeader("Authorization");

        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/api/users/register") || requestURI.startsWith("/api/users/login") || requestURI.startsWith("/login")) {
            chain.doFilter(request, response);  // Continue the filter chain without checking JWT
            return;
        }

        String token = null;
        if (request.getCookies() != null) {
            token = Arrays.stream(request.getCookies())
                    .filter(cookie -> "jwtToken".equals(cookie.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }

        if (token == null || token.isEmpty()) {
            // If token is null or empty, set status as Unauthorized and send response
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Unauthorized: Please login");
            logger.warn("Unauthorized: Please login");
            return;
        }



        if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username, null, Collections.emptyList());

                SecurityContextHolder.getContext().setAuthentication(authentication);
        }else {
                // If token is invalid, return Unauthorized response
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("Unauthorized: Please login");
                logger.warn("Unauthorized: Please login");
                return;
        }

        chain.doFilter(request, response);
    }
}
