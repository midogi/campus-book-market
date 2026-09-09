package com.skc04.campusbookmarket.web.post;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.skc04.campusbookmarket.file.FileStore;
import com.skc04.campusbookmarket.file.StoredImageNotFoundException;
import com.skc04.campusbookmarket.web.error.GlobalExceptionHandler;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PostImageController.class)
@Import(GlobalExceptionHandler.class)
class PostImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileStore fileStore;

    @Test
    void servesStoredImageWithContentTypeAndCacheHeader() throws Exception {
        given(fileStore.load("generated.png"))
                .willReturn(new ByteArrayResource(new byte[]{1, 2, 3}));

        mockMvc.perform(get("/post-images/generated.png"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(new byte[]{1, 2, 3}))
                .andExpect(header().string("Cache-Control", containsString("max-age")));
    }

    @Test
    void missingStoredImageReturnsNotFound() throws Exception {
        given(fileStore.load("missing.png"))
                .willThrow(new StoredImageNotFoundException("missing.png"));

        mockMvc.perform(get("/post-images/missing.png").locale(Locale.KOREAN))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("게시글을 찾을 수 없습니다")));
    }
}
