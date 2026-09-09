package com.skc04.campusbookmarket.trade.service;

import com.skc04.campusbookmarket.post.domain.TradePost;
import com.skc04.campusbookmarket.post.domain.TradeStatus;
import com.skc04.campusbookmarket.trade.domain.RequestStatus;
import com.skc04.campusbookmarket.trade.repository.TradeRequestRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PostTradePolicy {
    private final TradeRequestRepository requests;
    public PostTradePolicy(TradeRequestRepository requests) { this.requests = requests; }

    public void validateEditable(TradePost post) {
        // 신청자가 확인한 가격/설명을 예약 전에 몰래 바꾸는 것도 막는다.
        if (post.getStatus() != TradeStatus.SALE || requests.existsByPostIdAndStatusIn(
                post.getId(), List.of(RequestStatus.REQUESTED, RequestStatus.RESERVED, RequestStatus.COMPLETED))) {
            throw new TradeConflictException("trade.error.editBlocked");
        }
    }

    public void validateDeletable(TradePost post) {
        validateEditable(post);
        if (requests.existsByPostId(post.getId())) {
            throw new TradeConflictException("trade.error.deleteBlocked");
        }
    }

    public void validateManualStatus(TradePost post, TradeStatus status) {
        // 기존 버튼/URL로 거래 절차를 건너뛰지 못한다. 과거 수동 상태만 SALE로 복구 가능.
        if (status != TradeStatus.SALE || requests.existsByPostId(post.getId())) {
            throw new TradeConflictException("trade.error.manualStatus");
        }
    }
}
