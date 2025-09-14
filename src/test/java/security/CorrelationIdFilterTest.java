package security;

import com.rag.chat.security.CorrelationIdFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CorrelationIdFilterTest {

    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;
    @Mock
    FilterChain filterChain;

    CorrelationIdFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filter = new CorrelationIdFilter();
        MDC.clear();
    }

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void doFilter_blanksHeader_generatesId() throws IOException, ServletException {
        when(request.getHeader(CorrelationIdFilter.HEADER)).thenReturn("   ");

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response).setHeader(eq(CorrelationIdFilter.HEADER), captor.capture());
        String generatedId = captor.getValue();
        assertNotNull(generatedId);
        assertDoesNotThrow(() -> UUID.fromString(generatedId));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_removesMdcAfterwards() throws IOException, ServletException {
        when(request.getHeader(CorrelationIdFilter.HEADER)).thenReturn("foo-bar-id");

        filter.doFilter(request, response, filterChain);

        assertNull(MDC.get("requestId"));
    }
}