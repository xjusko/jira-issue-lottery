package org.jboss.jql;

import org.jboss.query.SearchQuery;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class JqlBuilder {

    private static final String ASSIGNEE = "assignee";
    private static final String STATUS = "status";
    private static final String COMPONENT = "component";
    private static final String PROJECT = "project";
    private static final String UPDATED = "updated";
    private static final String LABEL = "label";

    public static String build(SearchQuery searchQuery) {
        List<String> predicates = new ArrayList<>();
        searchQuery.getStatuses().flatMap(JqlBuilder::createQuerySet)
                .ifPresent(set -> predicates.add(Predicate.IN.apply(STATUS, set)));
        searchQuery.getAssignee().ifPresent(assignee -> {
            if (searchQuery.isAssigneeNotEmpty()) {
                predicates.add(Predicate.NON_EMPTY.apply(ASSIGNEE));
            } else if (searchQuery.isAssigneeEmpty()) {
                predicates.add(Predicate.EMPTY.apply(ASSIGNEE));
            } else {
                predicates.add(Predicate.EQUAL.apply(ASSIGNEE, assignee));
            }
        });
        searchQuery.getComponents().flatMap(JqlBuilder::createQuerySet)
                .ifPresent(set -> predicates.add(Predicate.IN.apply(COMPONENT, set)));
        searchQuery.getProjects().flatMap(JqlBuilder::createQuerySet)
                .ifPresent(set -> predicates.add(Predicate.IN.apply(PROJECT, set)));
        searchQuery.getStartDate().ifPresent(date -> {
            String formattedDate = date.atStartOfDay().format((DateTimeFormatter.ISO_LOCAL_DATE));
            predicates.add(Predicate.GE_THAN.apply(UPDATED, formattedDate));
        });

        searchQuery.getLabels().flatMap(JqlBuilder::createQuerySet)
                .ifPresent(set -> predicates.add(Predicate.IN.apply(LABEL, set)));
        return String.join(Predicate.AND.toString(), predicates).strip();
    }

    private static Optional<String> createQuerySet(Collection<?> set) {
        if (set.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of("(%s)".formatted(String.join(", ", set.stream().map(JqlBuilder::wrapLiteral).toList())));
    }

    public static String wrapLiteral(Object o) {
        return "'%s'".formatted(o);
    }
}
