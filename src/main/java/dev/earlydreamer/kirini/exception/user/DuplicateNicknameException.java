package dev.earlydreamer.kirini.exception.user;

import dev.earlydreamer.kirini.exception.BusinessException;

/**
 * 닉네임 중복 예외
 */
public class DuplicateNicknameException extends BusinessException {
    public DuplicateNicknameException() {
        super("이미 사용 중인 닉네임입니다.", "DUPLICATE_NICKNAME");
    }
}

