package com.skc04.campusbookmarket.web.post.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** 게시글 수정 폼의 입력값과 Bean Validation 규칙을 담는다. */
public class PostUpdateForm {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
    private String title;

    @NotNull(message = "가격은 필수입니다.")
    @PositiveOrZero(message = "가격은 0원 이상이어야 합니다.")
    private Long price;

    @NotBlank(message = "판매자는 필수입니다.")
    @Size(max = 30, message = "판매자는 30자 이하여야 합니다.")
    private String sellerName;

    @NotBlank(message = "상품 설명은 필수입니다.")
    @Size(max = 1000, message = "상품 설명은 1000자 이하여야 합니다.")
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
