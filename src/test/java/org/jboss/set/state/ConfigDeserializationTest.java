package org.jboss.set.state;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.jboss.config.LotteryConfig;
import org.jboss.config.GitHubRawUrl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@QuarkusTest
public class ConfigDeserializationTest {

    @Inject
    ObjectMapper objectMapper;

    @Test
    void testDeserializingSimpleConfigFile() {
        String configFile = """
                delay: P14D
                participants:
                  - email: The-Huginn@thehuginn.com
                    projects:
                      - project: WFLY
                        components: [Logging]
                        maxIssues: 5
                availableProjects:
                  WFLY:
                    - Logging
                    - JMS
                """;

        Assertions.assertTrue(objectMapper.getFactory() instanceof YAMLFactory);
        try {
            objectMapper.readValue(configFile, LotteryConfig.class);
        } catch (IOException e) {
            Assertions.fail("Expecting no problem deserializing the config file, but encountered an error", e);
        }
    }

    @Test
    void testConvertingGitHubRepoUrlWithoutTrailingSlash() {
        String repoUrl = "https://github.com/The-Huginn/jira-issue-lottery";
        GitHubRawUrl gitHubRawUrl = new GitHubRawUrl.GitHubRepoToRawUrlConverter().convert(repoUrl);
        Assertions.assertEquals(
                "https://raw.githubusercontent.com/The-Huginn/jira-issue-lottery/main/.github/jira-issue-lottery.yml",
                gitHubRawUrl.getRawContentsUrl());
    }

    @Test
    void testConvertingGitHubRepoUrlWithTrailingSlash() {
        String repoUrl = "https://github.com/The-Huginn/jira-issue-lottery/";
        GitHubRawUrl gitHubRawUrl = new GitHubRawUrl.GitHubRepoToRawUrlConverter().convert(repoUrl);
        Assertions.assertEquals(
                "https://raw.githubusercontent.com/The-Huginn/jira-issue-lottery/main/.github/jira-issue-lottery.yml",
                gitHubRawUrl.getRawContentsUrl());
    }
}
