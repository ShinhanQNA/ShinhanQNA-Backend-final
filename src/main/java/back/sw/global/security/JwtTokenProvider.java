package back.sw.global.security;

import back.sw.domain.member.entity.Member;
import back.sw.global.exception.ServiceException;
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
        return generateToken(member, accessTokenExpirationMillis);
    }

    public String generateRefreshToken(Member member) {
        return generateToken(member, refreshTokenExpirationMillis);
    }

    public int getMemberId(String token) {
        Claims claims = parseClaims(token);

        try {
            return Integer.parseInt(claims.getSubject());
        } catch (NumberFormatException e) {
            throw new ServiceException("401-1", "유효하지 않은 토큰입니다.");
        }
    }

    private String generateToken(Member member, long expirationMillis) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(String.valueOf(member.getId()))
                .claim("email", member.getEmail())
                .claim("role", member.getRole().name())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new ServiceException("401-1", "만료된 토큰입니다.");
        } catch (JwtException | IllegalArgumentException e) {
            throw new ServiceException("401-1", "유효하지 않은 토큰입니다.");
        }
    }
}
