package org.jboss.set;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import jakarta.inject.Inject;
import org.jboss.set.processing.CollectorProducer;
import org.jboss.set.processing.NewIssueCollector;
import picocli.CommandLine.Command;

@TopCommand
@Command(name = "jira-issue-lottery", subcommands = { ValidationCommand.class })
public class LotteryCommand extends JiraCommand {

    @Inject
    CollectorProducer collectorProducer;

    @Override
    public void run() {
        NewIssueCollector newIssueCollector = collectorProducer.newIssueCollectorInstance(getJiraEndpoint());
        try {
            newIssueCollector.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
