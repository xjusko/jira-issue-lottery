package org.jboss.set.state;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.jboss.set.config.GitHubRawUrl;
import org.jboss.set.config.LotteryConfig;
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
                  - email: Tadpole@thehuginn.com
                    maxIssues: 5
                    projects:
                      - project: WFLY
                        components: [Logging]
                        maxIssues: 5""";

        Assertions.assertTrue(objectMapper.getFactory() instanceof YAMLFactory);
        try {
            objectMapper.readValue(configFile, LotteryConfig.class);
        } catch (IOException e) {
            Assertions.fail("Expecting no problem deserializing the config file %s");
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
