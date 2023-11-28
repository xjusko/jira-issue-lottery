/**
 * This file is inspired by https://github.com/jboss-set/aphrodite
 */
package org.jboss.query;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

public class SearchQuery {
    private final IssueStatus status;
    private final String assignee;
    private final String reporter;
    private final String product;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Integer maxResults;
    private final Set<String> labels;

    private SearchQuery(IssueStatus status, String assignee, String reporter, String product, LocalDate startDate,
            LocalDate endDate, Integer maxResults, Set<String> labels) {
        this.status = status;
        this.assignee = assignee;
        this.reporter = reporter;
        this.product = product;
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxResults = maxResults;
        this.labels = labels;

        if (startDate != null && startDate.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("startDate cannot be in the future.");

        if (endDate != null && endDate.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("endDate cannot be in the future.");
    }

    public Optional<IssueStatus> getStatus() {
        return Optional.ofNullable(status);
    }

    public Optional<String> getAssignee() {
        return Optional.ofNullable(assignee);
    }

    public Optional<String> getReporter() {
        return Optional.ofNullable(reporter);
    }

    public Optional<String> getProduct() {
        return Optional.ofNullable(product);
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

    public java.util.stream.Stream<String> getLabels() {
        return labels == null ? java.util.stream.Stream.empty() : labels.stream();
    }

    public boolean isEmpty() {
        return status == null && assignee == null && reporter == null && product == null && startDate == null && endDate == null
                && maxResults == null;
    }

    public static class Builder {

        private IssueStatus status;
        private String assignee;
        private String reporter;
        private String product;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer maxResults;
        private Set<String> labels;

        public Builder setStatus(IssueStatus status) {
            this.status = status;
            return this;
        }

        public Builder setAssignee(String assignee) {
            this.assignee = assignee;
            return this;
        }

        public Builder setReporter(String reporter) {
            this.reporter = reporter;
            return this;
        }

        public Builder setProduct(String product) {
            this.product = product;
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

        public Builder setLabels(Set<String> labels) {
            this.labels = labels;
            return this;
        }

        public SearchQuery build() {
            return new SearchQuery(status, assignee, reporter, product, startDate, endDate, maxResults, labels);
        }
    }
}
