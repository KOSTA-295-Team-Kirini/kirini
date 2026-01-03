package dev.earlydreamer.kirini.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 회원가입 응답 DTO
 */
@Getter
@Builder
public class SignUpResponse {
    private Integer userId;
    private String email;
    private String nickname;
}

