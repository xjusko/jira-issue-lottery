package org.jboss.set;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.camel.CamelContext;
import org.apache.camel.component.jira.JiraComponent;
import org.apache.camel.component.jira.JiraConfiguration;
import org.apache.camel.component.jira.JiraEndpoint;
import org.jboss.set.config.JiraLotteryAppConfig;
import org.jboss.set.processing.CollectorProducer;
import org.jboss.set.processing.NewIssueCollector;
import org.jboss.set.testing.JiraCommand;
import picocli.CommandLine.Command;

@Command(name = "jira-issue-lottery", mixinStandardHelpOptions = true)
@ApplicationScoped
@JiraCommand
public class JiraIssueLotteryCommand implements Runnable {

    @Inject
    JiraLotteryAppConfig jiraLotteryAppConfig;

    @Inject
    CamelContext camelContext;

    @Inject
    JiraEndpointProducer jiraEndpointProducer;

    @Inject
    CollectorProducer collectorProducer;

    private JiraConfiguration jiraConfiguration;
    private JiraEndpoint jiraEndpoint;

    @PostConstruct
    public void setup() {
        jiraConfiguration = setupJiraConfiguration();
        jiraEndpoint = setupJiraEndpoint(jiraConfiguration);
    }

    private JiraEndpoint setupJiraEndpoint(JiraConfiguration jiraConfiguration) {
        JiraEndpoint jiraEndpoint = jiraEndpointProducer.getEndpoint("issues.redhat.com",
                camelContext.getComponent("jira", JiraComponent.class), jiraConfiguration);
        jiraEndpoint.connect();
        jiraEndpoint.setMaxResults(jiraLotteryAppConfig.maxResults());
        return jiraEndpoint;
    }

    private JiraConfiguration setupJiraConfiguration() {
        JiraConfiguration jiraConfiguration = new JiraConfiguration();
        jiraConfiguration.setJiraUrl("https://issues.redhat.com");
        jiraConfiguration.setAccessToken(jiraLotteryAppConfig.accessToken());
        return jiraConfiguration;
    }

    @Override
    public void run() {
        NewIssueCollector newIssueCollector = collectorProducer.newIssueCollectorInstance(jiraEndpoint);
        try {
            newIssueCollector.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
