package back.sw.global.security;

import back.sw.domain.member.entity.Member;
import back.sw.domain.member.entity.MemberRole;
import back.sw.global.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenProviderTest {
    private static final String SECRET = "abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789";

    @Test
    void generateAndParseMemberIdFromAccessToken() {
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(SECRET, 1800, 1209600);
        Member member = Member.join("user1@univ.ac.kr", "20250001", "encoded", "nick1");
        ReflectionTestUtils.setField(member, "id", 7);

        String token = jwtTokenProvider.generateAccessToken(member);

        int memberId = jwtTokenProvider.getMemberIdFromAccessToken(token);
        MemberRole role = jwtTokenProvider.getMemberRoleFromAccessToken(token);

        assertEquals(7, memberId);
        assertEquals(MemberRole.STUDENT, role);
    }

    @Test
    void throwsServiceExceptionWhenTokenMalformed() {
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(SECRET, 1800, 1209600);

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> jwtTokenProvider.getMemberIdFromAccessToken("not-a-jwt")
        );

        assertEquals("401-1", exception.getRsData().resultCode());
    }

    @Test
    void throwsServiceExceptionWhenTokenExpired() {
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(SECRET, -1, 1209600);
        Member member = Member.join("user2@univ.ac.kr", "20250002", "encoded", "nick2");
        ReflectionTestUtils.setField(member, "id", 9);

        String token = jwtTokenProvider.generateAccessToken(member);

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> jwtTokenProvider.getMemberIdFromAccessToken(token)
        );

        assertEquals("401-1", exception.getRsData().resultCode());
    }

    @Test
    void rejectsRefreshTokenWhenUsedAsAccessToken() {
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(SECRET, 1800, 1209600);
        Member member = Member.join("user3@univ.ac.kr", "20250003", "encoded", "nick3");
        ReflectionTestUtils.setField(member, "id", 11);

        String refreshToken = jwtTokenProvider.generateRefreshToken(member);

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> jwtTokenProvider.getMemberIdFromAccessToken(refreshToken)
        );

        assertEquals("401-1", exception.getRsData().resultCode());
    }
}
