package com.vts.hrms.cfg;


import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;

@Service
public class JwtUtil {

    private String secret;
    private int jwtExpirationInMs;
    private int refreshExpirationDateInMs;

    @Value("${jwt.secret}")
    public void setSecret(String secret) {
        this.secret = secret;
    }

    @Value("${jwt.expirationDateInMs}")
    public void setJwtExpirationInMs(int jwtExpirationInMs) {
        this.jwtExpirationInMs = jwtExpirationInMs;
    }

    @Value("${jwt.refreshExpirationDateInMs}")
    public void setRefreshExpirationDateInMs(int refreshExpirationDateInMs) {
        this.refreshExpirationDateInMs = refreshExpirationDateInMs;
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        Collection<? extends GrantedAuthority> roles = userDetails.getAuthorities();

        if (roles.contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            claims.put("isAdmin", true);
        }if (roles.contains(new SimpleGrantedAuthority("ROLE_USER"))) {
            claims.put("isUser", true);
        }if (roles.contains(new SimpleGrantedAuthority("ROLE_DH"))) {
            claims.put("isDh", true);
        }if (roles.contains(new SimpleGrantedAuthority("ROLE_GH"))) {
            claims.put("isGh", true);
        }if (roles.contains(new SimpleGrantedAuthority("ROLE_GHDH"))) {
            claims.put("isGhDh", true);
        }if (roles.contains(new SimpleGrantedAuthority("ROLE_AD_HRT"))) {
            claims.put("isAdHrT", true);
        }if (roles.contains(new SimpleGrantedAuthority("ROLE_SM_HRT"))) {
            claims.put("isSmHrt", true);
        }if (roles.contains(new SimpleGrantedAuthority("ROLE_PROJECT_DIRECTOR"))) {
            claims.put("isProjectDirector", true);
        }if (roles.contains(new SimpleGrantedAuthority("ROLE_SA_HRT"))) {
            claims.put("isSaHrt", true);
        }if (roles.contains(new SimpleGrantedAuthority("ROLE_CAG_DIV"))) {
            claims.put("isCagDiv", true);
        }if (roles.contains(new SimpleGrantedAuthority("ROLE_TCG_DIV"))) {
            claims.put("isTcgDiv", true);
        }if (roles.contains(new SimpleGrantedAuthority("ROLE_ADMIN_DIV"))) {
            claims.put("isAdmDiv", true);
        }if (roles.contains(new SimpleGrantedAuthority("ROLE_DIRECTOR"))) {
            claims.put("isDirector", true);
        }


        return doGenerateToken(claims, userDetails.getUsername());
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String doGenerateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationInMs))
                .signWith(SignatureAlgorithm.HS256, getSignInKey()).compact();

    }

    public String doGenerateRefreshToken(Map<String, Object> claims, String subject) {

        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationDateInMs))
                .signWith(SignatureAlgorithm.HS256, getSignInKey()).compact();

    }

    public boolean validateToken(String authToken) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(getSignInKey()).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("INVALID_CREDENTIALS", ex);
        } catch (ExpiredJwtException ex) {
            throw ex;
        }
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(getSignInKey()).parseClaimsJws(token).getBody();
        return claims.getSubject();

    }

    public List<SimpleGrantedAuthority> getRolesFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        List<SimpleGrantedAuthority> roles = new ArrayList<>();

        if (Boolean.TRUE.equals(claims.get("isAdmin", Boolean.class))) roles.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (Boolean.TRUE.equals(claims.get("isUser", Boolean.class))) roles.add(new SimpleGrantedAuthority("ROLE_USER"));
        if (Boolean.TRUE.equals(claims.get("isDh", Boolean.class))) roles.add(new SimpleGrantedAuthority("ROLE_DH"));
        if (Boolean.TRUE.equals(claims.get("isGh", Boolean.class))) roles.add(new SimpleGrantedAuthority("ROLE_GH"));
        if (Boolean.TRUE.equals(claims.get("isGhDh", Boolean.class))) roles.add(new SimpleGrantedAuthority("ROLE_GHDH"));
        if (Boolean.TRUE.equals(claims.get("isAdHrT", Boolean.class))) roles.add(new SimpleGrantedAuthority("ROLE_AD_HRT"));
        if (Boolean.TRUE.equals(claims.get("isSmHrt", Boolean.class))) roles.add(new SimpleGrantedAuthority("ROLE_SM_HRT"));
        if (Boolean.TRUE.equals(claims.get("isProjectDirector", Boolean.class))) roles.add(new SimpleGrantedAuthority("ROLE_PROJECT_DIRECTOR"));
        if (Boolean.TRUE.equals(claims.get("isSaHrt", Boolean.class))) roles.add(new SimpleGrantedAuthority("ROLE_SA_HRT"));
        if (Boolean.TRUE.equals(claims.get("isCagDiv", Boolean.class))) roles.add(new SimpleGrantedAuthority("ROLE_CAG_DIV"));
        if (Boolean.TRUE.equals(claims.get("isTcgDiv", Boolean.class))) roles.add(new SimpleGrantedAuthority("ROLE_TCG_DIV"));
        if (Boolean.TRUE.equals(claims.get("isAdmDiv", Boolean.class))) roles.add(new SimpleGrantedAuthority("ROLE_ADMIN_DIV"));
        if (Boolean.TRUE.equals(claims.get("isDirector", Boolean.class))) roles.add(new SimpleGrantedAuthority("ROLE_DIRECTOR"));

        return roles;
    }


}

