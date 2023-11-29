/**
 * This file is inspired by https://github.com/jboss-set/aphrodite
 */
package org.jboss.query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchQuery {
    private static final String NOT_EMPTY = "NOT_EMPTY";
    private static final String EMPTY_ASSIGNEE = "UNDEFINED";

    private final List<IssueStatus> statuses;
    private final String assignee;
    private final List<String> projects;
    private final List<String> components;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Integer maxResults;
    private final Set<String> labels;

    private SearchQuery(List<IssueStatus> statuses, String assignee, List<String> projects, List<String> components,
            LocalDate startDate, LocalDate endDate, Integer maxResults, Set<String> labels) {
        this.statuses = statuses;
        this.assignee = assignee;
        this.projects = projects;
        this.components = components;
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxResults = maxResults;
        this.labels = labels;

        if (startDate != null && startDate.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("startDate cannot be in the future.");

        if (endDate != null && endDate.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("endDate cannot be in the future.");
    }

    public Optional<List<IssueStatus>> getStatuses() {
        return Optional.ofNullable(statuses);
    }

    public boolean isAssigneeNotEmpty() {
        return assignee != null && assignee.equals(NOT_EMPTY);
    }

    public boolean isAssigneeEmpty() {
        return assignee != null && assignee.equals(EMPTY_ASSIGNEE);
    }

    public Optional<String> getAssignee() {
        return Optional.ofNullable(assignee);
    }

    public Optional<List<String>> getProjects() {
        return Optional.ofNullable(projects);
    }

    public Optional<List<String>> getComponents() {
        return Optional.ofNullable(components);
    }

    public Optional<LocalDate> getStartDate() {
        return Optional.ofNullable(startDate);
    }

    public Optional<LocalDate> getEndDate() {
        return Optional.ofNullable(endDate);
    }

    public Optional<Integer> getMaxResults() {
        return Optional.ofNullable(maxResults);
    }

    public Optional<Set<String>> getLabels() {
        return Optional.ofNullable(labels);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private List<IssueStatus> statuses;
        private String assignee;
        private List<String> projects;
        private List<String> components;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer maxResults;
        private Set<String> labels;

        private Builder() {
        }

        public Builder setStatus(IssueStatus status, IssueStatus... statuses) {
            this.statuses = Stream.concat(Stream.of(status), Stream.of(statuses)).toList();
            return this;
        }

        public Builder setAssigneeNotEmpty() {
            this.assignee = NOT_EMPTY;
            return this;
        }

        public Builder setAssigneeEmpty() {
            this.assignee = EMPTY_ASSIGNEE;
            return this;
        }

        public Builder setAssignee(String assignee) {
            this.assignee = assignee;
            return this;
        }

        public Builder setProjects(String project, String... projects) {
            this.projects = Stream.concat(Stream.of(project), Stream.of(projects)).toList();
            return this;
        }

        public Builder setComponents(String component, String... components) {
            this.components = Stream.concat(Stream.of(component), Stream.of(components)).toList();
            return this;
        }

        public Builder setStartDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder setEndDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder setMaxResults(Integer maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        public Builder setLabels(String label, String... labels) {
            this.labels = Stream.concat(Stream.of(label), Stream.of(labels)).collect(Collectors.toSet());
            return this;
        }

        public SearchQuery build() {
            return new SearchQuery(statuses, assignee, projects, components, startDate, endDate, maxResults, labels);
        }
    }
}
