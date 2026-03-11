package back.sw.global.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RsDataTest {
    @Test
    void statusCodeIsParsedFromResultCode() {
        RsData<String> rsData = new RsData<>("200-1", "ok", "data");

        assertEquals("200-1", rsData.resultCode());
        assertEquals(200, rsData.statusCode());
        assertEquals("ok", rsData.msg());
        assertEquals("data", rsData.data());
    }

    @Test
    void invalidResultCodeFallsBackTo500() {
        RsData<Void> rsData = new RsData<>("BAD", "fail");

        assertEquals(500, rsData.statusCode());
    }
}
