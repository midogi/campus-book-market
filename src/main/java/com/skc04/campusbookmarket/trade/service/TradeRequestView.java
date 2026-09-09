package com.skc04.campusbookmarket.trade.service;

import com.skc04.campusbookmarket.trade.domain.RequestStatus;
import java.time.LocalDateTime;

/** OSIV가 꺼져 있으므로 뷰에 필요한 값과 버튼 권한을 트랜잭션 안에서 계산한다. */
public record TradeRequestView(Long id, Long postId, String title, long price,
        String buyerName, String sellerName, RequestStatus status, LocalDateTime createdAt,
        boolean canReserve, boolean canReject, boolean canCancel, boolean canComplete) { }
