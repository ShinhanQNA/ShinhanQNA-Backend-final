package back.sw.domain.auth.service;

import back.sw.domain.auth.dto.request.LoginRequest;
import back.sw.domain.auth.dto.request.LogoutRequest;
import back.sw.domain.auth.dto.request.RefreshTokenRequest;
import back.sw.domain.auth.dto.response.AccessTokenResponse;
import back.sw.domain.auth.dto.response.TokenResponse;
import back.sw.domain.member.entity.Member;
import back.sw.domain.member.repository.MemberRepository;
import back.sw.global.exception.ServiceException;
import back.sw.global.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginSuccess() {
        Member member = Member.join("user1@univ.ac.kr", "20250001", "encoded-password", "nick1");

        when(memberRepository.findByEmail("user1@univ.ac.kr")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("password1234", "encoded-password")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(member)).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(member)).thenReturn("refresh-token");

        TokenResponse response = authService.login(new LoginRequest("user1@univ.ac.kr", "password1234"));

        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        assertEquals("refresh-token", member.getRefreshToken());
    }

    @Test
    void loginFailsWhenPasswordNotMatched() {
        Member member = Member.join("user2@univ.ac.kr", "20250002", "encoded-password", "nick2");

        when(memberRepository.findByEmail("user2@univ.ac.kr")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("wrong", "encoded-password")).thenReturn(false);

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> authService.login(new LoginRequest("user2@univ.ac.kr", "wrong"))
        );

        assertEquals("401-1", exception.getRsData().resultCode());
    }

    @Test
    void refreshSuccess() {
        Member member = Member.join("user3@univ.ac.kr", "20250003", "encoded-password", "nick3");
        member.updateRefreshToken("refresh-token");

        when(jwtTokenProvider.getMemberIdFromRefreshToken("refresh-token")).thenReturn(1);
        when(memberRepository.findById(1)).thenReturn(Optional.of(member));
        when(jwtTokenProvider.generateAccessToken(member)).thenReturn("new-access-token");

        AccessTokenResponse response = authService.refresh(new RefreshTokenRequest("refresh-token"));

        assertEquals("new-access-token", response.accessToken());
    }

    @Test
    void refreshFailsWhenStoredTokenMismatched() {
        Member member = Member.join("user4@univ.ac.kr", "20250004", "encoded-password", "nick4");
        member.updateRefreshToken("another-token");

        when(jwtTokenProvider.getMemberIdFromRefreshToken("refresh-token")).thenReturn(1);
        when(memberRepository.findById(1)).thenReturn(Optional.of(member));

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> authService.refresh(new RefreshTokenRequest("refresh-token"))
        );

        assertEquals("401-1", exception.getRsData().resultCode());
    }

    @Test
    void logoutSuccess() {
        Member member = Member.join("user5@univ.ac.kr", "20250005", "encoded-password", "nick5");
        member.updateRefreshToken("refresh-token");

        when(jwtTokenProvider.getMemberIdFromRefreshToken("refresh-token")).thenReturn(1);
        when(memberRepository.findById(1)).thenReturn(Optional.of(member));

        authService.logout(new LogoutRequest("refresh-token"));

        assertNull(member.getRefreshToken());
    }
}
