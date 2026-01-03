package dev.earlydreamer.kirini.controller;

import dev.earlydreamer.kirini.dto.request.SignUpRequest;
import dev.earlydreamer.kirini.dto.response.ApiResponse;
import dev.earlydreamer.kirini.dto.response.DuplicateCheckResponse;
import dev.earlydreamer.kirini.dto.response.SignUpResponse;
import dev.earlydreamer.kirini.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 관련 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 회원가입
     *
     * @param request 회원가입 요청 DTO
     * @return 회원가입 응답
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<SignUpResponse>> register(
            @Valid @RequestBody SignUpRequest request) {

        SignUpResponse response = userService.registerUser(request);

        return ResponseEntity.ok(
                ApiResponse.success("회원가입이 완료되었습니다.", response)
        );
    }

    /**
     * 이메일 중복 체크
     *
     * @param email 이메일
     * @return 중복 체크 응답
     */
    @GetMapping("/check-email")
    public ResponseEntity<DuplicateCheckResponse> checkEmail(
            @RequestParam String email) {

        boolean isDuplicate = userService.checkDuplicateEmail(email);

        if (isDuplicate) {
            return ResponseEntity.ok(
                    DuplicateCheckResponse.duplicate("이미 사용 중인 이메일입니다.")
            );
        }
        return ResponseEntity.ok(
                DuplicateCheckResponse.available("사용 가능한 이메일입니다.")
        );
    }

    /**
     * 닉네임 중복 체크
     *
     * @param nickname 닉네임
     * @return 중복 체크 응답
     */
    @GetMapping("/check-nickname")
    public ResponseEntity<DuplicateCheckResponse> checkNickname(
            @RequestParam String nickname) {

        boolean isDuplicate = userService.checkDuplicateNickname(nickname);

        if (isDuplicate) {
            return ResponseEntity.ok(
                    DuplicateCheckResponse.duplicate("이미 사용 중인 닉네임입니다.")
            );
        }
        return ResponseEntity.ok(
                DuplicateCheckResponse.available("사용 가능한 닉네임입니다.")
        );
    }
}

