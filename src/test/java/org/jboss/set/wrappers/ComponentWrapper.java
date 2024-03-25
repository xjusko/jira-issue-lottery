package org.jboss.set.wrappers;

import com.atlassian.jira.rest.client.api.domain.BasicComponent;

public class ComponentWrapper extends BasicComponent {
    public ComponentWrapper(String name) {
        super(null, null, name, null);
    }
}
