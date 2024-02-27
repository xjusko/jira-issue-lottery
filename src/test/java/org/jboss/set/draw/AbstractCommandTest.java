/**
 * @brief This should be a base class for testing and mocking issues retrieval from camel extension.
 *  You should implement {@code setupIssues} method, which expects issues used across all
 *  tests.
 *  Furthermore, then you can access List of {@code org.jboss.draw.entities.Issue}, with
 *  the same issues, in variable {@code ourIssues}. This is useful for methods such as
 *  {@link org.jboss.set.draw.Lottery#createEmailText(java.lang.String, java.util.List)}.
 */
package org.jboss.set.draw;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.ProjectRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Project;
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
import org.jboss.set.JiraEndpointProducer;
import org.jboss.set.helper.MockedClientsProducer;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.List;

import static org.apache.camel.component.jira.JiraConstants.JIRA_REST_CLIENT_FACTORY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public abstract class AbstractCommandTest extends CamelQuarkusTestSupport {

    @Inject
    protected MockMailbox mailbox;

    @InjectMock
    private JiraEndpointProducer jiraEndpointProducer;

    @Inject
    protected ObjectMapper objectMapper;

    @Inject
    MockedClientsProducer mockedClientsProducer;

    private final JiraRestClient jiraRestClient = Mockito.mock(JiraRestClient.class);
    private final JiraRestClientFactory jiraRestClientFactory = Mockito.mock(JiraRestClientFactory.class);
    private final JiraEndpoint jiraEndpoint = Mockito.mock(JiraEndpoint.class);

    @Inject
    CamelContext camelContext;

    protected List<Issue> issues;
    protected List<org.jboss.set.draw.entities.Issue> ourIssues;

    protected List<Issue> setupIssues() {
        return Collections.emptyList();
    }

    protected List<Project> setupProjects() {
        return Collections.emptyList();
    }

    @BeforeEach
    protected void doBeforeEach() {
        mailbox.clear();
    }

    @Override
    protected void bindToRegistry(Registry registry) {
        registry.bind(JIRA_REST_CLIENT_FACTORY, jiraRestClientFactory);
    }

    public void setupMocks() {
        when(jiraRestClientFactory.create(any(), (AuthenticationHandler) any())).thenReturn(jiraRestClient);
        when(jiraRestClientFactory.create(any(), (HttpClient) any())).thenReturn(jiraRestClient);

        when(jiraEndpointProducer.getEndpoint()).thenReturn(jiraEndpoint);

        when(jiraEndpoint.getClient()).thenReturn(jiraRestClient);
        when(jiraEndpoint.getCamelContext()).thenReturn(camelContext);

        when(jiraRestClient.getSearchClient()).thenReturn(mockedClientsProducer.getSearchRestClient());
        when(jiraRestClient.getProjectClient()).thenReturn(mockedClientsProducer.getProjectRestClient());
    }

    public void setIssueMocks() {
        SearchRestClient searchRestClient = mockedClientsProducer.getSearchRestClient();

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

    public void setProjectMocks(List<Project> projects) {
        ProjectRestClient projectRestClient = mockedClientsProducer.getProjectRestClient();

        reset(projectRestClient);
        when(projectRestClient.getProject(anyString())).thenAnswer(invocationOnMock -> {
            String projectArg = invocationOnMock.getArgument(0);
            for (Project project : projects) {
                if (projectArg.equals(project.getKey())) {
                    return Promises.promise(project);
                }
            }
            throw new RestClientException(new RuntimeException("Missing project on mocked JIRA"));
        });
    }

    @Override
    protected CamelContext createCamelContext() throws Exception {
        setupMocks();

        issues = setupIssues();
        ourIssues = issues.stream().map(org.jboss.set.draw.entities.Issue::new).toList();
        setIssueMocks();

        setProjectMocks(setupProjects());

        return camelContext;
    }
}
