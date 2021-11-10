package com.cybereason.schema.service.impl;

import com.cybereason.schema.client.GithubClient;
import com.cybereason.schema.model.Release;
import com.cybereason.schema.service.ReleaseCreatorService;
import com.cybereason.schema.service.ReleaseProviderService;
import com.cybereason.schema.service.SchemaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class SchemaServiceImpl implements SchemaService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaServiceImpl.class);
    ObjectMapper objectMapper = new ObjectMapper();
    private final GithubClient githubClient;

    public SchemaServiceImpl(GithubClient githubClient) {
        this.githubClient = githubClient;
    }

    @Override
    public String getRepositoryReleases(String repositoryName) {
        LOGGER.info("Getting repository, (" + repositoryName + "), releases");
        GHRepository repo = this.githubClient.getRepository(repositoryName);

        try {
            List<GHRelease> repoReleases = repo.getReleases();
            List<Release> releases = repoReleases.stream()
                    .map(item -> new Release(item.getTagName(), item.getId())).collect(Collectors.toList());
            return objectMapper.writeValueAsString(releases);
        } catch (IOException e) {
            LOGGER.error("Error occurred while trying to fetch schema releases: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public InputStream getRelease(String repositoryName, Long releaseID) {
        LOGGER.info("Getting release , repository: " + repositoryName + ", ID: " + releaseID);
        GHRepository repo = this.githubClient.getRepository(repositoryName);
        ReleaseProviderService releaseProviderService = new ReleaseProviderServiceImpl(repo);
        return releaseProviderService.getReleaseContent(releaseID);
    }

    @Override
    public String getReleaseInfo(String repositoryName, Long releaseID) {
        LOGGER.info("Getting release info, repository: " + repositoryName + ", ID: " + releaseID);
        GHRepository repo = this.githubClient.getRepository(repositoryName);
        ReleaseProviderService releaseProviderService = new ReleaseProviderServiceImpl(repo);
        return releaseProviderService.getReleaseInfo(releaseID);
    }

    @Override
    public String createPullRequest(String repositoryName, InputStream fileContent) {
        LOGGER.info("Creating a new pull request, repository: " + repositoryName);
        GHRepository repo = this.githubClient.getRepository(repositoryName);
        ReleaseCreatorService releaseCreatorService = new ReleaseCreatorServiceImpl(repo);
        return releaseCreatorService.createNewRelease(fileContent);
    }
}