package org.jboss.set.state;

import io.quarkus.test.junit.QuarkusTest;
import org.jboss.set.query.IssueStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@QuarkusTest
public class IssueStatusTest {

    /**
     * We check only for number of distinguished entries.
     * In the next text we check for identity of {@code org.jboss.set.query.IssueStatus}.
     * So if we have multiple same entries we would count them as a single one.
     */
    @Test
    void testGettingCorrectEnum() {
        List<String> statuses = List.of("new", "open", "coding in progress", "pull request sent",
                "resolved", "ready for qa", "verified", "closed");
        Set<IssueStatus> resolvedStatuses = statuses.stream().map(IssueStatus::getInstance).collect(Collectors.toSet());
        Assertions.assertEquals(new HashSet<>(statuses).size(), resolvedStatuses.size());
    }

    @Test
    void testGettingSpecialCasedEnum() {
        Assertions.assertNotEquals(IssueStatus.UNDEFINED, IssueStatus.getInstance("reopened"));
        Assertions.assertNotEquals(IssueStatus.UNDEFINED, IssueStatus.getInstance("qa in progress"));
    }

    @Test
    void testIdentityOfEnum() {
        Assertions.assertEquals(IssueStatus.getInstance("new"), IssueStatus.getInstance("new"));
        Assertions.assertEquals(IssueStatus.getInstance("random"), IssueStatus.getInstance("another random"));
        Assertions.assertEquals(IssueStatus.NEW, IssueStatus.getInstance("reopened"));
        Assertions.assertEquals(IssueStatus.ON_QA, IssueStatus.getInstance("qa in progress"));
    }

    @Test
    void testGettingUnidentifiedEnum() {
        Assertions.assertEquals(IssueStatus.UNDEFINED, IssueStatus.getInstance("random status"));
    }
}
