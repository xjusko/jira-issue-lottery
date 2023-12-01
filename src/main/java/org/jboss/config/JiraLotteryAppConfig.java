package org.jboss.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "jira-issue-lottery")
public interface JiraLotteryAppConfig {

    String accessToken();

    @WithDefault("50")
    Integer maxResults();

    /**
     * URL of the public GitHub repository, must be public.
     * The config file is then expected under .github/ directory
     * called {@link LotteryConfig#FILE_NAME}
     */
    GitHubRawUrl configFileRepo();
}
