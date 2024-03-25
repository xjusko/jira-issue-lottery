package org.jboss.set;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.Project;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.set.config.LotteryConfig;
import org.jboss.set.config.LotteryConfigProducer;
import org.jboss.set.util.GitHubProducer;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestFileDetail;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import picocli.CommandLine;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;

@CommandLine.Command(name = "validate", description = "Validates the configuration file upon Pull Request opened, changing the configuration file")
public class ValidationCommand extends JiraCommand {

    @CommandLine.Parameters(index = "0", description = "Number of the opened Pull Request, should be added in GitHub Actions")
    private Integer pullRequestId;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @ConfigProperty(name = "jira-issue-lottery.config-file-repo")
    private Optional<String> repoUrl;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @Inject
    LotteryConfigProducer lotteryConfigProducer;

    @Inject
    GitHubProducer gitHubProducer;

    @Override
    public void run() {
        if (repoUrl.isEmpty()) {
            errorAndPrint("Unable to retrieve github repository property");
            throw new RuntimeException("Unable to retrieve github repository property");
        }

        Log.infof("Running validation upon Pull Request %d in repository %s", pullRequestId, repoUrl.get());

        try {
            GitHub gitHub = gitHubProducer.getGitHub();
            GHRepository ghRepo = gitHub.getRepository(new URI(repoUrl.get()).getPath().substring(1));
            GHPullRequest pullRequest = ghRepo.getPullRequest(pullRequestId);
            for (GHPullRequestFileDetail changedFile : pullRequest.listFiles()) {
                if (changedFile.getFilename().equals(".github/" + LotteryConfig.FILE_NAME)) {
                    Log.infof("Pull Request %d tries to change %s file", pullRequestId, LotteryConfig.FILE_NAME);
                    GHContent updatedFile = gitHub.getRepository(pullRequest.getHead().getRepository().getFullName())
                            .getFileContent(".github/"
                                    + LotteryConfig.FILE_NAME, pullRequest.getHead().getSha());
                    String updatedFileContent = new String(updatedFile.read().readAllBytes());
                    LotteryConfig lotteryConfig = lotteryConfigProducer.getLotteryConfigFromString(updatedFileContent);
                    if (validateConfig(lotteryConfig)) {
                        Log.infof("Pull Request %d keeps the config file valid", pullRequestId);
                    } else {
                        Log.infof("Pull Request %d doesn't keep the config file valid", pullRequestId);
                        // This will cause unsuccessful exit code, thus failing the CI/CD pipeline action
                        throw new RuntimeException("The file was not validated successfully");
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            errorAndPrint("Unable to open anonymous connection to GitHub or parse URL of the repository", e);
            throw new RuntimeException(e);
        }
    }

    private boolean validateConfig(LotteryConfig lotteryConfig) {
        Map<String, Collection<String>> projectComponentsMap = new HashMap<>();
        Set<String> participants = new HashSet<>();
        boolean valid = true;

        for (LotteryConfig.Participant participant : lotteryConfig.participants()) {

            if (participant.maxIssues() != null && participant.maxIssues() <= 0) {
                errorAndPrint("Participant %s has defined non-positive number of max issues".formatted(participant.email()));
                valid = false;
                continue;
            }

            if (participants.contains(participant.email())) {
                errorAndPrint("Two identical participants detected %s".formatted(participant.email()));
                valid = false;
            }
            participants.add(participant.email());

            for (LotteryConfig.Participant.Project project : participant.projects()) {
                if (project.participation().maxIssues() <= 0) {
                    errorAndPrint("Participant %s has defined non-positive number of max issues for project %s"
                            .formatted(participant.email(), project.project()));
                    valid = false;
                    continue;
                }

                // we assume a valid project has some components
                Collection<String> components = projectComponentsMap.computeIfAbsent(project.project(), projectKey -> {
                    try {
                        Project jiraProject = getJiraEndpoint().getClient().getProjectClient().getProject(projectKey).claim();
                        return StreamSupport.stream(jiraProject.getComponents().spliterator(), false)
                                .map(BasicComponent::getName)
                                .map(String::toLowerCase).toList();
                    } catch (RestClientException e) {
                        Log.infof(e, "Unable to find project %s", projectKey);
                        return Collections.emptyList();
                    }
                });

                if (components.isEmpty()) {
                    errorAndPrint(
                            "For participant %s and project %s no components were found on JIRA. You probably made a typo."
                                    .formatted(participant.email(), project.project()));
                    valid = false;
                    continue;
                }

                if (project.components() == null) {
                    continue;
                }

                Collection<String> lowerCasedComponents = project.components().stream().map(String::toLowerCase).toList();
                if (!components.containsAll(lowerCasedComponents)) {
                    errorAndPrint(
                            "For participant %s and project %s some components were found under the project based on JIRA. A typo in one or more components is thus expected."
                                    .formatted(participant.email(), project.project()));
                    valid = false;
                }
            }
        }
        return valid;
    }

    // why "::error::" is prepended see https://stackoverflow.com/a/71091060
    private void errorAndPrint(String message) {
        spec.commandLine().getOut().println("::error::" + message);
        Log.error(message);
    }

    // why "::error::" is prepended see https://stackoverflow.com/a/71091060
    private void errorAndPrint(String message, Throwable e) {
        spec.commandLine().getOut().println("::error::" + message);
        Log.errorf(e, message);
    }
}
