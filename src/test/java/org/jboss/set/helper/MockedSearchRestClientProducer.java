package org.jboss.set.helper;

import com.atlassian.jira.rest.client.api.SearchRestClient;
import jakarta.inject.Singleton;
import org.mockito.Mockito;

@Singleton
public class MockedSearchRestClientProducer {
    private final SearchRestClient searchRestClient = Mockito.mock(SearchRestClient.class);

    public SearchRestClient getSearchRestClient() {
        return searchRestClient;
    }
}
