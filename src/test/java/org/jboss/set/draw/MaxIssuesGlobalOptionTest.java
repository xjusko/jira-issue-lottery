package org.jboss.set.draw;

import com.atlassian.jira.rest.client.api.domain.Issue;
import io.quarkus.mailer.Mail;
import io.quarkus.test.junit.QuarkusTest;
import org.jboss.set.config.LotteryConfig;
import org.jboss.set.query.IssueStatus;
import org.jboss.set.wrappers.IssueWrapper;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@QuarkusTest
public class MaxIssuesGlobalOptionTest extends AbstractLotteryTest {

    @Override
    protected List<Issue> setupIssues() {
        try {
            return List.of(
                    new IssueWrapper("Issue", 1L, "WFLY", IssueStatus.NEW, Set.of("Documentation")),
                    new IssueWrapper("Issue", 2L, "WFLY", IssueStatus.NEW, Set.of("Documentation")),
                    new IssueWrapper("Issue", 3L, "WFLY", IssueStatus.NEW, Set.of("Documentation")),
                    new IssueWrapper("Issue", 4L, "RESTEASY", IssueStatus.NEW, Set.of("Logging")),
                    new IssueWrapper("Issue", 5L, "RESTEASY", IssueStatus.NEW, Set.of("Logging")),
                    new IssueWrapper("Issue", 6L, "RESTEASY", IssueStatus.NEW, Set.of("Logging")),
                    new IssueWrapper("Issue", 7L, "WELD", IssueStatus.NEW, Set.of("Logging")),
                    new IssueWrapper("Issue", 8L, "WELD", IssueStatus.NEW, Set.of("Logging")),
                    new IssueWrapper("Issue", 9L, "WELD", IssueStatus.NEW, Set.of("Logging")));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testAssigningOneIssue() throws Exception {
        String email = "The-Huginn@thehuginn.com";
        String configFile = """
                delay: P14D
                participants:
                  - email: The-Huginn@thehuginn.com
                    maxIssues: 1
                    projects:
                      - project: WFLY
                        maxIssues: 2""";

        LotteryConfig testLotteryConfig = objectMapper.readValue(configFile, LotteryConfig.class);
        when(lotteryConfigProducer.getLotteryConfig()).thenReturn(testLotteryConfig);

        StringWriter sw = new StringWriter();
        CommandLine cmd = new CommandLine(app);
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute();
        assertEquals(0, exitCode);

        List<Mail> sent = mailbox.getMailsSentTo(email);
        assertEquals(1, sent.size());
        assertEquals(Lottery.createEmailText(email, List.of(ourIssues.get(0))), sent.get(0).getText());
    }

    @Test
    public void testRespectingGlobalMaxIssuesWithTwoProjects() throws Exception {
        String email = "The-Huginn@thehuginn.com";
        String configFile = """
                delay: P14D
                participants:
                  - email: The-Huginn@thehuginn.com
                    maxIssues: 5
                    projects:
                      - project: WFLY
                        maxIssues: 5
                      - project: RESTEASY
                        maxIssues: 5""";

        LotteryConfig testLotteryConfig = objectMapper.readValue(configFile, LotteryConfig.class);
        when(lotteryConfigProducer.getLotteryConfig()).thenReturn(testLotteryConfig);

        StringWriter sw = new StringWriter();
        CommandLine cmd = new CommandLine(app);
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute();
        assertEquals(0, exitCode);

        List<Mail> sent = mailbox.getMailsSentTo(email);
        assertEquals(1, sent.size());
        assertEquals(Lottery.createEmailText(email, ourIssues.subList(0, 5)), sent.get(0).getText());
    }

    @Test
    public void testRespectingGlobalMaxIssuesWithThreeProjects() throws Exception {
        String email = "The-Huginn@thehuginn.com";
        String configFile = """
                delay: P14D
                participants:
                  - email: The-Huginn@thehuginn.com
                    maxIssues: 5
                    projects:
                      - project: WFLY
                        maxIssues: 2
                      - project: RESTEASY
                        maxIssues: 2
                      - project: WELD
                        maxIssues: 2""";

        LotteryConfig testLotteryConfig = objectMapper.readValue(configFile, LotteryConfig.class);
        when(lotteryConfigProducer.getLotteryConfig()).thenReturn(testLotteryConfig);

        StringWriter sw = new StringWriter();
        CommandLine cmd = new CommandLine(app);
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute();
        assertEquals(0, exitCode);

        List<Mail> sent = mailbox.getMailsSentTo(email);
        assertEquals(1, sent.size());
        assertEquals(Lottery.createEmailText(email, List.of(
                ourIssues.get(0), ourIssues.get(1),
                ourIssues.get(3), ourIssues.get(4),
                ourIssues.get(6))), sent.get(0).getText());
    }
}
