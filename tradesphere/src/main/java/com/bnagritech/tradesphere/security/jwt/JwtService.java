package com.bnagritech.tradesphere.security.jwt;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private  String generateToken(String userName){
        return OAuth2ResourceServerProperties.Jwt.builder()
                .subject(userName)
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
                ).compact();

    }
}
