package com.ootd.ootd.utils.controller;

import com.ootd.ootd.utils.service.GoogleCloudStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class UploadController {
    // 구글 클라우드 버킷 이름.
    @Value("${gcs.bucket-name}")
    private String bucketName;

    @Autowired
    GoogleCloudStorageService googleCloudStorageService;

    public UploadController(GoogleCloudStorageService googleCloudStorageService) {
        this.googleCloudStorageService = googleCloudStorageService;
    }

    @PostMapping("/upload")
    public String uploadToGCS(@RequestParam("file") MultipartFile file) {

        return "";
    }

}
