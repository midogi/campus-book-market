package com.skc04.campusbookmarket.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileStore {

    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;

    private static final Map<String, String> ALLOWED_IMAGE_TYPES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final Path uploadPath;

    public FileStore(
            @Value("${app.file.upload-dir}") String uploadDirectory
    ) {
        this.uploadPath = Path.of(uploadDirectory)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(uploadPath);
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "업로드 폴더를 생성하지 못했습니다.",
                    exception
            );
        }
    }

    public Optional<StoredFile> store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Optional.empty();
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new InvalidImageException("이미지는 5MB 이하여야 합니다.");
        }

        String extension = ALLOWED_IMAGE_TYPES.get(file.getContentType());
        if (extension == null) {
            throw new InvalidImageException(
                    "JPG, PNG, WEBP 이미지만 업로드할 수 있습니다."
            );
        }
        validateSignature(file, extension);

        String rawName = Objects.requireNonNullElse(
                file.getOriginalFilename(),
                ""
        );
        String originalName = StringUtils.getFilename(
                StringUtils.cleanPath(rawName)
        );

        if (!StringUtils.hasText(originalName)) {
            originalName = "image." + extension;
        }

        String storedName = UUID.randomUUID() + "." + extension;
        Path targetPath = resolveSafely(storedName);

        try {
            file.transferTo(targetPath);
        } catch (IOException exception) {
            deleteQuietly(targetPath);
            throw new UncheckedIOException(
                    "이미지 파일을 저장하지 못했습니다.",
                    exception
            );
        }

        return Optional.of(new StoredFile(originalName, storedName));
    }

    /** 저장명으로 이미지를 읽어 HTTP 응답에 사용할 Resource로 반환한다. */
    public Resource load(String storedName) {
        Path targetPath = resolveSafely(storedName);
        if (!Files.isRegularFile(targetPath) || !Files.isReadable(targetPath)) {
            throw new StoredImageNotFoundException(storedName);
        }
        return new FileSystemResource(targetPath);
    }

    /** 게시글 이미지가 교체되거나 게시글이 삭제되면 기존 실제 파일도 제거한다. */
    public void delete(String storedName) {
        if (!StringUtils.hasText(storedName)) {
            return;
        }

        try {
            Files.deleteIfExists(resolveSafely(storedName));
        } catch (IOException exception) {
            throw new UncheckedIOException("이미지 파일을 삭제하지 못했습니다.", exception);
        }
    }

    private Path resolveSafely(String storedName) {
        if (!StringUtils.hasText(storedName)) {
            throw new StoredImageNotFoundException(storedName);
        }

        Path targetPath = uploadPath.resolve(storedName).normalize();
        if (!Objects.equals(targetPath.getParent(), uploadPath)) {
            throw new StoredImageNotFoundException(storedName);
        }
        return targetPath;
    }

    private void validateSignature(MultipartFile file, String extension) {
        byte[] header = new byte[12];
        int readLength;
        try (InputStream inputStream = file.getInputStream()) {
            readLength = inputStream.read(header);
        } catch (IOException exception) {
            throw new UncheckedIOException("이미지 파일을 검사하지 못했습니다.", exception);
        }

        boolean valid = switch (extension) {
            case "jpg" -> startsWith(header, readLength, new int[]{0xFF, 0xD8, 0xFF});
            case "png" -> startsWith(
                    header,
                    readLength,
                    new int[]{0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}
            );
            case "webp" -> startsWith(header, readLength, new int[]{'R', 'I', 'F', 'F'})
                    && matchesAt(header, readLength, 8, new int[]{'W', 'E', 'B', 'P'});
            default -> false;
        };

        if (!valid) {
            throw new InvalidImageException("파일 내용이 올바른 이미지 형식이 아닙니다.");
        }
    }

    private boolean startsWith(byte[] source, int length, int[] expected) {
        return matchesAt(source, length, 0, expected);
    }

    private boolean matchesAt(byte[] source, int length, int offset, int[] expected) {
        if (length < offset + expected.length) {
            return false;
        }
        for (int index = 0; index < expected.length; index++) {
            if (Byte.toUnsignedInt(source[offset + index]) != expected[index]) {
                return false;
            }
        }
        return true;
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // 원래 저장 실패 예외가 보존되도록 정리 실패는 별도로 던지지 않는다.
        }
    }
}
