package vacance_log.sogang.global.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import vacance_log.sogang.global.exception.image.ImageUploadException;
import vacance_log.sogang.global.util.S3KeyUtils;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;


    public String uploadPhoto(MultipartFile file, Long roomId, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new ImageUploadException("업로드할 파일이 비어있습니다.");
        }

        String fileName = String.format("%d_%s%s", userId, UUID.randomUUID(), getExt(file.getOriginalFilename()));
        String key = "photos/" + roomId + "/" + fileName;

        return putObjectToS3(file, key);
    }

    private String putObjectToS3(MultipartFile file, String key) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("✅ S3 Upload Success: {}", key);
            return getPublicUrl(key);

        } catch (IOException e) {
            log.error("❌ S3 File Read Failed: {}", file.getOriginalFilename());
            throw new ImageUploadException("파일 읽기 실패");
        }
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;

        try {
            String key = S3KeyUtils.toKey(bucket, fileUrl);

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("🗑️ S3 Delete Success: {}", key);
        } catch (Exception e) {
            log.error("❌ S3 Delete Failed: {}", fileUrl, e);
        }
    }

    private String getPublicUrl(String key) {
        return String.format("https://%s.s3.ap-northeast-2.amazonaws.com/%s", bucket, key);
    }

    private String getExt(String original) {
        if (original == null) return "";
        int idx = original.lastIndexOf('.');
        return (idx >= 0) ? original.substring(idx).toLowerCase() : "";
    }
}
