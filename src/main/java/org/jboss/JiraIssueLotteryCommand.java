package org.jboss;

import jakarta.inject.Inject;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.component.jira.JiraComponent;
import org.apache.camel.component.jira.JiraConfiguration;
import org.apache.camel.component.jira.JiraEndpoint;
import org.jboss.config.JiraLotteryAppConfig;
import org.jboss.processing.IssueProcessor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "jira-issue-lottery", mixinStandardHelpOptions = true)
public class JiraIssueLotteryCommand implements Runnable {

    @Inject
    JiraLotteryAppConfig jiraLotteryAppConfig;

    @Inject
    CamelContext camelContext;

    @Parameters(paramLabel = "<name>", defaultValue = "picocli", description = "Your name.")
    String name;

    @Override
    public void run() {
        JiraConfiguration jiraConfiguration = new JiraConfiguration();
        jiraConfiguration.setJiraUrl("https://issues.redhat.com");
        jiraConfiguration.setAccessToken(jiraLotteryAppConfig.accessToken());
        //        jiraEndpoint.setJql("project in (JBEAP, WFLY, WFCORE, RESTEASY) AND component not in (Documentation, Localization) AND assignee = rhn-support-rbudinsk");
        JiraEndpoint jiraEndpoint = new JiraEndpoint("issues.redhat.com", new JiraComponent(camelContext), jiraConfiguration);
        jiraEndpoint.connect();
        Exchange exchange = jiraEndpoint.createExchange();
        try {
            new IssueProcessor(jiraEndpoint, "JBEAP-25900").process(exchange);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //        jiraEndpoint.setType(JiraType.NEWISSUES);
        //        try {
        //            Consumer issues = jiraEndpoint.createConsumer(new IssueProcessor());
        //            issues.start();
        //            try {
        //                Thread.sleep(5 * 1000);
        //            }
        //            catch (InterruptedException e) {
        //                e.printStackTrace();
        //            }
        //            issues.stop();
        //            issues.close();
        ////            System.out.printf("Hello %s\n", issues.createExchange(true).getMessage());
        //        } catch (Exception e) {
        //            throw new RuntimeException(e);
        //        }
    }
}
