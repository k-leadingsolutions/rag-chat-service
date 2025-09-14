package util;

import com.fasterxml.jackson.databind.JsonNode;
import com.rag.chat.util.JSONSerializerDeserializerUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class JSONSerializerDeserializerUtilTest {

    static class TestPOJO {
        public String foo;
        public int bar;

        public TestPOJO(String foo, int bar) {
            this.foo = foo;
            this.bar = bar;
        }
        public TestPOJO() {}
    }

    @Test
    void deserialize_validJson_returnsJsonNode() {
        String json = "{\"foo\":\"hello\",\"bar\":42}";
        JsonNode node = JSONSerializerDeserializerUtil.deserialize(json);
        assertNotNull(node);
        assertEquals("hello", node.get("foo").asText());
        assertEquals(42, node.get("bar").asInt());
    }

    @Test
    void deserialize_invalidJson_returnsNull() {
        String json = "{foo:hello,bar:42}";
        JsonNode node = JSONSerializerDeserializerUtil.deserialize(json);
        assertNull(node);
    }

    @Test
    void deserialize_nullOrBlank_returnsNull() {
        assertNull(JSONSerializerDeserializerUtil.deserialize(null));
        assertNull(JSONSerializerDeserializerUtil.deserialize(""));
        assertNull(JSONSerializerDeserializerUtil.deserialize("   "));
    }

    @Test
    void serialize_validObject_returnsJsonString() {
        TestPOJO pojo = new TestPOJO("world", 123);
        String json = JSONSerializerDeserializerUtil.serialize(pojo, "testPojo");
        assertNotNull(json);
        assertTrue(json.contains("\"foo\":\"world\""));
        assertTrue(json.contains("\"bar\":123"));
    }

    @Test
    void serialize_nullObject_returnsNull() {
        assertNull(JSONSerializerDeserializerUtil.serialize(null, "nullField"));
    }

    @Test
    void serialize_unserializableObject_returnsNull() {
        Object unserializable = new Object() {
            private void writeObject(java.io.ObjectOutputStream out) throws IOException {
                throw new IOException("Cannot serialize");
            }
        };
        String json = JSONSerializerDeserializerUtil.serialize(unserializable, "unserializableField");
        assertNull(json);
    }
}