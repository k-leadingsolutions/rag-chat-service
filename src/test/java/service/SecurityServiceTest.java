package service;

import com.rag.chat.service.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SecurityServiceTest {

    private SecurityService securityService;

    @BeforeEach
    void setUp() {
        securityService = new SecurityService();
    }

    @Test
    void sanitizeInput_shouldReturnNullIfInputNull() {
        assertNull(securityService.sanitizeInput(null));
    }

    @Test
    void sanitizeInput_shouldReturnUnchangedStringIfNoTags() {
        String input = "Hello, world!";
        assertEquals(input, securityService.sanitizeInput(input));
    }

    @Test
    void sanitizeInput_shouldRemoveScriptTagsAndContent() {
        String input = "Hello<script>alert('XSS')</script>World";
        String expected = "HelloWorld";
        assertEquals(expected, securityService.sanitizeInput(input));
    }

    @Test
    void sanitizeInput_shouldRemoveHtmlTags() {
        String input = "<b>Hello</b> <i>World</i>";
        String expected = "Hello World";
        assertEquals(expected, securityService.sanitizeInput(input));
    }

    @Test
    void sanitizeInput_shouldTrimSanitizedResult() {
        String input = "   <div>  Test  </div>   ";
        String expected = "Test";
        assertEquals(expected, securityService.sanitizeInput(input));
    }

    @Test
    void sanitizeInput_shouldLogWhenSanitizationOccurs() {
        String input = "<script>alert(1)</script>abc";
        String result = securityService.sanitizeInput(input);
        assertEquals("abc", result);
    }
}