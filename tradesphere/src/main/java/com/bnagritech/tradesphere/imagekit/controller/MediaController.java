package com.bnagritech.tradesphere.imagekit.controller;

import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.imagekit.dto.ImageKitAuthResponse;
import com.bnagritech.tradesphere.imagekit.dto.MediaFolder;
import com.bnagritech.tradesphere.imagekit.dto.MediaRef;
import com.bnagritech.tradesphere.imagekit.service.ImageKitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Both endpoints must sit behind your normal JWT security (not permitAll):
 * anyone who can call /upload-auth can upload files to your ImageKit account.

 * If your other controllers get the current user differently, change the @AuthenticationPrincipal lines.
 */
@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final ImageKitService imageKitService;

    /** Step 1 of a mobile upload: GET /api/media/upload-auth?folder=VISIT_IMAGES
     *  (or OUTLET_IMAGES, PROFILE_PICTURE) */
    @GetMapping("/upload-auth")
    public ImageKitAuthResponse uploadAuth(@RequestParam MediaFolder folder) {
        return imageKitService.createUploadAuth(folder);
    }

    /** Server-side upload: POST /api/media/upload (multipart: file, folder, tags) */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MediaRef upload(@RequestPart("file") MultipartFile file,
                           @RequestParam MediaFolder folder,
                           @RequestParam(required = false) List<String> tags,
                           @AuthenticationPrincipal User currentUser) {
        return imageKitService.upload(file, folder, tags, currentUser.getId());
    }
}
