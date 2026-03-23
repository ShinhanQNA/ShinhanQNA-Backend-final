package back.sw.global.handler;

import back.sw.global.exception.ServiceException;
import back.sw.global.response.RsData;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlesNoSuchElementException() {
        ResponseEntity<RsData<Void>> response = handler.handle(new NoSuchElementException("not found"));

        assertEquals(404, response.getStatusCode().value());
        assertEquals("404-1", response.getBody().resultCode());
    }

    @Test
    void handlesIllegalArgumentException() {
        ResponseEntity<RsData<Void>> response = handler.handle(new IllegalArgumentException("invalid"));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("400-1", response.getBody().resultCode());
        assertEquals("invalid", response.getBody().msg());
    }

    @Test
    void handlesIllegalStateException() {
        ResponseEntity<RsData<Void>> response = handler.handle(new IllegalStateException("state"));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("400-2", response.getBody().resultCode());
        assertEquals("state", response.getBody().msg());
    }

    @Test
    void handlesUnreadableHttpMessage() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException(
                "bad body",
                new MockHttpInputMessage("{}".getBytes(StandardCharsets.UTF_8))
        );

        ResponseEntity<RsData<Void>> response = handler.handle(exception);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("400-1", response.getBody().resultCode());
    }

    @Test
    void handlesServiceExceptionAndSetsServletStatus() {
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServiceException exception = new ServiceException("401-1", "로그인이 필요합니다.");

        RsData<Void> body = handler.handle(exception, servletResponse);

        assertEquals(401, servletResponse.getStatus());
        assertEquals("401-1", body.resultCode());
        assertEquals("로그인이 필요합니다.", body.msg());
    }
}
