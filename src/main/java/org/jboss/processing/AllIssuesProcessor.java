package org.jboss.processing;

import com.atlassian.jira.rest.client.api.domain.Issue;
import io.quarkus.logging.Log;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.jira.JiraEndpoint;
import org.apache.camel.component.jira.consumer.NewIssuesConsumer;

public class AllIssuesProcessor extends NewIssuesConsumer implements Executable {

    private AllIssuesProcessor(JiraEndpoint jiraEndpoint) {
        super(jiraEndpoint, new PerIssueProcessor());
    }

    private static final class PerIssueProcessor implements Processor {

        @Override
        public void process(Exchange exchange) throws Exception {
            Log.info(exchange.getIn().getBody(Issue.class).getId());
        }
    }

    public static AllIssuesProcessor getInstance(JiraEndpoint jiraEndpoint) {
        jiraEndpoint.setJql(
                "project in (JBEAP, WFLY, WFCORE, RESTEASY) AND component not in (Documentation, Localization) AND assignee = rhn-support-rbudinsk");
        return new AllIssuesProcessor(jiraEndpoint);
    }

    @Override
    public void execute() throws Exception {
        doPoll();
    }
}
