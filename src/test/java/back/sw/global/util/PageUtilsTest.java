package back.sw.global.util;

import back.sw.global.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PageUtilsTest {
    @Test
    void createPageableWithSort() {
        Pageable pageable = PageUtils.createPageable(1, 10, Sort.by(Sort.Direction.DESC, "id"));

        assertEquals(1, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("id").getDirection());
    }

    @Test
    void createPageableWithoutSort() {
        Pageable pageable = PageUtils.createPageable(0, 20);

        assertEquals(0, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());
    }

    @Test
    void throwsWhenPageIsNegative() {
        ServiceException exception = assertThrows(ServiceException.class, () -> PageUtils.validatePageParameters(-1, 10));

        assertEquals("400-1", exception.getRsData().resultCode());
    }

    @Test
    void throwsWhenSizeIsOutOfRange() {
        assertThrows(ServiceException.class, () -> PageUtils.validatePageParameters(0, 0));
        assertThrows(ServiceException.class, () -> PageUtils.validatePageParameters(0, 101));
    }
}
