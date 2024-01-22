package org.jboss.set.jql;

import io.quarkus.test.junit.QuarkusTest;
import org.jboss.set.query.IssueStatus;
import org.jboss.set.query.SearchQuery;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

@QuarkusTest
public class JqlFromSearchQueryTest {

    private SearchQuery searchQuery;

    @Test
    void onlyAssigneeQueryTest() {
        searchQuery = SearchQuery.builder().assignee("Tadpole").build();
        Assertions.assertEquals("assignee = 'Tadpole'", JqlBuilder.build(searchQuery));
    }

    @Test
    void onlyProjectQueryTest() {
        searchQuery = SearchQuery.builder().projects("WFLY", "RESTEASY", "WFCORE").build();
        Assertions.assertEquals("project IN ('WFLY', 'RESTEASY', 'WFCORE')", JqlBuilder.build(searchQuery));
    }

    @Test
    void assigneeAndProjectQueryTest() {
        searchQuery = SearchQuery.builder()
                .assignee("Tadpole")
                .projects("WFLY", "RESTEASY", "WFCORE")
                .build();
        Assertions.assertEquals("assignee = 'Tadpole' AND project IN ('WFLY', 'RESTEASY', 'WFCORE')",
                JqlBuilder.build(searchQuery));
    }

    @Test
    void projectsAndComponentQueryTest() {
        searchQuery = SearchQuery.builder()
                .projects("WFLY", "RESTEASY", "WFCORE")
                .components("Documentation")
                .build();
        Assertions.assertEquals("component = 'Documentation' AND project IN ('WFLY', 'RESTEASY', 'WFCORE')",
                JqlBuilder.build(searchQuery));
    }

    @Test
    void assigneeAndProjectsAndComponentQueryTest() {
        searchQuery = SearchQuery.builder()
                .assignee("Tadpole")
                .projects("WFLY", "RESTEASY", "WFCORE")
                .components("Documentation", "Logging")
                .build();
        Assertions.assertEquals(
                "assignee = 'Tadpole' AND component IN ('Documentation', 'Logging') AND project IN ('WFLY', 'RESTEASY', 'WFCORE')",
                JqlBuilder.build(searchQuery));
    }

    @Test
    void assigneeAndProjectsAndComponentsQueryTest() {
        searchQuery = SearchQuery.builder()
                .assignee("Tadpole")
                .projects("WFLY", "RESTEASY", "WFCORE")
                .components("Documentation")
                .build();
        Assertions.assertEquals(
                "assignee = 'Tadpole' AND component = 'Documentation' AND project IN ('WFLY', 'RESTEASY', 'WFCORE')",
                JqlBuilder.build(searchQuery));
    }

    @Test
    void notEmptyAssigneeAndMultipleStatusesQueryTest() {
        searchQuery = SearchQuery.builder()
                .assigneeNotEmpty()
                .status(IssueStatus.CREATED, IssueStatus.ON_QA, IssueStatus.POST)
                .build();
        Assertions.assertEquals("status IN ('new', 'ready for qa', 'pull request sent') AND assignee IS NOT EMPTY",
                JqlBuilder.build(searchQuery));
    }

    @Test
    void assigneeEmptyQueryTest() {
        searchQuery = SearchQuery.builder().assigneeEmpty().build();
        Assertions.assertEquals("assignee IS EMPTY", JqlBuilder.build(searchQuery));
    }

    @Test
    void beforeClauseQueryTest() {
        LocalDate before = LocalDate.now().minusDays(3);
        searchQuery = SearchQuery.builder()
                .before(before)
                .build();
        Assertions.assertEquals("updated <= '%s'".formatted(before.toString()), JqlBuilder.build(searchQuery));
    }
}
