package com.bnagritech.tradesphere.imagekit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class ImageKitConfig {

    /** Server-side calls to ImageKit use Basic auth: private key as username, empty password. */
    @Bean
    public RestClient imageKitRestClient(@Value("${app.imagekit.private-key}") String privateKey) {
        String basic = Base64.getEncoder()
                .encodeToString((privateKey + ":").getBytes(StandardCharsets.UTF_8));

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(60_000);   // uploads of large photos on slow links

        return RestClient.builder()
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .build();
    }
}
