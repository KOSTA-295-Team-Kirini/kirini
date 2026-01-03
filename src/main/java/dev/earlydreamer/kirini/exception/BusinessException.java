package dev.earlydreamer.kirini.exception;

import lombok.Getter;

/**
 * 비즈니스 예외 기본 클래스
 */
@Getter
public class BusinessException extends RuntimeException {
    private final String errorCode;

    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}

