/**
 * This file is inspired by Quarkus GitHub lottery - https://github.com/quarkusio/quarkus-github-lottery/blob/b0da185b00151eb0c2f2d0f19e91e76b7aedf020/src/main/java/io/quarkus/github/lottery/config/LotteryConfig.java
 */
package org.jboss.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

// Used for deserializing the config file
@RegisterForReflection(ignoreNested = false)
public record LotteryConfig(
        @JsonProperty(required = true) Notifications notifications,
        @JsonProperty(required = true) Buckets buckets,
        List<Participant> participants) {

    public static final String FILE_NAME = "jira-issue-lottery.yml";

    public record Notifications(
            @JsonProperty(required = true) CreateIssuesConfig createIssues) {
        public record CreateIssuesConfig(
                @JsonProperty(required = true) String repository) {
        }
    }

    public record Buckets(
            @JsonProperty(required = true) Triage triage,
            @JsonProperty(required = true) Maintenance maintenance,
            @JsonProperty(required = true) Stewardship stewardship) {

        public record Triage(
                String label,
                @JsonUnwrapped @JsonProperty(access = JsonProperty.Access.READ_ONLY) Notification notification) {
            // https://stackoverflow.com/a/71539100/6692043
            // Also gives us a less verbose constructor for tests
            @JsonCreator
            public Triage(@JsonProperty(required = true) String label,
                    @JsonProperty(required = true) Duration delay, @JsonProperty(required = true) Duration timeout) {
                this(label, new Notification(delay, timeout));
            }
        }

        public record Maintenance(
                @JsonProperty(required = true) Feedback feedback,
                @JsonProperty(required = true) Stale stale) {

            public record Feedback(
                    @JsonProperty(required = true) List<String> labels,
                    @JsonProperty(required = true) Needed needed,
                    @JsonProperty(required = true) Provided provided) {

                public record Needed(
                        @JsonUnwrapped @JsonProperty(access = JsonProperty.Access.READ_ONLY) Notification notification) {
                    // https://stackoverflow.com/a/71539100/6692043
                    // Also gives us a less verbose constructor for tests
                    @JsonCreator
                    public Needed(@JsonProperty(required = true) Duration delay,
                            @JsonProperty(required = true) Duration timeout) {
                        this(new Notification(delay, timeout));
                    }
                }

                public record Provided(
                        @JsonUnwrapped @JsonProperty(access = JsonProperty.Access.READ_ONLY) Notification notification) {
                    // https://stackoverflow.com/a/71539100/6692043
                    // Also gives us a less verbose constructor for tests
                    @JsonCreator
                    public Provided(@JsonProperty(required = true) Duration delay,
                            @JsonProperty(required = true) Duration timeout) {
                        this(new Notification(delay, timeout));
                    }
                }
            }

            public record Stale(
                    @JsonUnwrapped @JsonProperty(access = JsonProperty.Access.READ_ONLY) Notification notification) {
                // https://stackoverflow.com/a/71539100/6692043
                // Also gives us a less verbose constructor for tests
                @JsonCreator
                public Stale(@JsonProperty(required = true) Duration delay, @JsonProperty(required = true) Duration timeout) {
                    this(new Notification(delay, timeout));
                }
            }
        }

        public record Stewardship(
                @JsonUnwrapped @JsonProperty(access = JsonProperty.Access.READ_ONLY) Notification notification) {
            // https://stackoverflow.com/a/71539100/6692043
            // Also gives us a less verbose constructor for tests
            @JsonCreator
            public Stewardship(@JsonProperty(required = true) Duration delay, @JsonProperty(required = true) Duration timeout) {
                this(new Notification(delay, timeout));
            }
        }

        public record Notification(
                @JsonProperty(required = true) Duration delay,
                @JsonProperty(required = true) Duration timeout) {
        }
    }

    public record Participant(
            @JsonProperty(required = true) String username,
            Optional<ZoneId> timezone,
            Optional<Triage> triage,
            Optional<Maintenance> maintenance,
            Optional<Stewardship> stewardship) {

        public record Triage(
                @JsonDeserialize(as = TreeSet.class) Set<DayOfWeek> days,
                @JsonUnwrapped @JsonProperty(access = JsonProperty.Access.READ_ONLY) Participation participation) {
            // https://stackoverflow.com/a/71539100/6692043
            @JsonCreator
            public Triage(@JsonProperty(required = true) Set<DayOfWeek> days, @JsonProperty(required = true) int maxIssues) {
                this(days, new Participation(maxIssues));
            }
        }

        public record Maintenance(
                // TODO default to all labels configured for this user in .github/quarkus-bot.yml
                @JsonProperty(required = true) List<String> labels,
                @JsonProperty(required = true) @JsonDeserialize(as = TreeSet.class) Set<DayOfWeek> days,
                Feedback feedback,
                @JsonProperty(required = true) Participation stale) {
            public record Feedback(
                    @JsonProperty(required = true) Participation needed,
                    @JsonProperty(required = true) Participation provided) {
            }
        }

        public record Stewardship(
                @JsonDeserialize(as = TreeSet.class) Set<DayOfWeek> days,
                @JsonUnwrapped @JsonProperty(access = JsonProperty.Access.READ_ONLY) Participation participation) {
            // https://stackoverflow.com/a/71539100/6692043
            @JsonCreator
            public Stewardship(@JsonProperty(required = true) Set<DayOfWeek> days,
                    @JsonProperty(required = true) int maxIssues) {
                this(days, new Participation(maxIssues));
            }
        }

        public record Participation(@JsonProperty(required = true) int maxIssues) {
        }
    }

}
