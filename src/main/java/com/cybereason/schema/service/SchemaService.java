package com.cybereason.schema.service;

import java.io.InputStream;

public interface SchemaService {
    String getRepositoryReleases(String repositoryName);

    InputStream getRelease(String repositoryName, Long releaseTag);

    String getReleaseInfo(String repositoryName, Long releaseID);

    String createPullRequest(String repositoryName, InputStream fileContent);
}
