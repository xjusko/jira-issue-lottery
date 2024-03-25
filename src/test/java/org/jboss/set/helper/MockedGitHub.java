package org.jboss.set.helper;

import org.jboss.set.config.LotteryConfig;
import org.jboss.set.util.GitHubProducer;
import org.kohsuke.github.GHCommitPointer;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestFileDetail;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterator;
import org.kohsuke.github.PagedSearchIterable;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockedGitHub {

    private final List<String> files = new ArrayList<>();
    private String configFileContent = "";

    private MockedGitHub() {
    }

    public static MockedGitHub builder() {
        return new MockedGitHub();
    }

    public MockedGitHub prFiles(String... filenames) {
        this.files.addAll(new ArrayList<>(Arrays.asList(filenames)));
        return this;
    }

    public MockedGitHub prConfigFileContent(String content) {
        this.configFileContent = content;
        return this;
    }

    public void mockGitHub(GitHubProducer gitHubProducer) throws IOException {
        GHPullRequest pullRequest = mock(GHPullRequest.class);
        GHRepository repository = mock(GHRepository.class);
        GHCommitPointer ghCommitPointer = mock(GHCommitPointer.class);

        when(repository.getPullRequest(anyInt())).thenReturn(pullRequest);
        when(pullRequest.getHead()).thenReturn(ghCommitPointer);
        when(ghCommitPointer.getRepository()).thenReturn(repository);

        GitHub gitHub = mock(GitHub.class);

        when(gitHubProducer.getGitHub()).thenReturn(gitHub);
        when(gitHub.getRepository(any())).thenReturn(repository);

        List<GHPullRequestFileDetail> mockedFileDetails = new ArrayList<>();
        for (String filename : files) {
            GHPullRequestFileDetail fileDetail = Mockito.mock(GHPullRequestFileDetail.class);
            Mockito.when(fileDetail.getFilename()).thenReturn(filename);
            mockedFileDetails.add(fileDetail);
        }

        // Heavily inspired by Quarkus's GitHub App, permalink to the class https://github.com/quarkiverse/quarkus-github-app/blob/3917701637b174eea76b5b7856737400fb3bac72/testing/src/main/java/io/quarkiverse/githubapp/testing/GitHubAppMockito.java
        //noinspection unchecked
        PagedSearchIterable<GHPullRequestFileDetail> fileDetails = (PagedSearchIterable<GHPullRequestFileDetail>) mock(
                PagedSearchIterable.class);
        when(fileDetails.iterator()).thenAnswer(ignored -> {
            //noinspection unchecked
            PagedIterator<GHPullRequestFileDetail> iteratorMock = (PagedIterator<GHPullRequestFileDetail>) Mockito
                    .mock(PagedIterator.class);
            Iterator<GHPullRequestFileDetail> actualIterator = mockedFileDetails.iterator();
            Mockito.when(iteratorMock.next()).thenAnswer((ignored2) -> actualIterator.next());
            Mockito.when(iteratorMock.hasNext()).thenAnswer((ignored2) -> actualIterator.hasNext());
            return iteratorMock;
        });

        Mockito.when(pullRequest.listFiles()).thenReturn(fileDetails);

        GHContent mockGHContent = mock(GHContent.class);
        when(repository.getFileContent(eq(".github/" + LotteryConfig.FILE_NAME), any())).thenReturn(mockGHContent);
        when(mockGHContent.read()).thenReturn(new ByteArrayInputStream(configFileContent.getBytes(StandardCharsets.UTF_8)));
    }
}
