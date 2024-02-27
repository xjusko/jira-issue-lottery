/**
 * @brief This should be a base class for testing and mocking issues retrieval from camel extension.
 *  You should implement {@code setupIssues} method, which expects issues used across all
 *  tests.
 *  Furthermore, then you can access List of {@code org.jboss.draw.entities.Issue}, with
 *  the same issues, in variable {@code ourIssues}. This is useful for methods such as
 *  {@link org.jboss.draw.Lottery#createEmailText(java.lang.String, java.util.List)}.
 */
package org.jboss.set.draw;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.atlassian.util.concurrent.Promise;
import io.atlassian.util.concurrent.Promises;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.apache.camel.CamelContext;
import org.apache.camel.component.jira.JiraEndpoint;
import org.apache.camel.quarkus.test.CamelQuarkusTestSupport;
import org.apache.camel.spi.Registry;
import org.jboss.JiraEndpointProducer;
import org.jboss.JiraIssueLotteryCommand;
import org.jboss.config.LotteryConfigProducer;
import org.jboss.set.helper.MockedSearchRestClientProducer;
import org.jboss.testing.JiraCommand;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.List;

import static org.apache.camel.component.jira.JiraConstants.JIRA_REST_CLIENT_FACTORY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public abstract class AbstractLotteryTest extends CamelQuarkusTestSupport {
    @Inject
    @JiraCommand
    JiraIssueLotteryCommand app;

    @Inject
    MockMailbox mailbox;

    @InjectMock
    private JiraEndpointProducer jiraEndpointProducer;

    @Inject
    ObjectMapper objectMapper;

    @InjectMock
    LotteryConfigProducer lotteryConfigProducer;

    @Inject
    MockedSearchRestClientProducer mockedSearchRestClientProducer;

    private final JiraRestClient jiraRestClient = Mockito.mock(JiraRestClient.class);
    private final JiraRestClientFactory jiraRestClientFactory = Mockito.mock(JiraRestClientFactory.class);
    private final JiraEndpoint jiraEndpoint = Mockito.mock(JiraEndpoint.class);

    @Inject
    CamelContext camelContext;

    protected List<Issue> issues;
    protected List<org.jboss.draw.entities.Issue> ourIssues;

    protected abstract List<Issue> setupIssues();

    @BeforeEach
    protected void doBeforeEach() {
        reset(lotteryConfigProducer);
        mailbox.clear();
    }

    @Override
    protected void bindToRegistry(Registry registry) {
        registry.bind(JIRA_REST_CLIENT_FACTORY, jiraRestClientFactory);
    }

    public void setMocks() {
        SearchRestClient searchRestClient = mockedSearchRestClientProducer.getSearchRestClient();

        when(jiraRestClientFactory.create(any(), (AuthenticationHandler) any())).thenReturn(jiraRestClient);
        when(jiraRestClientFactory.create(any(), (HttpClient) any())).thenReturn(jiraRestClient);

        when(jiraEndpointProducer.getEndpoint(any(), any(), any())).thenReturn(jiraEndpoint);

        when(jiraEndpoint.getClient()).thenReturn(jiraRestClient);
        when(jiraEndpoint.getCamelContext()).thenReturn(camelContext);
        when(jiraRestClient.getSearchClient()).thenReturn(searchRestClient);

        when(searchRestClient.searchJql(any(), any(), any(), any())).thenAnswer(new Answer<Promise<SearchResult>>() {
            boolean visited = false;

            @Override
            public Promise<SearchResult> answer(InvocationOnMock invocationOnMock) throws Throwable {
                if (!visited) {
                    visited = true;
                    return Promises.promise(new SearchResult(0, 50, issues.size(), issues));
                }
                return Promises.promise(new SearchResult(0, 50, 0, Collections.emptyList()));
            }
        });
    }

    @Override
    protected CamelContext createCamelContext() throws Exception {
        issues = setupIssues();
        ourIssues = issues.stream().map(org.jboss.draw.entities.Issue::new).toList();
        setMocks();
        return camelContext;
    }
}
