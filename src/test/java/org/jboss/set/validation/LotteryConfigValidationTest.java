package org.jboss.set.validation;

import com.atlassian.jira.rest.client.api.domain.Project;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.jboss.set.ValidationCommand;
import org.jboss.set.config.LotteryConfig;
import org.jboss.set.draw.AbstractCommandTest;
import org.jboss.set.helper.MockedGitHub;
import org.jboss.set.util.GitHubProducer;
import org.jboss.set.wrappers.ComponentWrapper;
import org.jboss.set.wrappers.ProjectWrapper;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class LotteryConfigValidationTest extends AbstractCommandTest {

    @Inject
    ValidationCommand app;

    @InjectMock
    GitHubProducer gitHubProducer;

    @Override
    protected List<Project> setupProjects() {
        return List.of(
                new ProjectWrapper("WFLY", List.of(new ComponentWrapper("Logging"), new ComponentWrapper("Documentation"))),
                new ProjectWrapper("RESTEASY", List.of(new ComponentWrapper("Logging"), new ComponentWrapper("Documentation"))),
                new ProjectWrapper("WFCORE", List.of(new ComponentWrapper("Batch"), new ComponentWrapper("CDI / Weld"))));
    }

    @Test
    public void testCorrectConfigFileWithSingleEntry() throws IOException {
        String configFile = """
                delay: P14D
                participants:
                  - email: Tadpole@thehuginn.com
                    maxIssues: 1
                    projects:
                      - project: WFLY
                        maxIssues: 2""";

        MockedGitHub.builder().prFiles(".github/" + LotteryConfig.FILE_NAME).prConfigFileContent(configFile)
                .mockGitHub(gitHubProducer);
        StringWriter sw = new StringWriter();
        CommandLine cmd = new CommandLine(app);
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute("1");
        assertEquals(0, exitCode);
    }

    @Test
    public void testIncorrectComponentsConfigFileWithSingleEntry() throws IOException {
        String email = "Tadpole@thehuginn.com";
        String configFile = """
                delay: P14D
                participants:
                  - email: %s
                    maxIssues: 1
                    projects:
                      - project: WFLY
                        components: [Logging, Batch]
                        maxIssues: 2""".formatted(email);

        MockedGitHub.builder().prFiles(".github/" + LotteryConfig.FILE_NAME).prConfigFileContent(configFile)
                .mockGitHub(gitHubProducer);
        StringWriter sw = new StringWriter();
        CommandLine cmd = new CommandLine(app);
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute("1");
        assertEquals(1, exitCode);
        assertEquals(
                error("For participant %s and project %s some components were found under the project based on JIRA. A typo in one or more components is thus expected.\n")
                        .formatted(email, "WFLY"),
                sw.toString());
    }

    @Test
    public void testNegativeGlobalMaxIssuesConfigFileWithSingleEntry() throws IOException {
        String email = "Tadpole@thehuginn.com";
        String configFile = """
                delay: P14D
                participants:
                  - email: %s
                    maxIssues: 0
                    projects:
                      - project: WFLY
                        maxIssues: 2""".formatted(email);

        MockedGitHub.builder().prFiles(".github/" + LotteryConfig.FILE_NAME).prConfigFileContent(configFile)
                .mockGitHub(gitHubProducer);
        StringWriter sw = new StringWriter();
        CommandLine cmd = new CommandLine(app);
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute("1");
        assertEquals(1, exitCode);
        assertEquals(error("Participant %s has defined non-positive number of max issues\n").formatted(email), sw.toString());
    }

    @Test
    public void testNegativeProjectMaxIssuesConfigFileWithSingleEntry() throws IOException {
        String email = "Tadpole@thehuginn.com";
        String configFile = """
                delay: P14D
                participants:
                  - email: %s
                    projects:
                      - project: WFLY
                        maxIssues: 0""".formatted(email);

        MockedGitHub.builder().prFiles(".github/" + LotteryConfig.FILE_NAME).prConfigFileContent(configFile)
                .mockGitHub(gitHubProducer);
        StringWriter sw = new StringWriter();
        CommandLine cmd = new CommandLine(app);
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute("1");
        assertEquals(1, exitCode);
        assertEquals(
                error("Participant %s has defined non-positive number of max issues for project %s\n").formatted(email, "WFLY"),
                sw.toString());
    }

    @Test
    public void testNonExistingProjectInConfigFileWithSingleEntry() throws IOException {
        String email = "Tadpole@thehuginn.com";
        String configFile = """
                delay: P14D
                participants:
                  - email: %s
                    projects:
                      - project: MISSING_WFLY
                        maxIssues: 1""".formatted(email);

        MockedGitHub.builder().prFiles(".github/" + LotteryConfig.FILE_NAME).prConfigFileContent(configFile)
                .mockGitHub(gitHubProducer);
        StringWriter sw = new StringWriter();
        CommandLine cmd = new CommandLine(app);
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute("1");
        assertEquals(1, exitCode);
        assertEquals(
                error("For participant %s and project %s no components were found on JIRA. You probably made a typo.\n")
                        .formatted(email, "MISSING_WFLY"),
                sw.toString());
    }

    @Test
    public void testTwoIdenticalUsersInConfigFile() throws IOException {
        String email = "Tadpole@thehuginn.com";
        String configFile = """
                delay: P14D
                participants:
                  - email: %1$s
                    projects:
                      - project: WFLY
                        maxIssues: 1
                  - email: %1$s
                    projects:
                      - project: WFLY
                        maxIssues: 1""".formatted(email);

        MockedGitHub.builder().prFiles(".github/" + LotteryConfig.FILE_NAME).prConfigFileContent(configFile)
                .mockGitHub(gitHubProducer);
        StringWriter sw = new StringWriter();
        CommandLine cmd = new CommandLine(app);
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute("1");
        assertEquals(1, exitCode);
        assertEquals(
                error("Two identical participants detected %s\n").formatted(email),
                sw.toString());
    }

    private String error(String message) {
        return "::error::" + message;
    }
}
