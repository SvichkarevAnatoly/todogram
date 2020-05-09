package com.example;

import com.example.config.SecurityConfig;
import org.aeonbits.owner.ConfigFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    private static final String TEMPLATE_VALUE = "<your value>";

    private static SecurityConfig config;

    @BeforeAll
    static void setUp() throws IOException {
        final InputStream input = new FileInputStream(".env");
        final Properties envProps = new Properties();
        envProps.load(input);

        config = ConfigFactory.create(SecurityConfig.class, envProps);
    }

    @Test
    void botToken() {
        assertThat(config.botToken())
                .isNotBlank()
                .isNotEqualTo(TEMPLATE_VALUE);
    }

    @Test
    void botName() {
        assertThat(config.botName())
                .isNotBlank()
                .isNotEqualTo(TEMPLATE_VALUE);
    }

    @Test
    void proxyUser() {
        assertThat(config.proxyUser())
                .isNotBlank()
                .isNotEqualTo("<your value>");
    }

    @Test
    void proxyPassword() {
        assertThat(config.proxyPassword())
                .isNotBlank()
                .isNotEqualTo("<your value>");
    }
}