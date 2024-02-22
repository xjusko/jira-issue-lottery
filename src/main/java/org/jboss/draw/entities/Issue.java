package org.jboss.draw.entities;

import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import io.quarkus.logging.Log;
import org.jboss.processing.state.SingleIssueState;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.StreamSupport;

public class Issue {
    private static final String BROWSE_URI = "https://issues.redhat.com/browse/";

    private final Long id;
    private final URI uri;
    private final String project;
    private final List<String> components;
    private final String key;
    private Participant assignee;

    public Issue(SingleIssueState issue) {
        this(issue.getId(),
                issue.getSelf(),
                issue.getProject().getKey(),
                issue.getKey(),
                issue.getComponents().stream().map(BasicComponent::getName).toList());
    }

    public Issue(com.atlassian.jira.rest.client.api.domain.Issue issue) {
        this(issue.getId(),
                issue.getSelf(),
                issue.getProject().getKey(),
                issue.getKey(),
                StreamSupport.stream(issue.getComponents().spliterator(), false).map(BasicComponent::getName).toList());
    }

    public Issue(Long id, URI uri, String project, String key, List<String> components) {
        this.id = id;
        this.uri = uri;
        this.project = project;
        this.key = key;
        this.components = components;
    }

    public Long getId() {
        return id;
    }

    public URI getUri() {
        return uri;
    }

    public URI getBrowseUri() {
        try {
            return new URI(BROWSE_URI + key);
        } catch (URISyntaxException e) {
            Log.errorf(e, "Unable to construct browse URI for key %s", key);
            throw new RuntimeException(e);
        }
    }

    public String getProject() {
        return project;
    }

    public List<String> getComponents() {
        return components;
    }

    public Participant getAssignee() {
        return assignee;
    }

    /**
     * It's intentionally package private so only {@code Participant}
     * can invoke this method.
     */
    void setAssignee(Participant assignee) {
        this.assignee = assignee;
    }
}
