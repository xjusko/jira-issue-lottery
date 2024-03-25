package org.jboss.set.util;

import jakarta.enterprise.context.ApplicationScoped;
import org.kohsuke.github.GitHub;

import java.io.IOException;

@ApplicationScoped
public class GitHubProducer {

    public GitHub getGitHub() throws IOException {
        return GitHub.connectAnonymously();
    }
}
