package com.planotech.plano.auth;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.planotech.plano.auth.SecurityConstants.PUBLIC_URLS;

@Service
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    JwtService jwtService;

    @Autowired
    ApplicationContext context;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            String authToken = request.getHeader("Authorization");
            String path = request.getServletPath();
            if (isPublicUrl(path)) {
                filterChain.doFilter(request, response);
                return;
            }
            if (authToken == null || !authToken.startsWith("Bearer ")) {
                sendJwtError(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "JWT token is missing");
                return;
            }
            String token = null;
            String username = null;
            if (authToken.startsWith("Bearer")) {
                token = authToken.substring(7);
                username = jwtService.extractUsername(token);
            }
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = context.getBean(MyUserDetailService.class).loadUserByUsername(username);
                if (jwtService.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            sendJwtError(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT token expired");
        }
        catch (SignatureException e) {
            sendJwtError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT signature");
        }
        catch (MalformedJwtException e) {
            sendJwtError(response, HttpServletResponse.SC_UNAUTHORIZED, "Malformed JWT token");
        }
        catch (UnsupportedJwtException e) {
            sendJwtError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unsupported JWT token");
        }
        catch (IllegalArgumentException e) {
            sendJwtError(response, HttpServletResponse.SC_BAD_REQUEST, "JWT token is invalid");
        }

    }

    private boolean isPublicUrl(String path) {
        for (String pattern : PUBLIC_URLS) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private void sendJwtError(HttpServletResponse response,
                                   int status,
                                   String message) throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");

        response.getWriter().write("""
                    {
                      "status": "fail",
                      "code": %d,
                      "message": "%s"
                    }
                """.formatted(status, message));
    }
}

