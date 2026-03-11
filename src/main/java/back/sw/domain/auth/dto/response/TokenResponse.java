package back.sw.domain.auth.dto.response;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
