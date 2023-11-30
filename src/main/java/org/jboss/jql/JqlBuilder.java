package org.jboss.jql;

import org.jboss.query.SearchQuery;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class JqlBuilder {

    private static final String ASSIGNEE = "assignee";
    private static final String STATUS = "status";
    private static final String COMPONENT = "component";
    private static final String PROJECT = "project";
    private static final String UPDATED = "updated";
    private static final String LABEL = "label";

    public static String build(SearchQuery searchQuery) {
        List<String> predicates = new ArrayList<>();
        searchQuery.getStatuses().filter(issueStatuses -> !issueStatuses.isEmpty())
                .ifPresent(statuses -> predicates.add(Predicate.IN.apply(STATUS, statuses)));
        searchQuery.getAssignee().ifPresent(assignee -> {
            if (searchQuery.isAssigneeNotEmpty()) {
                predicates.add(Predicate.NON_EMPTY.apply(ASSIGNEE));
            } else if (searchQuery.isAssigneeEmpty()) {
                predicates.add(Predicate.EMPTY.apply(ASSIGNEE));
            } else {
                predicates.add(Predicate.EQUAL.apply(ASSIGNEE, assignee));
            }
        });
        searchQuery.getComponents().filter(components -> !components.isEmpty())
                .ifPresent(components -> predicates.add(Predicate.IN.apply(COMPONENT, components)));
        searchQuery.getProjects().filter(projects -> !projects.isEmpty())
                .ifPresent(projects -> predicates.add(Predicate.IN.apply(PROJECT, projects)));
        searchQuery.getStartDate().ifPresent(date -> {
            String formattedDate = date.atStartOfDay().format((DateTimeFormatter.ISO_LOCAL_DATE));
            predicates.add(Predicate.GE_THAN.apply(UPDATED, formattedDate));
        });

        searchQuery.getLabels().filter(labels -> !labels.isEmpty())
                .ifPresent(labels -> predicates.add(Predicate.IN.apply(LABEL, labels)));
        return String.join(Predicate.AND.toString(), predicates).strip();
    }
}
