package com.bnagritech.tradesphere.security.jwt;
import com.fasterxml.jackson.databind.ser.Serializers;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Base64;
import java.util.function.Function;

import java.util.Date;


@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken( String userName) {

   return Jwts.builder()
           .subject(userName)
           .issuedAt(new Date())
           .expiration(new Date(System.currentTimeMillis() + expiration))
           .signWith(getSignInKey())
           .compact();
    }
    public String generateRefreshToken( String userName) {
        return Jwts.builder()
                .subject(userName)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()+expiration))
                .signWith(getSignInKey())
                .compact();
    }
    public String extractUserName(String token) {

        return extractClaim(
                token,
                Claims::getSubject);
    }

    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver) {

        final Claims claims =
                extractAllClaims(token);

        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(
            String token) {

        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(
            String token,
            UserDetails userDetails) {

        final String username =
                extractUserName(token);

        return username.equals(
                userDetails.getUsername())
                && !isTokenExpired(token);
    }


    private boolean isTokenExpired(
            String token) {

        return extractExpiration(token)
                .before(new Date());
    }

    private Date extractExpiration(
            String token) {

        return extractClaim(
                token,
                Claims::getExpiration);
    }

    private SecretKey getSignInKey(){
        byte[] keyBytes= Decoders.BASE64.decode(Base64.getEncoder().encodeToString(
                secret.getBytes()));
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
