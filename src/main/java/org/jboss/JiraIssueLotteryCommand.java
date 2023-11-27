package org.jboss;

import jakarta.inject.Inject;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.component.jira.JiraComponent;
import org.apache.camel.component.jira.JiraConfiguration;
import org.apache.camel.component.jira.JiraEndpoint;
import org.jboss.config.JiraLotteryAppConfig;
import org.jboss.processing.IssueProcessor;
import org.jboss.processing.AllIssuesProcessor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "jira-issue-lottery", mixinStandardHelpOptions = true)
public class JiraIssueLotteryCommand implements Runnable {

    @Inject
    JiraLotteryAppConfig jiraLotteryAppConfig;

    @Inject
    CamelContext camelContext;

    @Parameters(paramLabel = "<name>", defaultValue = "picocli", description = "Your name.")
    String name;

    @Override
    public void run() {
        JiraConfiguration jiraConfiguration = new JiraConfiguration();
        jiraConfiguration.setJiraUrl("https://issues.redhat.com");
        jiraConfiguration.setAccessToken(jiraLotteryAppConfig.accessToken());
        JiraEndpoint jiraEndpoint = new JiraEndpoint("issues.redhat.com", new JiraComponent(camelContext), jiraConfiguration);
        jiraEndpoint.connect();
        AllIssuesProcessor allIssuesProcessor = AllIssuesProcessor.getInstance(jiraEndpoint);
        Exchange exchange = jiraEndpoint.createExchange();
        try {
            allIssuesProcessor.execute();
            new IssueProcessor(jiraEndpoint, "JBEAP-25900").process(exchange);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
