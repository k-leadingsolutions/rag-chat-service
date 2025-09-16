package util;

import com.rag.chat.util.Translator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TranslatorTest {

    private MessageSource messageSource;
    private Translator translator;

    @BeforeEach
    void setUp() {
        messageSource = mock(MessageSource.class);
        translator = new Translator(messageSource);
    }

    @Test
    void testToLocale_WithExplicitLocale() {
        String msgCode = "greeting";
        Locale locale = new Locale("ar");
        String expectedMessage = "مرحبا";

        when(messageSource.getMessage(msgCode, null, locale)).thenReturn(expectedMessage);

        String result = translator.toLocale(msgCode, locale);

        assertEquals(expectedMessage, result);
        verify(messageSource, times(1)).getMessage(msgCode, null, locale);
    }

    @Test
    void testToLocale_WithNullLocale_UsesDefault() {
        String msgCode = "greeting";
        Locale defaultLocale = Locale.getDefault();
        String expectedMessage = "Hello";

        when(messageSource.getMessage(msgCode, null, defaultLocale)).thenReturn(expectedMessage);

        String result = translator.toLocale(msgCode, null);

        assertEquals(expectedMessage, result);
        verify(messageSource, times(1)).getMessage(msgCode, null, defaultLocale);
    }
}