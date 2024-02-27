package org.jboss.set;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.camel.CamelContext;
import org.apache.camel.component.jira.JiraComponent;
import org.apache.camel.component.jira.JiraConfiguration;
import org.apache.camel.component.jira.JiraEndpoint;
import org.jboss.set.config.JiraLotteryAppConfig;

@ApplicationScoped
public class JiraEndpointProducer {

    @Inject
    JiraLotteryAppConfig jiraLotteryAppConfig;

    @Inject
    CamelContext camelContext;

    private JiraConfiguration jiraConfiguration;

    @PostConstruct
    public void setup() {
        jiraConfiguration = new JiraConfiguration();
        jiraConfiguration.setJiraUrl("https://issues.redhat.com");
        jiraConfiguration.setAccessToken(jiraLotteryAppConfig.accessToken());
    }

    public JiraEndpoint getEndpoint() {
        return new JiraEndpoint("issues.redhat.com", camelContext.getComponent("jira", JiraComponent.class), jiraConfiguration);
    }
}
