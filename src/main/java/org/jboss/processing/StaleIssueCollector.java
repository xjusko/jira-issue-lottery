package org.jboss.processing;

import com.atlassian.jira.rest.client.api.domain.Issue;
import io.quarkus.logging.Log;
import org.apache.camel.component.jira.JiraEndpoint;
import org.apache.camel.component.jira.consumer.NewIssuesConsumer;
import org.jboss.processing.state.SingleIssueState;

import java.util.ArrayList;
import java.util.List;

public class StaleIssueCollector extends NewIssuesConsumer implements Executable {

    private static final State state = new State();

    StaleIssueCollector(JiraEndpoint jiraEndpoint) {
        super(jiraEndpoint, exchange -> {
            Issue issue = exchange.getIn().getBody(Issue.class);
            if (issue != null) {
                state.issueStates.add(new SingleIssueState(issue));
            }
        });
        state.issueStates.clear();
    }

    private static final class State {
        List<SingleIssueState> issueStates = new ArrayList<>();
    }

    @Override
    public void execute() throws Exception {
        int issuesCount = doPoll();
        Log.infof("Found number of issues %d", issuesCount);
    }
}
