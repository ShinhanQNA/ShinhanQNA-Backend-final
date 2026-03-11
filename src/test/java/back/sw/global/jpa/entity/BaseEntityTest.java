package back.sw.global.jpa.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class BaseEntityTest {
    @Test
    void createsSubclassInstance() {
        DummyEntity entity = new DummyEntity();

        assertNotNull(entity);
    }

    private static class DummyEntity extends BaseEntity {
    }
}
