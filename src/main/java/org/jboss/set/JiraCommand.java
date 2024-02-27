package org.jboss.set;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import org.apache.camel.component.jira.JiraConfiguration;
import org.apache.camel.component.jira.JiraEndpoint;
import org.jboss.set.config.JiraLotteryAppConfig;

public abstract class JiraCommand implements Runnable {

    @Inject
    private JiraLotteryAppConfig jiraLotteryAppConfig;

    @Inject
    private JiraEndpointProducer jiraEndpointProducer;

    private JiraEndpoint jiraEndpoint;

    @PostConstruct
    private void setupJiraEndpoint() {
        JiraConfiguration jiraConfiguration = new JiraConfiguration();
        jiraConfiguration.setJiraUrl("https://issues.redhat.com");
        jiraConfiguration.setAccessToken(jiraLotteryAppConfig.accessToken());

        jiraEndpoint = jiraEndpointProducer.getEndpoint();
        jiraEndpoint.connect();
        jiraEndpoint.setMaxResults(jiraLotteryAppConfig.maxResults());
    }

    protected JiraEndpoint getJiraEndpoint() {
        return jiraEndpoint;
    }
}
