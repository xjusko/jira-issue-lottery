package org.jboss.processing;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.camel.component.jira.JiraEndpoint;
import org.jboss.config.LotteryConfig;
import org.jboss.config.LotteryConfigProducer;
import org.jboss.jql.JqlBuilder;
import org.jboss.query.IssueStatus;
import org.jboss.query.SearchQuery;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class CollectorProducer {

    @Inject
    private LotteryConfigProducer lotteryConfigProducer;

    public NewIssueCollector newIssueCollectorInstance(JiraEndpoint jiraEndpoint) {
        LotteryConfig lotteryConfig = lotteryConfigProducer.getLotteryConfig();
        Set<String> availableProjects = lotteryConfig.participants().stream()
                .map(LotteryConfig.Participant::projects)
                .map(projects -> projects.stream().map(LotteryConfig.Participant.Project::project).collect(Collectors.toSet()))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        SearchQuery searchQuery = SearchQuery.builder().projects(availableProjects).status(IssueStatus.NEW)
                .assigneeEmpty()
                .build();
        jiraEndpoint.setJql(JqlBuilder.build(searchQuery));
        return new NewIssueCollector(jiraEndpoint);
    }

    public StaleIssueCollector newStaleIssueCollector(JiraEndpoint jiraEndpoint) {
        LotteryConfig lotteryConfig = lotteryConfigProducer.getLotteryConfig();
        SearchQuery searchQuery = SearchQuery.builder().projects("WFLY")
                .before(LocalDate.now().minusDays(lotteryConfig.delay().toDays())).assigneeNotEmpty()
                .status(IssueStatus.CREATED, IssueStatus.ASSIGNED, IssueStatus.POST).build();
        jiraEndpoint.setJql(JqlBuilder.build(searchQuery));
        return new StaleIssueCollector(jiraEndpoint);
    }
}
