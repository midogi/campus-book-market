package com.skc04.campusbookmarket.file;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class FileStoreTest {

    private Path uploadDirectory;

    private FileStore fileStore;

    @BeforeEach
    void setUp() throws IOException {
        uploadDirectory = Path.of(
                "build",
                "test-uploads",
                UUID.randomUUID().toString()
        );
        Files.createDirectories(uploadDirectory);
        fileStore = new FileStore(uploadDirectory.toString());
    }

    @Test
    void storeGeneratesUniqueNameAndKeepsOriginalName() throws IOException {
        byte[] imageBytes = pngBytes();
        MockMultipartFile image = new MockMultipartFile(
                "imageFile",
                "lecture-note.png",
                "image/png",
                imageBytes
        );

        StoredFile storedFile = fileStore.store(image).orElseThrow();

        assertEquals("lecture-note.png", storedFile.originalName());
        assertTrue(storedFile.storedName().endsWith(".png"));
        assertFalse(storedFile.storedName().contains("lecture-note"));
        assertArrayEquals(
                imageBytes,
                fileStore.load(storedFile.storedName()).getContentAsByteArray()
        );
    }

    @Test
    void emptyFileDoesNotCreateStoredFile() {
        MockMultipartFile emptyImage = new MockMultipartFile(
                "imageFile", "", "application/octet-stream", new byte[0]
        );

        assertTrue(fileStore.store(emptyImage).isEmpty());
    }

    @Test
    void rejectsUnsupportedOrFakeImage() {
        MockMultipartFile textFile = new MockMultipartFile(
                "imageFile", "memo.txt", "text/plain", "hello".getBytes()
        );
        MockMultipartFile fakePng = new MockMultipartFile(
                "imageFile", "fake.png", "image/png", "not-png".getBytes()
        );

        assertThrows(InvalidImageException.class, () -> fileStore.store(textFile));
        assertThrows(InvalidImageException.class, () -> fileStore.store(fakePng));
    }

    @Test
    void deleteRemovesStoredImage() throws IOException {
        MockMultipartFile image = new MockMultipartFile(
                "imageFile", "book.png", "image/png", pngBytes()
        );
        StoredFile storedFile = fileStore.store(image).orElseThrow();

        fileStore.delete(storedFile.storedName());

        assertFalse(Files.exists(uploadDirectory.resolve(storedFile.storedName())));
        assertThrows(
                StoredImageNotFoundException.class,
                () -> fileStore.load(storedFile.storedName())
        );
    }

    @Test
    void loadRejectsPathTraversal() {
        assertThrows(
                StoredImageNotFoundException.class,
                () -> fileStore.load("../application.properties")
        );
    }

    private byte[] pngBytes() {
        return new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47,
                0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x00
        };
    }
}
