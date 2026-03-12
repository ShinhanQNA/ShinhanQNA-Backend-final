package back.sw.global.security;

import back.sw.domain.member.entity.Member;
import back.sw.domain.member.entity.MemberRole;
import back.sw.global.security.TokenAuthenticationException.TokenErrorType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public final class JwtTokenProvider {
    private static final String ROLE_CLAIM = "role";
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";

    private final SecretKey secretKey;
    private final long accessTokenExpirationMillis;
    private final long refreshTokenExpirationMillis;

    public JwtTokenProvider(
            @Value("${custom.jwt.secret-key}") String secretKey,
            @Value("${custom.jwt.access-token-expiration-seconds}") long accessTokenExpirationSeconds,
            @Value("${custom.jwt.refresh-token-expiration-seconds}") long refreshTokenExpirationSeconds
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMillis = accessTokenExpirationSeconds * 1000;
        this.refreshTokenExpirationMillis = refreshTokenExpirationSeconds * 1000;
    }

    public String generateAccessToken(Member member) {
        return generateToken(member, accessTokenExpirationMillis, ACCESS_TOKEN_TYPE);
    }

    public String generateRefreshToken(Member member) {
        return generateToken(member, refreshTokenExpirationMillis, REFRESH_TOKEN_TYPE);
    }

    public AccessTokenPayload getAccessTokenPayload(String token) {
        Claims claims = parseClaims(token);
        validateTokenType(claims, ACCESS_TOKEN_TYPE);

        return new AccessTokenPayload(
                parseMemberId(claims),
                parseMemberRole(claims)
        );
    }

    public int getMemberIdFromAccessToken(String token) {
        return getAccessTokenPayload(token).memberId();
    }

    public MemberRole getMemberRoleFromAccessToken(String token) {
        return getAccessTokenPayload(token).role();
    }

    public int getMemberIdFromRefreshToken(String token) {
        Claims claims = parseClaims(token);
        validateTokenType(claims, REFRESH_TOKEN_TYPE);

        return parseMemberId(claims);
    }

    private int parseMemberId(Claims claims) {
        try {
            return Integer.parseInt(claims.getSubject());
        } catch (NumberFormatException e) {
            throw new TokenAuthenticationException(TokenErrorType.INVALID, "유효하지 않은 토큰입니다.");
        }
    }

    private String generateToken(Member member, long expirationMillis, String tokenType) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(String.valueOf(member.getId()))
                .claim("email", member.getEmail())
                .claim("role", member.getRole().name())
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    private MemberRole parseMemberRole(Claims claims) {
        Object roleClaim = claims.get(ROLE_CLAIM);

        if (!(roleClaim instanceof String roleValue)) {
            throw new TokenAuthenticationException(TokenErrorType.INVALID, "유효하지 않은 토큰입니다.");
        }

        try {
            return MemberRole.valueOf(roleValue);
        } catch (IllegalArgumentException e) {
            throw new TokenAuthenticationException(TokenErrorType.INVALID, "유효하지 않은 토큰입니다.");
        }
    }

    private void validateTokenType(Claims claims, String expectedTokenType) {
        Object tokenType = claims.get(TOKEN_TYPE_CLAIM);
        if (!(tokenType instanceof String tokenTypeValue) || !expectedTokenType.equals(tokenTypeValue)) {
            throw new TokenAuthenticationException(TokenErrorType.INVALID, "유효하지 않은 토큰입니다.");
        }
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new TokenAuthenticationException(TokenErrorType.EXPIRED, "만료된 토큰입니다.");
        } catch (JwtException | IllegalArgumentException e) {
            throw new TokenAuthenticationException(TokenErrorType.INVALID, "유효하지 않은 토큰입니다.");
        }
    }

    public record AccessTokenPayload(int memberId, MemberRole role) {
    }
}
