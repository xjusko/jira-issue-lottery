package org.jboss.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "jira-issue-lottery")
public interface JiraLotteryAppConfig {

    String accessToken();

    @WithDefault("50")
    Integer maxResults();
}
