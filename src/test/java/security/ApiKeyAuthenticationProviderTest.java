package security;

import com.rag.chat.security.ApiKeyAuthenticationProvider;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ApiKeyAuthenticationProviderTest {

    @Test
    void validApiKeyReturnsTrue() {
        ApiKeyAuthenticationProvider provider = new ApiKeyAuthenticationProvider(Set.of("abc123", "def456"), "X-API-Key");
        assertTrue(provider.isValid("abc123"));
        assertTrue(provider.isValid("def456"));
    }

    @Test
    void invalidApiKeyReturnsFalse() {
        ApiKeyAuthenticationProvider provider = new ApiKeyAuthenticationProvider(Set.of("abc123"), "X-API-Key");
        assertFalse(provider.isValid("wrongKey"));
        assertFalse(provider.isValid(null));
        assertFalse(provider.isValid(""));
    }

    @Test
    void correctHeaderNameReturned() {
        ApiKeyAuthenticationProvider provider = new ApiKeyAuthenticationProvider(Set.of("a"), "X-API-Key");
        assertEquals("X-API-Key", provider.getApiKeyHeaderName());
    }
}