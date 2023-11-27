package org.jboss.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "jira-issue-lottery")
public interface JiraLotteryAppConfig {

    String accessToken();
}
