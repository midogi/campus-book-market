package com.skc04.campusbookmarket.web.post.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * 게시글 등록 폼의 HTTP 입력을 받는 객체다.
 * 엔티티와 분리해 화면 검증 규칙이 도메인 모델에 섞이지 않게 한다.
 */
public class PostCreateForm {

    @NotBlank(message = "{validation.post.title.required}")
    @Size(max = 100, message = "{validation.post.title.size}")
    private String title;

    @NotNull(message = "{validation.post.price.required}")
    @PositiveOrZero(message = "{validation.post.price.positiveOrZero}")
    private Long price;


    @NotBlank(message = "{validation.post.sellerName.required}")
    @Size(max = 30, message = "{validation.post.sellerName.size}")
    private String sellerName;

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

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
