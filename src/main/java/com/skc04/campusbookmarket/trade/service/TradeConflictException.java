package com.skc04.campusbookmarket.trade.service;

/** 유효하지 않은 거래 상태/중복 요청은 500이 아닌 409 화면으로 안내한다. */
public class TradeConflictException extends RuntimeException {
    private final String code;
    public TradeConflictException(String code) { super(code); this.code = code; }
    public String getCode() { return code; }
}
