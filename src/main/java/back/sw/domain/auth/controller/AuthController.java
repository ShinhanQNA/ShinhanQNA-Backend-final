package back.sw.domain.auth.controller;

import back.sw.domain.auth.dto.request.LoginRequest;
import back.sw.domain.auth.dto.request.LogoutRequest;
import back.sw.domain.auth.dto.request.RefreshTokenRequest;
import back.sw.domain.auth.dto.response.AccessTokenResponse;
import back.sw.domain.auth.dto.response.TokenResponse;
import back.sw.domain.auth.service.AuthService;
import back.sw.global.response.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public RsData<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse data = authService.login(request);

        return new RsData<>("200-1", "로그인에 성공했습니다.", data);
    }

    @PostMapping("/refresh")
    public RsData<AccessTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AccessTokenResponse data = authService.refresh(request);

        return new RsData<>("200-1", "Access Token을 재발급했습니다.", data);
    }

    @PostMapping("/logout")
    public RsData<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);

        return new RsData<>("200-1", "로그아웃되었습니다.", null);
    }
}
