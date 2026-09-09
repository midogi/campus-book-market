package com.skc04.campusbookmarket.trade.domain;

/** 게시글의 판매 상태와 별개로 각 구매 신청의 진행 결과를 보존한다. */
public enum RequestStatus {
    REQUESTED, RESERVED, COMPLETED, CANCELLED, REJECTED
}
