package com.ootd.ootd.utils.service;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GoogleCloudStorageService {
    private final Storage storage;

    public GoogleCloudStorageService() {
        try {
            // 기본 인증 방식 (GOOGLE_APPLICATION_CREDENTIALS 환경변수 사용)
            this.storage = StorageOptions.getDefaultInstance().getService();
        } catch (Exception e) {
            log.error("Failed to initialize Google Cloud Storage", e);
            throw new RuntimeException("GCS 초기화 실패", e);
        }
    }
}
