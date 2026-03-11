package back.sw.global.security;

import back.sw.global.exception.ServiceException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BearerTokenResolverTest {
    private final BearerTokenResolver bearerTokenResolver = new BearerTokenResolver();

    @Test
    void resolveSuccess() {
        String token = bearerTokenResolver.resolve("Bearer access-token");

        assertEquals("access-token", token);
    }

    @Test
    void resolveFailsWhenHeaderMissing() {
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> bearerTokenResolver.resolve(null)
        );

        assertEquals("400-1", exception.getRsData().resultCode());
    }

    @Test
    void resolveFailsWhenPrefixInvalid() {
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> bearerTokenResolver.resolve("Token access-token")
        );

        assertEquals("400-1", exception.getRsData().resultCode());
    }

    @Test
    void resolveFailsWhenTokenBlank() {
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> bearerTokenResolver.resolve("Bearer   ")
        );

        assertEquals("400-1", exception.getRsData().resultCode());
    }
}
