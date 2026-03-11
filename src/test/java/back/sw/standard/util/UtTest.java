package back.sw.standard.util;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtTest {
    @Test
    void convertsObjectToJsonString() {
        String json = Ut.Json.toString(Map.of("key", "value"));

        assertTrue(json.contains("\"key\":\"value\""));
    }

    @Test
    void returnsDefaultValueWhenSerializationFails() {
        SelfRef selfRef = new SelfRef();
        selfRef.self = selfRef;
        assertTrue(selfRef.self == selfRef);

        String json = Ut.Json.toString(selfRef, "{}");

        assertEquals("{}", json);
    }

    private static class SelfRef {
        private SelfRef self;
    }
}
