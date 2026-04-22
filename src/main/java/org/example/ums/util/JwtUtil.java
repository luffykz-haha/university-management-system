package org.example.ums.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    // Must be base64-encoded and 256 bits (32 chars for HS256)
    private static final String SECRET_KEY = "pThKpYb6KfQf3i2c3jXMJ4IBZ8Yw9skMWUny25vMJ2c=";

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("authorities", userDetails.getAuthorities())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUserEmail(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public List<String> extractUserRoles(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // The authorities claim was stored as a collection of SimpleGrantedAuthority objects
        Object authorities = claims.get("authorities");

        if (authorities instanceof List<?>) {
            return ((List<?>) authorities).stream()
                    .map(Object::toString) // You may refine this if format is like {authority=ROLE_ADMIN}
                    .map(authStr -> {
                        if (authStr.contains("=")) {
                            return authStr.split("=")[1].replace("}", "");
                        } else {
                            return authStr; // fallback
                        }
                    })
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    public String extractSingleUserRole(String token) {
        List<String> roles = extractUserRoles(token);
        return roles.isEmpty() ? null : roles.getFirst();
    }


    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUserEmail(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .before(new Date());
    }
}
