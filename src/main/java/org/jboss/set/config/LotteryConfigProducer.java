package org.jboss.set.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@ApplicationScoped
public class LotteryConfigProducer {

    private LotteryConfig lotteryConfig;
    @Inject
    ObjectMapper objectMapper;

    @Inject
    JiraLotteryAppConfig jiraLotteryAppConfig;

    public LotteryConfig getLotteryConfig() {
        if (lotteryConfig == null) {
            try {
                lotteryConfig = objectMapper.readValue(
                        new URI(jiraLotteryAppConfig.configFileRepo().getRawContentsUrl()).toURL(),
                        LotteryConfig.class);
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        return lotteryConfig;
    }

    public LotteryConfig getLotteryConfigFromString(String configFile) {
        try {
            return objectMapper.readValue(configFile, LotteryConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
