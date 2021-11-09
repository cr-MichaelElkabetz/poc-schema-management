package com.cybereason.schema.client;

import org.kohsuke.github.GHRepository;


public interface GithubClient {
   GHRepository getRepository(String repositoryName);
}
