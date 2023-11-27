package org.jboss.processing;

import com.atlassian.jira.rest.client.api.domain.Issue;
import io.quarkus.logging.Log;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.jira.JiraEndpoint;

public class AllIssuesProcessor {

    public AllIssuesProcessor(JiraEndpoint endpoint) throws Exception {
        endpoint.createConsumer(new PerIssueProcessor());
    }

    private static final class PerIssueProcessor implements Processor {

        @Override
        public void process(Exchange exchange) throws Exception {
            Log.info(exchange.getMessage(Issue.class).getId());
        }
    }
}
