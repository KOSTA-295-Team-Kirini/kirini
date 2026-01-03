package dev.earlydreamer.kirini.service;

import dev.earlydreamer.kirini.domain.User;
import dev.earlydreamer.kirini.dto.request.SignUpRequest;
import dev.earlydreamer.kirini.dto.response.SignUpResponse;
import dev.earlydreamer.kirini.exception.user.DuplicateEmailException;
import dev.earlydreamer.kirini.exception.user.DuplicateNicknameException;
import dev.earlydreamer.kirini.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     *
     * @param request 회원가입 요청 DTO
     * @return 회원가입 응답 DTO
     * @throws DuplicateEmailException    이메일 중복 시
     * @throws DuplicateNicknameException 닉네임 중복 시
     */
    @Transactional
    public SignUpResponse registerUser(SignUpRequest request) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException();
        }

        // 닉네임 중복 체크
        if (userRepository.existsByName(request.getNickname())) {
            throw new DuplicateNicknameException();
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // User 엔티티 생성
        User user = User.createForSignUp(
                request.getEmail(),
                encodedPassword,
                request.getNickname()
        );

        // 저장
        User savedUser = userRepository.save(user);

        // 응답 생성
        return SignUpResponse.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .nickname(savedUser.getName())
                .build();
    }

    /**
     * 이메일 중복 체크
     *
     * @param email 이메일
     * @return 중복이면 true, 아니면 false
     */
    public boolean checkDuplicateEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 닉네임 중복 체크
     *
     * @param nickname 닉네임
     * @return 중복이면 true, 아니면 false
     */
    public boolean checkDuplicateNickname(String nickname) {
        return userRepository.existsByName(nickname);
    }
}

