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
}
