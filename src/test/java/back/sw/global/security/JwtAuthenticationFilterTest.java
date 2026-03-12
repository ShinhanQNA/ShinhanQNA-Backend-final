package back.sw.global.security;

import back.sw.global.exception.ServiceException;
import back.sw.global.security.TokenAuthenticationException.TokenErrorType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private BearerTokenResolver bearerTokenResolver;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        RestAuthenticationEntryPoint authenticationEntryPoint =
                new RestAuthenticationEntryPoint(new ObjectMapper());
        jwtAuthenticationFilter = new JwtAuthenticationFilter(
                jwtTokenProvider,
                bearerTokenResolver,
                authenticationEntryPoint
        );
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void setsAuthenticationWhenTokenValid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer access-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(bearerTokenResolver.resolve("Bearer access-token")).thenReturn("access-token");
        when(jwtTokenProvider.getMemberIdFromAccessToken("access-token")).thenReturn(7);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.getPrincipal() instanceof AuthenticatedMember);
        assertEquals(7, ((AuthenticatedMember) authentication.getPrincipal()).memberId());
    }

    @Test
    void skipsAuthenticationWhenAuthorizationHeaderMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(bearerTokenResolver, jwtTokenProvider);
    }

    @Test
    void returns401WhenTokenInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Token invalid");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(bearerTokenResolver.resolve("Token invalid"))
                .thenThrow(new ServiceException("400-1", "Authorization 헤더 형식이 올바르지 않습니다."));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"resultCode\":\"401-1\""));
        assertTrue(response.getContentAsString().contains("\"msg\":\"유효하지 않은 토큰입니다.\""));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void returnsExpiredMessageWhenAccessTokenExpired() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer expired-access-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(bearerTokenResolver.resolve("Bearer expired-access-token")).thenReturn("expired-access-token");
        when(jwtTokenProvider.getMemberIdFromAccessToken("expired-access-token"))
                .thenThrow(new TokenAuthenticationException(TokenErrorType.EXPIRED, "만료된 토큰입니다."));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"msg\":\"만료된 토큰입니다.\""));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
