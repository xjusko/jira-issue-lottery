package org.jboss.draw;

import io.quarkus.arc.Unremovable;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.smallrye.mutiny.tuples.Tuple2;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.config.LotteryConfig;
import org.jboss.config.LotteryConfigProducer;
import org.jboss.draw.entities.Issue;
import org.jboss.draw.entities.Participant;
import org.jboss.processing.state.EveryIssueState;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static org.jboss.config.GitHubRawUrl.GitHubRepoToRawUrlConverter.RELATIVE_PATH;

@Unremovable
@ApplicationScoped
public class Lottery {

    @Inject
    private LotteryConfigProducer lotteryConfigProducer;

    @Inject
    private Mailer mailer;

    private static final Pattern USERNAME_FROM_EMAIL = Pattern.compile("\\s*(\\b[a-zA-Z0-9._%+-]+)@");
    public static final String EMAIL_SUBJECT = "This week's lottery issues picked for %s";
    private static final String EMAIL_BODY = """
            Hi %s,

            your issues chosen for you by the JIRA lottery:

            %s

            If you would like to unsubscribe, please remove yourself from the following repository's configuration file %s

            Have a nice day,
            your Jira issue lottery bot""";

    public void run(EveryIssueState collectedIssues) {
        LotteryConfig lotteryConfig = lotteryConfigProducer.getLotteryConfig();
        // retrieves participants from LotteryConfig
        List<Participant> participants = lotteryConfig.participants().parallelStream()
                .map(participant -> {
                    Map<String, Tuple2<Integer, Set<String>>> collectedProjects = participant.projects().stream()
                            .map(project -> Map.entry(project.project(),
                                    Tuple2.of(project.participation().maxIssues(), project.components())))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    return new Participant(participant.email(), participant.maxIssues(), collectedProjects);
                })
                .collect(Collectors.toList());
        // retrieves issues from LotteryConfig
        List<Issue> issues = collectedIssues.getIssueStates().parallelStream()
                .map(Issue::new)
                .toList();

        Collections.shuffle(participants);
        int lastParticipant = 0;
        for (Issue issue : issues) {
            // break this loop if the same participant is encountered again
            for (int i = (lastParticipant + 1) % participants.size(); true; i = (i + 1) % participants.size()) {
                Participant participant = participants.get(i);
                if (participant.isAssignable(issue)) {
                    participant.assignIssue(issue);
                    lastParticipant = i;
                    break;
                }
                if (lastParticipant == i) {
                    break;
                }
            }
        }

        Map<Participant, List<Issue>> aggregatedByAssignee = issues.stream().filter(issue -> issue.getAssignee() != null)
                .collect(Collectors.groupingBy(Issue::getAssignee));
        aggregatedByAssignee.forEach((participant, assignedIssues) -> mailer.send(new Mail()
                .setSubject(EMAIL_SUBJECT.formatted(getUsername(participant.getEmail())))
                .setText(createEmailText(participant.getEmail(), assignedIssues))
                .setTo(List.of(participant.getEmail()))));
    }

    public static String getUsername(String email) {
        Matcher matcher = USERNAME_FROM_EMAIL.matcher(email);
        return matcher.find() ? matcher.group(1) : "Somebody";
    }

    public static String createEmailText(String email, List<Issue> issues) {
        return EMAIL_BODY.formatted(getUsername(email),
                // group by Project name
                issues.stream().collect(groupingBy(Issue::getProject)).entrySet().stream()
                        // write project and then collect all links with bullet point prepended
                        .map(projectLinksEntry -> projectLinksEntry.getKey() + "\n" +
                                projectLinksEntry.getValue().stream().map(issue -> "\t-" + issue.getBrowseUri().toString())
                                        .collect(Collectors.joining("\n\n")))
                        .collect(joining("\n")),
                configFileUrl());
    }

    private static String configFileUrl() {
        String repo = ConfigProvider.getConfig().getConfigValue("jira-issue-lottery.config-file-repo").getRawValue();
        repo = repo.endsWith("/") ? repo.substring(0, repo.length() - 1) : repo;
        return repo + "/blob/" + RELATIVE_PATH;
    }
}
