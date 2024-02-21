/**
 * This file is inspired by Quarkus GitHub lottery - https://github.com/quarkusio/quarkus-github-lottery/blob/b0da185b00151eb0c2f2d0f19e91e76b7aedf020/src/main/java/io/quarkus/github/lottery/config/LotteryConfig.java
 */
package org.jboss.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

// Used for deserializing the config file
@RegisterForReflection(ignoreNested = false)
public record LotteryConfig(
        @JsonProperty(required = true) Duration delay,
        List<Participant> participants) {

    public static final String FILE_NAME = "jira-issue-lottery.yml";

    public record Participant(
            @JsonProperty(required = true) String email,
            Set<Project> projects) {

        public record Project(
                @JsonProperty(required = true) String project,
                @JsonDeserialize(as = TreeSet.class) Set<String> components,
                @JsonUnwrapped @JsonProperty(access = JsonProperty.Access.READ_ONLY) Participation participation) {
            // https://stackoverflow.com/a/71539100/6692043
            @JsonCreator
            public Project(@JsonProperty(required = true) String project, Set<String> components,
                    @JsonProperty(required = true) int maxIssues) {
                this(project, components, new Participation(maxIssues));
            }
        }

        public record Participation(@JsonProperty(required = true) int maxIssues) {
        }
    }

}
