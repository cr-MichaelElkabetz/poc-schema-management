package com.cybereason.schema.client.impl;

import com.cybereason.schema.client.GithubClient;
import com.cybereason.schema.constant.Constants;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class GithubClientImpl implements GithubClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(GithubClientImpl.class);
    private GitHub githubClient;

    public GithubClientImpl() {
        try {
            githubClient = new GitHubBuilder().withJwtToken(Constants.GITHUB_TOKEN).build();
        } catch (IOException e) {
            LOGGER.error("Unable to initialize github client: " + e.getMessage(), e);
        }

    }

    @Override
    public GHRepository getRepository(String repositoryName) {
        GHRepository repository = null;

        try {
            repository = githubClient.getRepository("cybereason-labs/" +repositoryName);
        } catch (IOException e) {
            LOGGER.error("Unable to get repository: " + repositoryName + "", e.getMessage(), e);
        }
        return repository;
    }

}

