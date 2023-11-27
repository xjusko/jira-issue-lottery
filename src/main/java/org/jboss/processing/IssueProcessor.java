package org.jboss.processing;

import com.atlassian.jira.rest.client.api.domain.Comment;
import io.quarkus.logging.Log;
import org.apache.camel.Exchange;
import org.apache.camel.component.jira.JiraConstants;
import org.apache.camel.component.jira.JiraEndpoint;
import org.apache.camel.component.jira.producer.FetchCommentsProducer;

import java.util.List;
import java.util.stream.Collectors;

public class IssueProcessor extends FetchCommentsProducer {

    private final String jiraIssue;

    public IssueProcessor(JiraEndpoint jiraEndpoint, String jiraIssue) {
        super(jiraEndpoint);
        this.jiraIssue = jiraIssue;
    }

    @Override
    public void process(Exchange exchange) {
        exchange.getIn().setHeader(JiraConstants.ISSUE_KEY, jiraIssue);

        super.process(exchange);

        //noinspection unchecked
        List<Comment> issues = (List<Comment>) exchange.getIn().getBody(List.class);
        String comments = issues.stream().map(Comment::getBody)
                .collect(Collectors.joining("\n\n"));
        Log.info(comments);
    }
}
