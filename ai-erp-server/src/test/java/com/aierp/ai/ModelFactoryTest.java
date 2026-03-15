package com.aierp.ai;

import com.aierp.ai.adapter.ClaudeAdapter;
import com.aierp.ai.adapter.OpenAIAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AI模型工厂测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ModelFactoryTest {

    @Mock
    private ClaudeAdapter claudeAdapter;

    @Mock
    private OpenAIAdapter openAIAdapter;

    @InjectMocks
    private ModelFactory modelFactory;

    @BeforeEach
    void setUp() {
        // Default: both adapters available
        when(claudeAdapter.isAvailable()).thenReturn(true);
        when(openAIAdapter.isAvailable()).thenReturn(true);
        when(claudeAdapter.getModelName()).thenReturn("claude");
        when(openAIAdapter.getModelName()).thenReturn("openai");
    }

    @Test
    void testGetDefaultModel() {
        // When
        ModelAdapter model = modelFactory.getDefaultModel();

        // Then
        assertNotNull(model);
        assertEquals("claude", model.getModelName());
    }

    @Test
    void testGetModel_Claude() {
        // When
        ModelAdapter model = modelFactory.getModel("claude");

        // Then
        assertNotNull(model);
        assertEquals("claude", model.getModelName());
    }

    @Test
    void testGetModel_OpenAI() {
        // When
        ModelAdapter model = modelFactory.getModel("openai");

        // Then
        assertNotNull(model);
        assertEquals("openai", model.getModelName());
    }

    @Test
    void testGetModel_Gpt() {
        // When - "gpt" should also return OpenAI adapter
        ModelAdapter model = modelFactory.getModel("gpt");

        // Then
        assertNotNull(model);
        assertEquals("openai", model.getModelName());
    }

    @Test
    void testGetModel_UnknownDefaultsToClaude() {
        // When
        ModelAdapter model = modelFactory.getModel("unknown");

        // Then
        assertNotNull(model);
        assertEquals("claude", model.getModelName());
    }

    @Test
    void testGetModel_FallbackWhenClaudeUnavailable() {
        // Given
        when(claudeAdapter.isAvailable()).thenReturn(false);

        // When
        ModelAdapter model = modelFactory.getModel("claude");

        // Then - should fallback to OpenAI
        assertNotNull(model);
    }

    @Test
    void testGetAvailableModels() {
        // When
        List<String> available = modelFactory.getAvailableModels();

        // Then
        assertNotNull(available);
        assertTrue(available.contains("claude"));
        assertTrue(available.contains("openai"));
    }

    @Test
    void testHasAvailableModel_True() {
        // When
        boolean hasModel = modelFactory.hasAvailableModel();

        // Then
        assertTrue(hasModel);
    }

    @Test
    void testHasAvailableModel_False() {
        // Given
        when(claudeAdapter.isAvailable()).thenReturn(false);
        when(openAIAdapter.isAvailable()).thenReturn(false);

        // When
        boolean hasModel = modelFactory.hasAvailableModel();

        // Then
        assertFalse(hasModel);
    }
}
