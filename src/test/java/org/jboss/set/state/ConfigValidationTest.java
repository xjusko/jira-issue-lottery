package org.jboss.set.state;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.jboss.config.LotteryConfig;
import org.jboss.config.LotteryConfigValidation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@QuarkusTest
public class ConfigValidationTest {

    @Inject
    ObjectMapper objectMapper;

    @Inject
    LotteryConfigValidation lotteryConfigValidation;

    @Test
    void testValidConfiguration() {
        String configFile = """
                delay: P14D
                participants:
                  - email: The-Huginn@thehuginn.com
                    projects:
                      - project: WFLY
                        components: [Logging]
                        maxIssues: 5
                      - project: ELY
                        components: [HTTP, Core]
                        maxIssues: 3
                availableProjects:
                  WFLY:
                    - Logging
                    - JMS
                  ELY:
                    - HTTP
                    - Core
                  UNDERTOW:
                    - Servlet
                    - Core
                """;

        // No exceptions should be thrown
        Assertions.assertDoesNotThrow(() -> validateWithConfig(configFile));
    }

    @Test
    void testParticipantWithInvalidProject() {
        String configFile = """
                delay: P14D
                participants:
                  - email: The-Huginn@thehuginn.com
                    projects:
                      - project: INVALID_PROJECT
                        components: [Logging]
                        maxIssues: 5
                availableProjects:
                  WFLY:
                    - Logging
                """;
        Assertions.assertThrows(RuntimeException.class, () -> validateWithConfig(configFile));
    }

    @Test
    void testParticipantWithInvalidComponent() {
        String configFile = """
                delay: P14D
                participants:
                  - email: The-Huginn@thehuginn.com
                    projects:
                      - project: WFLY
                        components: [INVALID_COMPONENT]
                        maxIssues: 5
                availableProjects:
                  WFLY:
                    - Logging
                """;
        Assertions.assertThrows(RuntimeException.class, () -> validateWithConfig(configFile));
    }

    @Test
    void testParticipantWithMultipleInvalidComponents() {
        String configFile = """
                delay: P14D
                participants:
                  - email: The-Huginn@thehuginn.com
                    projects:
                      - project: WFLY
                        components: [INVALID_COMPONENT1, INVALID_COMPONENT2]
                        maxIssues: 5
                availableProjects:
                  WFLY:
                    - Logging
                """;
        Assertions.assertThrows(RuntimeException.class, () -> validateWithConfig(configFile));
    }

    // Helper method to handle configuration validation
    private void validateWithConfig(String configFile) {
        Assertions.assertTrue(objectMapper.getFactory() instanceof YAMLFactory);
        try {
            LotteryConfig config = objectMapper.readValue(configFile, LotteryConfig.class);
            lotteryConfigValidation.validate(config);
        } catch (IOException e) {
            throw new RuntimeException("Validation failed", e);
        }
    }
}
