package org.jboss.set.processing.state;

import java.util.ArrayList;
import java.util.List;

public class EveryIssueState {
    private final List<SingleIssueState> issueStates;

    public EveryIssueState(List<SingleIssueState> issueStates) {
        this.issueStates = new ArrayList<>(issueStates);
    }

    public List<SingleIssueState> getIssueStates() {
        return issueStates;
    }
}
