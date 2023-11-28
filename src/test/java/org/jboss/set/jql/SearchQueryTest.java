package org.jboss.set.jql;

import io.quarkus.test.junit.QuarkusTest;
import org.jboss.query.IssueStatus;
import org.jboss.query.SearchQuery;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

@QuarkusTest
public class SearchQueryTest {

    private enum QueryParameter {
        getStatus,
        getAssignee,
        getProjects,
        getComponents,
        getStartDate,
        getEndDate,
        getMaxResults,
        getLabels
    }

    @Test
    void emptySearchQueryTest() {
        testEmptyGetters(SearchQuery.builder().build());
    }

    @Test
    void createSimpleSearchQueryWithoutCollectionsTest() {
        testEmptyGetters(SearchQuery.builder().setAssignee("Tadpole").setStatus(IssueStatus.NEW).setMaxResults(20).build(),
                QueryParameter.getAssignee, QueryParameter.getStatus, QueryParameter.getMaxResults);
    }

    @Test
    void createSearchQueryWithCollectionsTest() {
        SearchQuery searchQuery = SearchQuery.builder().setProjects("WFLY", "RESTEASY", "JBEAP")
                .setComponents("Localization", "Documentation").build();
        testEmptyGetters(searchQuery, QueryParameter.getProjects, QueryParameter.getComponents);
        searchQuery.getProjects().ifPresent(projects -> Assertions.assertEquals(3, projects.size()));
        searchQuery.getComponents().ifPresent(components -> Assertions.assertEquals(2, components.size()));
    }

    private void testEmptyGetters(SearchQuery query, QueryParameter... notEmpty) {
        List<QueryParameter> nonEmptyGetters = Arrays.asList(notEmpty);
        for (QueryParameter getter : QueryParameter.values()) {
            if (nonEmptyGetters.contains(getter)) {
                switch (getter) {
                    case getStatus -> Assertions.assertTrue(query.getStatus().isPresent());
                    case getAssignee -> Assertions.assertTrue(query.getAssignee().isPresent());
                    case getProjects -> Assertions.assertTrue(query.getProjects().isPresent());
                    case getComponents -> Assertions.assertTrue(query.getComponents().isPresent());
                    case getStartDate -> Assertions.assertTrue(query.getStartDate().isPresent());
                    case getEndDate -> Assertions.assertTrue(query.getEndDate().isPresent());
                    case getMaxResults -> Assertions.assertTrue(query.getMaxResults().isPresent());
                    case getLabels -> Assertions.assertTrue(query.getLabels().isPresent());
                }
            } else {
                switch (getter) {
                    case getStatus -> Assertions.assertTrue(query.getStatus().isEmpty());
                    case getAssignee -> Assertions.assertTrue(query.getAssignee().isEmpty());
                    case getProjects -> Assertions.assertTrue(query.getProjects().isEmpty());
                    case getComponents -> Assertions.assertTrue(query.getComponents().isEmpty());
                    case getStartDate -> Assertions.assertTrue(query.getStartDate().isEmpty());
                    case getEndDate -> Assertions.assertTrue(query.getEndDate().isEmpty());
                    case getMaxResults -> Assertions.assertTrue(query.getMaxResults().isEmpty());
                    case getLabels -> Assertions.assertTrue(query.getLabels().isEmpty());
                }
            }
        }
    }
}
