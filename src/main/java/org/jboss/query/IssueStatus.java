package org.jboss.query;

import java.util.HashMap;
import java.util.Map;

public enum IssueStatus {
    CREATED("new"),
    UNDEFINED("UNDEFINED"),
    NEW("open"),
    ASSIGNED("coding in progress"),
    POST("pull request sent"),
    MODIFIED("resolved"),
    ON_QA("ready for qa"),
    VERIFIED("verified"),
    CLOSED("closed");

    private final String description;

    private static Map<String, IssueStatus> issueStatusLut = null;

    private IssueStatus(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }

    public static IssueStatus getInstance(String description) {
        if (issueStatusLut == null) {
            issueStatusLut = new HashMap<>();
            for (IssueStatus issueStatus : IssueStatus.values()) {
                issueStatusLut.put(issueStatus.toString(), issueStatus);
            }
            // Special duplicate cases
            issueStatusLut.put("reopened", NEW);
            issueStatusLut.put("qa in progress", ON_QA);
        }

        return issueStatusLut.getOrDefault(description, UNDEFINED);
    }
}
