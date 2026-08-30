package com.skc04.campusbookmarket.web.post.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** 게시글 수정 폼의 입력값과 Bean Validation 규칙을 담는다. */
public class PostUpdateForm {

    @NotBlank(message = "{validation.post.title.required}")
    @Size(max = 100, message = "{validation.post.title.size}")
    private String title;

    @NotNull(message = "{validation.post.price.required}")
    @PositiveOrZero(message = "{validation.post.price.positiveOrZero}")
    private Long price;

    @NotBlank(message = "{validation.post.description.required}")
    @Size(max = 1000, message = "{validation.post.description.size}")
    private String description;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
