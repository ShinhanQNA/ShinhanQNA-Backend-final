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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private static final String INVALID_CREDENTIAL_MESSAGE = "이메일 또는 비밀번호가 올바르지 않습니다.";
    private static final String INVALID_TOKEN_MESSAGE = "유효하지 않은 토큰입니다.";

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public TokenResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(this::invalidCredentialException);

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw invalidCredentialException();
        }

        String accessToken = jwtTokenProvider.generateAccessToken(member);
        String refreshToken = jwtTokenProvider.generateRefreshToken(member);

        member.updateRefreshToken(refreshToken);

        return new TokenResponse(accessToken, refreshToken);
    }

    @Transactional
    public AccessTokenResponse refresh(RefreshTokenRequest request) {
        Member member = getMemberByRefreshToken(request.refreshToken());

        String accessToken = jwtTokenProvider.generateAccessToken(member);

        return new AccessTokenResponse(accessToken);
    }

    @Transactional
    public void logout(LogoutRequest request) {
        Member member = getMemberByRefreshToken(request.refreshToken());

        member.clearRefreshToken();
    }

    private Member getMemberByRefreshToken(String refreshToken) {
        int memberId = jwtTokenProvider.getMemberIdFromRefreshToken(refreshToken);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException("401-1", INVALID_TOKEN_MESSAGE));

        if (member.getRefreshToken() == null || !member.getRefreshToken().equals(refreshToken)) {
            throw new ServiceException("401-1", INVALID_TOKEN_MESSAGE);
        }

        return member;
    }

    private ServiceException invalidCredentialException() {
        return new ServiceException("401-1", INVALID_CREDENTIAL_MESSAGE);
    }
}
