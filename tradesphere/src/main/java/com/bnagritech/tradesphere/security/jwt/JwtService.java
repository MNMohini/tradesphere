package com.bnagritech.tradesphere.security.jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import java.util.function.Function;

import java.util.Date;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(String username) {

        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis() + expiration
                        )
                )
                .signWith(
                        io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                                secret.getBytes()
                        )
                )
                .compact();
    }
    private Claims ectractAllClaims(String token){
        return Jwts.parser()
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public <T> T extractClaim(
            String token, Function<Claims, T>claimsResolver){
        final Claims claims = ectractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractUserName(String token){
        return extractClaim(
                token,
                Claims:: getSubject);
    }

    public boolean isTokenValid(
            String token,String userName)
    {
        return userName.equals(extractUserName(token));
    }
}
