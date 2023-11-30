package org.jboss.set.jql;

import io.quarkus.test.junit.QuarkusTest;
import org.jboss.jql.JqlBuilder;
import org.jboss.query.IssueStatus;
import org.jboss.query.SearchQuery;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
    void ProjectAndComponentQueryTest() {
        searchQuery = SearchQuery.builder()
                .projects("WFLY", "RESTEASY", "WFCORE")
                .components("Documentation")
                .build();
        Assertions.assertEquals("component IN ('Documentation') AND project IN ('WFLY', 'RESTEASY', 'WFCORE')",
                JqlBuilder.build(searchQuery));
    }

    @Test
    void assigneeAndProjectAndComponentQueryTest() {
        searchQuery = SearchQuery.builder()
                .assignee("Tadpole")
                .projects("WFLY", "RESTEASY", "WFCORE")
                .components("Documentation")
                .build();
        Assertions.assertEquals(
                "assignee = 'Tadpole' AND component IN ('Documentation') AND project IN ('WFLY', 'RESTEASY', 'WFCORE')",
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
}
