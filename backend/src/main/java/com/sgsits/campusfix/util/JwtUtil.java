package com.sgsits.campusfix.util;

import com.sgsits.campusfix.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${app.jwt.secret}")        private String secret;
    @Value("${app.jwt.expiration-ms}") private long   expMs;

    private Key key() { return Keys.hmacShaKeyFor(secret.getBytes()); }

    public String generate(User u) {
        return Jwts.builder().setSubject(u.getEmail())
            .claim("userId", u.getId()).claim("role", u.getRole()).claim("name", u.getName())
            .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + expMs))
            .signWith(key(), SignatureAlgorithm.HS256).compact();
    }

    public String email(String t) {
        return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(t).getBody().getSubject();
    }

    public boolean valid(String t) {
        try { Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(t); return true; }
        catch (Exception e) { return false; }
    }
}
