package org.jboss.config;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.util.Set;

@ApplicationScoped
public class LotteryConfigValidation {

    LotteryConfig config;

    public void validate(LotteryConfig config) throws IOException {
        this.config = config;
        validateParticipantProjects();
    }

    private void validateParticipantProjects() throws IOException {
        for (LotteryConfig.Participant participant : config.participants()) {
            for (LotteryConfig.Project project : participant.projects()) {
                if (!config.availableProjects().containsKey(project.project())) {
                    throw new IOException(
                            "Participant " + participant.email() + " has an invalid project: " + project.project());
                }

                Set<String> availableComponents = config.availableProjects().get(project.project());
                if (!availableComponents.containsAll(project.components())) {
                    throw new IOException("Participant " + participant.email() + " has invalid components for project "
                            + project.project() + ": " + project.components());
                }
            }
        }
    }
}
