package security;

import com.rag.chat.security.JwtAuthenticationProvider;
import com.rag.chat.util.JWTUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;

class JwtAuthenticationProviderTest {

    private JWTUtil jwtUtil;
    private JwtAuthenticationProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtUtil = Mockito.mock(JWTUtil.class);
        jwtProvider = new JwtAuthenticationProvider(jwtUtil);
    }

    @Test
    void validTokenReturnsClaims() {
        Claims claims = Mockito.mock(Claims.class);
        Jws<Claims> jwsClaims = Mockito.mock(Jws.class);
        Mockito.when(jwsClaims.getBody()).thenReturn(claims);
        Mockito.when(jwtUtil.validateToken(anyString())).thenReturn(jwsClaims);

        String header = "Bearer valid.jwt.token";
        Claims result = jwtProvider.validate(header);

        assertNotNull(result);
        Mockito.verify(jwtUtil).validateToken("valid.jwt.token");
    }

    @Test
    void invalidHeaderThrows() {
        assertThrows(JwtException.class, () -> jwtProvider.validate("NotBearerToken"));
    }

    @Test
    void emptyTokenThrows() {
        assertThrows(JwtException.class, () -> jwtProvider.validate("Bearer   "));
    }

    @Test
    void jwtUtilThrowsPropagates() {
        Mockito.when(jwtUtil.validateToken(anyString())).thenThrow(new JwtException("bad token"));
        assertThrows(JwtException.class, () -> jwtProvider.validate("Bearer bad.token"));
    }
}