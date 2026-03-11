package back.sw.domain.post.service;

import back.sw.global.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostImageStorageServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void storeSavesFilesAndReturnsPublicUrls() throws Exception {
        PostImageStorageService storageService = new PostImageStorageService(tempDir.toString());
        List<MockMultipartFile> images = List.of(
                createImage("a.png", "image-a"),
                createImage("b.png", "image-b")
        );

        List<String> urls = storageService.store(images);

        assertEquals(2, urls.size());
        assertTrue(urls.get(0).startsWith("/uploads/"));
        assertTrue(urls.get(1).startsWith("/uploads/"));

        assertTrue(Files.exists(tempDir.resolve(extractFileName(urls.get(0)))));
        assertTrue(Files.exists(tempDir.resolve(extractFileName(urls.get(1)))));
    }

    @Test
    void storeFailsWhenImageIsEmpty() {
        PostImageStorageService storageService = new PostImageStorageService(tempDir.toString());
        List<MockMultipartFile> images = List.of(new MockMultipartFile(
                "images",
                "empty.png",
                "image/png",
                new byte[0]
        ));

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> storageService.store(images)
        );

        assertEquals("400-1", exception.getRsData().resultCode());
    }

    private MockMultipartFile createImage(String fileName, String content) {
        return new MockMultipartFile(
                "images",
                fileName,
                "image/png",
                content.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String extractFileName(String url) {
        return url.substring("/uploads/".length());
    }
}
