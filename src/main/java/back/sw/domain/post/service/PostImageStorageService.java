package back.sw.domain.post.service;

import back.sw.global.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PostImageStorageService {
    private static final String UPLOAD_URL_PREFIX = "/uploads/";
    private static final Logger log = LoggerFactory.getLogger(PostImageStorageService.class);

    private final Path uploadPath;

    public PostImageStorageService(@Value("${file.upload-dir:./uploads}") String uploadDir) {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public List<String> store(List<? extends MultipartFile> images) {
        createUploadDirectoryIfAbsent();

        List<String> urls = new ArrayList<>();
        for (MultipartFile image : images) {
            if (image.isEmpty()) {
                throw new ServiceException("400-1", "빈 이미지 파일은 업로드할 수 없습니다.");
            }

            String savedFileName = createSavedFileName(image.getOriginalFilename());
            Path targetPath = uploadPath.resolve(savedFileName);
            transfer(image, targetPath);
            urls.add(UPLOAD_URL_PREFIX + savedFileName);
        }

        return urls;
    }

    public void deleteAll(List<String> imageUrls) {
        for (String imageUrl : imageUrls) {
            deleteIfExists(imageUrl);
        }
    }

    private void createUploadDirectoryIfAbsent() {
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new ServiceException("500-1", "업로드 디렉터리를 생성할 수 없습니다.");
        }
    }

    private String createSavedFileName(String originalFileName) {
        String extension = extractExtension(originalFileName);
        return UUID.randomUUID() + extension;
    }

    private String extractExtension(String originalFileName) {
        if (originalFileName == null) {
            return "";
        }

        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFileName.length() - 1) {
            return "";
        }

        return originalFileName.substring(dotIndex);
    }

    private void transfer(MultipartFile image, Path targetPath) {
        try {
            image.transferTo(targetPath);
        } catch (IOException e) {
            throw new ServiceException("500-1", "이미지 저장 중 오류가 발생했습니다.");
        }
    }

    private void deleteIfExists(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith(UPLOAD_URL_PREFIX)) {
            return;
        }

        String fileName = imageUrl.substring(UPLOAD_URL_PREFIX.length());
        if (fileName.isBlank()) {
            return;
        }

        Path targetPath = uploadPath.resolve(fileName).normalize();
        if (!targetPath.startsWith(uploadPath)) {
            return;
        }

        try {
            Files.deleteIfExists(targetPath);
        } catch (IOException e) {
            log.warn("이미지 삭제에 실패했습니다. imageUrl={}, path={}", imageUrl, targetPath, e);
        }
    }
}
