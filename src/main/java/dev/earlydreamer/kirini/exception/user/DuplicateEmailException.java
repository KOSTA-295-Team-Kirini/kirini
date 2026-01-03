package dev.earlydreamer.kirini.exception.user;

import dev.earlydreamer.kirini.exception.BusinessException;

/**
 * 이메일 중복 예외
 */
public class DuplicateEmailException extends BusinessException {
    public DuplicateEmailException() {
        super("이미 사용 중인 이메일입니다.", "DUPLICATE_EMAIL");
    }
}

