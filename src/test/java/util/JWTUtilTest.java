package util;

import com.rag.chat.util.JWTUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JWTUtilTest {

    private JWTUtil jwtUtil;

    // Minimum 32 chars for HS256
    private static final String SECRET = "mysecretkey12345678901234567890123";
    private static final long EXPIRATION_MS = 1000 * 60 * 60; // 1 hour
    private static final String ISSUER = "test-issuer";
    private static final String AUDIENCE = "test-audience";

    @BeforeEach
    void setUp() {
        jwtUtil = new JWTUtil(SECRET, EXPIRATION_MS, ISSUER, AUDIENCE);
    }

    @Test
    void generateServiceToken_and_validateToken_success() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("service", "rag-chat");
        claims.put("scope", "read:messages");

        String token = jwtUtil.generateServiceToken(claims);
        assertNotNull(token);

        Jws<Claims> parsed = jwtUtil.validateToken(token);
        assertEquals(ISSUER, parsed.getBody().getIssuer());
        assertEquals(AUDIENCE, parsed.getBody().getAudience());
        assertEquals("rag-chat", parsed.getBody().get("service", String.class));
        assertEquals("read:messages", parsed.getBody().get("scope", String.class));
        assertNotNull(parsed.getBody().getIssuedAt());
        assertNotNull(parsed.getBody().getExpiration());
    }

    @Test
    void validateToken_withWrongSecret_shouldFail() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("service", "rag-chat");

        String token = jwtUtil.generateServiceToken(claims);

        // Create a JwtUtil with a different secret
        JWTUtil wrongJWTUtil = new JWTUtil("anothersecretkeythatissupersecure123456", EXPIRATION_MS, ISSUER, AUDIENCE);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> wrongJWTUtil.validateToken(token));
        assertTrue(ex.getMessage().contains("Invalid or expired JWT token"));
    }

    @Test
    void validateToken_withWrongIssuer_shouldFail() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("service", "rag-chat");

        String token = jwtUtil.generateServiceToken(claims);

        JWTUtil wrongIssuerUtil = new JWTUtil(SECRET, EXPIRATION_MS, "wrong-issuer", AUDIENCE);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> wrongIssuerUtil.validateToken(token));
        assertTrue(ex.getMessage().contains("Invalid or expired JWT token"));
    }

    @Test
    void validateToken_withWrongAudience_shouldFail() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("service", "rag-chat");

        String token = jwtUtil.generateServiceToken(claims);

        JWTUtil wrongAudienceUtil = new JWTUtil(SECRET, EXPIRATION_MS, ISSUER, "wrong-audience");

        Exception ex = assertThrows(IllegalArgumentException.class, () -> wrongAudienceUtil.validateToken(token));
        assertTrue(ex.getMessage().contains("Invalid or expired JWT token"));
    }

    @Test
    void constructor_secretTooShort_shouldThrow() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                new JWTUtil("shortsecret", EXPIRATION_MS, ISSUER, AUDIENCE)
        );
        assertEquals("JWT secret must be at least 32 characters (HS256).", ex.getMessage());
    }
}