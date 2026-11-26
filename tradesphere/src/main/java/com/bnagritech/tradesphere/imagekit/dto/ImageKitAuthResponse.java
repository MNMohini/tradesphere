package com.bnagritech.tradesphere.imagekit.dto;

import lombok.Builder;
import lombok.Value;

/** What the app needs to upload one file directly to ImageKit. */
@Value
@Builder
public class ImageKitAuthResponse {
    String token;       // one-time; ImageKit rejects a token that was already used
    long expire;        // Unix seconds
    String signature;   // HMAC-SHA1(privateKey, token + expire), lowercase hex
    String publicKey;
    String urlEndpoint;
    String folder;      // full folder path the app must send with the upload
}
