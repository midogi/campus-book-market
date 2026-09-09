package com.skc04.campusbookmarket.web.error;

import com.skc04.campusbookmarket.file.StoredImageNotFoundException;
import com.skc04.campusbookmarket.post.service.PostAccessDeniedException;
import com.skc04.campusbookmarket.post.service.PostNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/** MVC 컨트롤러에서 발생한 예상 가능한 예외를 상태 코드별 오류 화면으로 변환한다. */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(com.skc04.campusbookmarket.trade.service.TradeConflictException.class)
    public String handleTradeConflict(com.skc04.campusbookmarket.trade.service.TradeConflictException exception,
            org.springframework.ui.Model model) {
        model.addAttribute("tradeErrorCode", exception.getCode());
        return "error/409";
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(PostNotFoundException.class)
    public String handlePostNotFound() {
        return "error/404";
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(PostAccessDeniedException.class)
    public String handlePostAccessDenied() {
        return "error/403";
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(StoredImageNotFoundException.class)
    public String handleStoredImageNotFound() {
        return "error/404";
    }

    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceeded() {
        return "error/413";
    }
}
