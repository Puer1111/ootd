package com.ootd.ootd.utils.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class GoogleCloudStorageService {
    private Storage storage;

    @Value("${gcs.bucket.name}")
    private String bucketName;


    public GoogleCloudStorageService() {

        // StorageOptions 빌드 시 프로젝트 ID 설정
        StorageOptions.Builder builder = StorageOptions.newBuilder();

        this.storage = builder.build().getService();
    }

    @PostConstruct
    public void init() {
        // Spring Cloud GCP가 자동으로 인증 처리
        this.storage = StorageOptions.getDefaultInstance().getService();
    }

    /**
     * 단일 또는 여러 이미지를 Google Cloud Storage에 업로드
     * @param files MultipartFile 배열 (단일 파일도 배열로 전달)
     * @return 업로드된 이미지들의 URL 리스트
     */
    public List<String> uploadImages(MultipartFile[] files) throws IOException {
        List<String> uploadedUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                // 고유한 파일명 생성
                String fileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();

                // Blob ID 생성
                BlobId blobId = BlobId.of(bucketName, fileName);

                // Blob 정보 설정
                BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                        .setContentType(file.getContentType())
                        .build();

                // 파일 업로드
                storage.create(blobInfo, file.getBytes());

                // 공개 URL 생성 및 리스트에 추가
                String imageUrl = String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName);
                uploadedUrls.add(imageUrl);
            }
        }

        return uploadedUrls;
    }

//    /**
//     * 단일 이미지 업로드를 위한 편의 메서드
//     * @param file 단일 MultipartFile
//     * @return 업로드된 이미지의 URL
//     */
//    public String uploadSingleImage(MultipartFile file) throws IOException {
//        List<String> urls = uploadImages(file);
//        return urls.isEmpty() ? null : urls.get(0);
//    }
}
