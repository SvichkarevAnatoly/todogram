package com.example.bot;

import com.example.config.BotConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class CaptionServiceTest {

    @Mock
    private RequestService requestService;

    @Mock
    private BotConfig config;

    private CaptionService captionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        captionService = new CaptionService(config, requestService);
    }

    @Test
    void getCaption() {
        when(config.sloganWelcomeTemplate()).thenReturn("hi %s ");
        when(requestService.getJson(anyString()))
                .thenReturn("json");
        when(requestService.getAttribute(anyString(), anyString()))
                .thenReturn("slogan");

        assertEquals("hi userName slogan",
                captionService.getCaption("userName"));
    }
}