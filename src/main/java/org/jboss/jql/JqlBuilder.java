package org.jboss.jql;

import org.jboss.query.Predicate;
import org.jboss.query.SearchQuery;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class JqlBuilder {

    public static String build(SearchQuery searchQuery) {
        List<String> predicates = new ArrayList<>();
        searchQuery.getStatuses().ifPresent(status -> predicates.add(Predicate.EQUAL.apply("status", status.toString())));
        searchQuery.getAssignee().ifPresent(assignee -> {
            if (searchQuery.isAssigneeEmpty()) {
                predicates.add(Predicate.EMPTY.apply("assignee"));
            } else {
                predicates.add(Predicate.EQUAL.apply("assignee", assignee));
            }
        });
        searchQuery.getComponents().flatMap(JqlBuilder::createQuerySet)
                .ifPresent(set -> predicates.add(Predicate.IN.apply("component", set)));
        searchQuery.getProjects().flatMap(JqlBuilder::createQuerySet)
                .ifPresent(set -> predicates.add(Predicate.IN.apply("project", set)));
        searchQuery.getStartDate().ifPresent(date -> {
            String formattedDate = date.atStartOfDay().format((DateTimeFormatter.ISO_LOCAL_DATE));
            predicates.add(Predicate.GE_THAN.apply("updated", formattedDate));
        });

        searchQuery.getLabels().flatMap(JqlBuilder::createQuerySet)
                .ifPresent(set -> predicates.add(Predicate.IN.apply("label", set)));
        return String.join(Predicate.AND.toString(), predicates).strip();
    }

    private static Optional<String> createQuerySet(Collection<String> set) {
        if (set.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of("(%s)".formatted(String.join(", ", set)));
    }
}
