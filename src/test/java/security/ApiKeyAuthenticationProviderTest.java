package security;

import com.rag.chat.security.ApiKeyAuthenticationProvider;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ApiKeyAuthenticationProviderTest {

    @Test
    void validApiKeyReturnsTrue() {
        ApiKeyAuthenticationProvider provider = new ApiKeyAuthenticationProvider(Set.of("abc123", "def456"), "x-api-key");
        assertTrue(provider.isValid("abc123"));
        assertTrue(provider.isValid("def456"));
    }

    @Test
    void invalidApiKeyReturnsFalse() {
        ApiKeyAuthenticationProvider provider = new ApiKeyAuthenticationProvider(Set.of("abc123"), "x-api-key");
        assertFalse(provider.isValid("wrongKey"));
        assertFalse(provider.isValid(null));
        assertFalse(provider.isValid(""));
    }

    @Test
    void correctHeaderNameReturned() {
        ApiKeyAuthenticationProvider provider = new ApiKeyAuthenticationProvider(Set.of("a"), "x-api-key");
        assertEquals("x-api-key", provider.getApiKeyHeaderName());
    }
}