package org.jboss;

import com.atlassian.jira.rest.client.api.domain.Comment;
import org.jboss.config.JiraLotteryAppConfig;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.jira.JiraComponent;
import org.apache.camel.component.jira.JiraConfiguration;
import org.apache.camel.component.jira.JiraConstants;
import org.apache.camel.component.jira.JiraEndpoint;
import org.apache.camel.component.jira.JiraType;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.stream.Collectors;

@Command(name = "jira-issue-lottery", mixinStandardHelpOptions = true)
public class JiraIssueLotteryCommand implements Runnable {

    @Inject
    JiraLotteryAppConfig jiraLotteryAppConfig;

    @Inject
    CamelContext camelContext;

    @Parameters(paramLabel = "<name>", defaultValue = "picocli", description = "Your name.")
    String name;

    public static class IssueProcessor implements Processor {

        @Override
        public void process(Exchange exchange) throws Exception {
            //noinspection unchecked
            List<Comment> issues = (List<Comment>) exchange.getIn().getBody(List.class);
            String comments = issues.stream().map(Comment::getBody)
                    .collect(Collectors.joining("\n\n"));
            Log.info(comments);
        }
    }

    @Override
    public void run() {
        JiraConfiguration jiraConfiguration = new JiraConfiguration();
        jiraConfiguration.setJiraUrl("https://issues.redhat.com");
        jiraConfiguration.setAccessToken(jiraLotteryAppConfig.accessToken());
        JiraEndpoint jiraEndpoint = new JiraEndpoint("issues.redhat.com", new JiraComponent(camelContext), jiraConfiguration);
        jiraEndpoint.connect();
        Exchange exchange = jiraEndpoint.createExchange();
        exchange.getIn().setHeader(JiraConstants.ISSUE_KEY, "JBEAP-25900");
        jiraEndpoint.setType(JiraType.FETCHCOMMENTS);
        try {
            jiraEndpoint.createProducer().process(exchange);
            new IssueProcessor().process(exchange);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
