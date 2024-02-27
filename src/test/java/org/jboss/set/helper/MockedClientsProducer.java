package org.jboss.set.helper;

import com.atlassian.jira.rest.client.api.ProjectRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import jakarta.inject.Singleton;
import org.mockito.Mockito;

@Singleton
public class MockedClientsProducer {
    private final SearchRestClient searchRestClient = Mockito.mock(SearchRestClient.class);
    private final ProjectRestClient projectRestClient = Mockito.mock(ProjectRestClient.class);

    public SearchRestClient getSearchRestClient() {
        return searchRestClient;
    }

    public ProjectRestClient getProjectRestClient() {
        return projectRestClient;
    }
}
