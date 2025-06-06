package com.ootd.ootd.utils.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class GoogleCloudStorageService {
    private Storage storage;

    @Value("${gcs.bucket.name}")
    private String bucketName;

    @Value("${google.cloud.storage.credentials.location}")
    private String credentialsLocation;

    @Value("${gcp.project.id}")
    private String projectId;

    public GoogleCloudStorageService() {
        // 생성자에서는 초기화하지 않음 - PostConstruct에서 처리
    }

    @PostConstruct
    public void init() {
        try {
            log.info("GCS 초기화 시작 - Credentials: {}, Bucket: {}", credentialsLocation, bucketName);

            // classpath: 접두사 제거
            String resourcePath = credentialsLocation.replace("classpath:", "");

            // 인증 정보 로드
            InputStream credentialsStream = new ClassPathResource(resourcePath).getInputStream();
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(credentialsStream)
                    .createScoped("https://www.googleapis.com/auth/cloud-platform");

            // Storage 서비스 초기화 (명시적 인증 정보 사용)
            this.storage = StorageOptions.newBuilder()
                    .setProjectId(projectId)
                    .setCredentials(credentials)
                    .build()
                    .getService();

            log.info("✅ GCS 초기화 성공 - Project: {}, Bucket: {}", projectId, bucketName);

            // 버킷 접근 테스트
            try {
                Bucket bucket = storage.get(bucketName);
                if (bucket != null) {
                    log.info("✅ 버킷 접근 확인: {}", bucket.getName());
                } else {
                    log.warn("⚠️ 버킷을 찾을 수 없음: {}", bucketName);
                }
            } catch (Exception e) {
                log.warn("⚠️ 버킷 접근 테스트 실패: {}", e.getMessage());
            }

        } catch (IOException e) {
            log.error("❌ GCS 초기화 실패 - Credentials 파일을 찾을 수 없음: {}", credentialsLocation, e);
            throw new RuntimeException("GCS 인증 정보 로드 실패", e);
        } catch (Exception e) {
            log.error("❌ GCS 초기화 실패", e);
            throw new RuntimeException("GCS 초기화 실패", e);
        }
    }

    /**
     * 단일 또는 여러 이미지를 Google Cloud Storage에 업로드
     * @param files MultipartFile 배열 (단일 파일도 배열로 전달)
     * @return 업로드된 이미지들의 URL 리스트
     */
    public List<String> uploadImages(MultipartFile[] files) throws IOException {
        List<String> uploadedUrls = new ArrayList<>();

        if (storage == null) {
            throw new IllegalStateException("Storage 서비스가 초기화되지 않았습니다.");
        }

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                try {
                    // 고유한 파일명 생성
                    String fileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
                    log.info("파일 업로드 시작: {}", fileName);

                    // Blob ID 생성
                    BlobId blobId = BlobId.of(bucketName, fileName);

                    // Blob 정보 설정
                    BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                            .setContentType(file.getContentType())
                            .build();


                // 파일 업로드
                Blob blob = storage.create(blobInfo, file.getBytes());

                    // 공개 읽기 권한 설정
                    try {
                        blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
                        log.info("✅ 파일 업로드 및 공개 권한 설정 완료: {}", fileName);
                    } catch (Exception e) {
                        log.warn("⚠️ 공개 권한 설정 실패 (파일은 업로드됨): {}", e.getMessage());
                    }

                    // 공개 URL 생성 및 리스트에 추가
                    String imageUrl = String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName);
                    uploadedUrls.add(imageUrl);

                    log.info("✅ 파일 업로드 성공: {}", imageUrl);

                } catch (Exception e) {
                    log.error("❌ 파일 업로드 실패: {}", file.getOriginalFilename(), e);
                    throw new IOException("파일 업로드 실패: " + file.getOriginalFilename(), e);
                }
            }
        }

        return uploadedUrls;
    }
}