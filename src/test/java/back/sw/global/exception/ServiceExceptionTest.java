package back.sw.global.exception;

import back.sw.global.response.RsData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ServiceExceptionTest {
    @Test
    void convertsToRsData() {
        ServiceException exception = new ServiceException("400-1", "잘못된 요청");

        RsData<Void> rsData = exception.getRsData();

        assertEquals("400-1", rsData.resultCode());
        assertEquals(400, rsData.statusCode());
        assertEquals("잘못된 요청", rsData.msg());
        assertNull(rsData.data());
    }
}
