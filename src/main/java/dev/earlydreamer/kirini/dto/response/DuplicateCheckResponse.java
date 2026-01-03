package dev.earlydreamer.kirini.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 중복 체크 응답 DTO (Legacy 호환)
 */
@Getter
@Builder
public class DuplicateCheckResponse {
    private boolean isAvailable;
    private boolean isDuplicate;
    private String message;

    /**
     * 사용 가능 응답 생성
     *
     * @param message 메시지
     * @return DuplicateCheckResponse
     */
    public static DuplicateCheckResponse available(String message) {
        return DuplicateCheckResponse.builder()
                .isAvailable(true)
                .isDuplicate(false)
                .message(message)
                .build();
    }

    /**
     * 중복 응답 생성
     *
     * @param message 메시지
     * @return DuplicateCheckResponse
     */
    public static DuplicateCheckResponse duplicate(String message) {
        return DuplicateCheckResponse.builder()
                .isAvailable(false)
                .isDuplicate(true)
                .message(message)
                .build();
    }
}

