package dev.earlydreamer.kirini.repository;

import dev.earlydreamer.kirini.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User 엔티티에 대한 데이터 접근 Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * 이메일로 사용자 조회
     *
     * @param email 이메일
     * @return 사용자 Optional
     */
    Optional<User> findByEmail(String email);

    /**
     * 이메일 존재 여부 확인 (중복 체크용)
     *
     * @param email 이메일
     * @return 존재하면 true
     */
    boolean existsByEmail(String email);

    /**
     * 닉네임 존재 여부 확인 (중복 체크용)
     *
     * @param name 닉네임
     * @return 존재하면 true
     */
    boolean existsByName(String name);

    /**
     * userId 존재 여부 확인
     *
     * @param userId 사용자 ID
     * @return 존재하면 true
     */
    boolean existsByUserId(String userId);
}

