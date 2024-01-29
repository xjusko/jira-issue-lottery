package org.jboss.config;

import org.eclipse.microprofile.config.spi.Converter;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Class containing only String but due to some preprocessing of this
 * property we need to have it encapsulated. We expect the repository
 * to be publicly available.
 */
public class GitHubRawUrl {

    private final String rawContentsUrl;

    private GitHubRawUrl(String rawContentsUrl) {
        this.rawContentsUrl = rawContentsUrl;
    }

    public String getRawContentsUrl() {
        return rawContentsUrl;
    }

    @Override
    public String toString() {
        return rawContentsUrl;
    }

    public static class GitHubRepoToRawUrlConverter implements Converter<GitHubRawUrl> {

        private static final String RAW_CONTENTS = "https://raw.githubusercontent.com";
        public static final String RELATIVE_PATH = "main/.github/" + LotteryConfig.FILE_NAME;

        public GitHubRepoToRawUrlConverter() {
        }

        @Override
        public GitHubRawUrl convert(String value) {
            try {
                String repo = new URI(value).getPath();
                String rawContentsURI = RAW_CONTENTS + repo + (repo.endsWith("/") ? "" : "/") + RELATIVE_PATH;
                return new GitHubRawUrl(rawContentsURI);

            } catch (URISyntaxException e) {
                throw new RuntimeException("Provided GitHub repo url is not valid");
            }
        }
    }
}
