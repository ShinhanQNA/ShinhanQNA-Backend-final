package back.sw.domain.member.repository;

import back.sw.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Integer> {
    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    boolean existsByStudentNumber(String studentNumber);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByRefreshToken(String refreshToken);
}
