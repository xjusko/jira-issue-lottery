package org.jboss.set.jql;

import io.quarkus.test.junit.QuarkusTest;
import org.jboss.jql.JqlBuilder;
import org.jboss.query.SearchQuery;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class JqlFromSearchQueryTest {

    private SearchQuery searchQuery;

    @Test
    void onlyAssigneeQueryTest() {
        searchQuery = SearchQuery.builder().setAssignee("Tadpole").build();
        Assertions.assertEquals("assignee = 'Tadpole'", JqlBuilder.build(searchQuery));
    }

    @Test
    void onlyProjectQueryTest() {
        searchQuery = SearchQuery.builder().setProjects("WFLY", "RESTEASY", "WFCORE").build();
        Assertions.assertEquals("project IN (WFLY, RESTEASY, WFCORE)", JqlBuilder.build(searchQuery));
    }

    @Test
    void assigneeAndProjectQueryTest() {
        searchQuery = SearchQuery.builder()
                .setAssignee("Tadpole")
                .setProjects("WFLY", "RESTEASY", "WFCORE")
                .build();
        Assertions.assertEquals("assignee = 'Tadpole' AND project IN (WFLY, RESTEASY, WFCORE)", JqlBuilder.build(searchQuery));
    }

    @Test
    void ProjectAndComponentQueryTest() {
        searchQuery = SearchQuery.builder()
                .setProjects("WFLY", "RESTEASY", "WFCORE")
                .setComponents("Documentation")
                .build();
        Assertions.assertEquals("component IN (Documentation) AND project IN (WFLY, RESTEASY, WFCORE)", JqlBuilder.build(searchQuery));
    }

    @Test
    void assigneeAndProjectAndComponentQueryTest() {
        searchQuery = SearchQuery.builder()
                .setAssignee("Tadpole")
                .setProjects("WFLY", "RESTEASY", "WFCORE")
                .setComponents("Documentation")
                .build();
        Assertions.assertEquals("assignee = 'Tadpole' AND component IN (Documentation) AND project IN (WFLY, RESTEASY, WFCORE)", JqlBuilder.build(searchQuery));
    }
}
