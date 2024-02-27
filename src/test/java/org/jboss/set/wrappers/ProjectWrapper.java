package org.jboss.set.wrappers;

import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.Project;

import java.util.Collection;

public class ProjectWrapper extends Project {
    public ProjectWrapper(String key, Collection<BasicComponent> components) {
        super(null, null, key, null, null, null, null, null, null, components, null, null);
    }
}
