package org.jboss.processing;

import com.atlassian.jira.rest.client.api.domain.Issue;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.jira.JiraEndpoint;
import org.apache.camel.component.jira.consumer.NewIssuesConsumer;
import org.jboss.processing.state.EveryIssueState;
import org.jboss.processing.state.SingleIssueState;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EveryIssueProcessor extends NewIssuesConsumer implements Executable<EveryIssueState> {

    private static final State state = new State();

    private EveryIssueProcessor(JiraEndpoint jiraEndpoint) {
        super(jiraEndpoint, new Processor() {
            @Override
            public void process(Exchange exchange) {
                Issue issue = exchange.getIn().getBody(Issue.class);
                if (issue != null) {
                    state.issueStates.add(new SingleIssueState(issue));
                }
            }
        });
    }

    private static final class State {
        List<SingleIssueState> issueStates = new CopyOnWriteArrayList<>();
    }

    public static EveryIssueProcessor getInstance(JiraEndpoint jiraEndpoint) {
        jiraEndpoint.setJql(
                "project in (JBEAP, WFLY, WFCORE, RESTEASY) AND component not in (Documentation, Localization) AND assignee = rhn-support-rbudinsk");
        return new EveryIssueProcessor(jiraEndpoint);
    }

    @Override
    public EveryIssueState execute() throws Exception {
        doPoll();
        return new EveryIssueState(state.issueStates);
    }
}
