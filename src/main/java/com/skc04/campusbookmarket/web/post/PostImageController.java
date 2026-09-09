package com.skc04.campusbookmarket.web.post;

import com.skc04.campusbookmarket.file.FileStore;
import java.time.Duration;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** 로컬 디스크에 저장된 게시글 이미지를 브라우저에 공개한다. */
@RestController
public class PostImageController {

    private final FileStore fileStore;

    public PostImageController(FileStore fileStore) {
        this.fileStore = fileStore;
    }

    @GetMapping("/post-images/{storedName:.+}")
    public ResponseEntity<Resource> image(@PathVariable String storedName) {
        Resource resource = fileStore.load(storedName);
        return ResponseEntity.ok()
                .contentType(mediaType(storedName))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .body(resource);
    }

    private MediaType mediaType(String storedName) {
        String extension = StringUtils.getFilenameExtension(storedName);
        if (extension == null) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        return switch (extension.toLowerCase()) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            case "webp" -> MediaType.parseMediaType("image/webp");
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }
}
