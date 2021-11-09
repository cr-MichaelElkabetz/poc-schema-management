package com.cybereason.schema.service.impl;

import com.cybereason.schema.model.ReleaseInfo;
import com.cybereason.schema.service.ReleaseCreatorService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContentBuilder;
import org.kohsuke.github.GHRef;
import org.kohsuke.github.GHRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class ReleaseCreatorServiceImpl extends ReleaseServiceAbstract implements ReleaseCreatorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseCreatorServiceImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final GHRepository repo;

    private ReleaseInfo releaseInfo;


    public ReleaseCreatorServiceImpl(GHRepository repo) {
        this.repo = repo;
    }

    @Override
    public String createPullRequest(InputStream fileContent) {
        extractReleaseFiles(fileContent);
        releaseInfo = getReleaseInfo();
        String featureBranch = createBranch();

        if (featureBranch != null) {
            commitFiles(featureBranch);
            executePullRequest(featureBranch);
            LOGGER.info("Pull request created successfully");
            return "Pull request created successfully";
        }
        return "Unable to create a pull request";
    }

    private void commitFiles(String featureBranch) {
        if (releaseInfoFileContent != null) {
            try {
                String releaseInfoSha = repo.getFileContent("schema/src/main/resources/release-info.json").getSha();
                commitFile("schema/src/main/resources", "release-info.json", releaseInfoSha, releaseInfoFileContent, featureBranch);
            } catch (IOException e) {
                LOGGER.error("Error occurred while tried to commit release-info file");
            }
        }
        if (featureDefinitionsContent != null) {
            try {
                String releaseInfoSha = repo.getFileContent("schema/src/main/resources/feature.definitions").getSha();
                commitFile("schema/src/main/resources", "feature.definitions", releaseInfoSha, featureDefinitionsContent, featureBranch);
            } catch (IOException e) {
                LOGGER.error("Error occurred while tried to commit feature.definitions file");
            }
        }
        if (dynamicDefinitionsContent != null) {
            try {
                String releaseInfoSha = repo.getFileContent("schema/src/main/resources/dynamic.definitions").getSha();
                commitFile("schema/src/main/resources", "dynamic.definitions", releaseInfoSha, dynamicDefinitionsContent, featureBranch);
            } catch (IOException e) {
                LOGGER.error("Error occurred while tried to commit dynamic.definitions file");
            }
        }
        if (deliveryDefinitionsContent != null) {
            try {
                String releaseInfoSha = repo.getFileContent("schema/src/main/resources/delivery.definitions").getSha();
                commitFile("schema/src/main/resources", "delivery.definitions", releaseInfoSha, deliveryDefinitionsContent, featureBranch);
            } catch (IOException e) {
                LOGGER.error("Error occurred while tried to commit delivery.definitions file");
            }
        }
    }

    public ReleaseInfo getReleaseInfo() {
        if (releaseInfoFileContent == null) {
            return null;
        }

        String releaseInfoString = new BufferedReader(
                new InputStreamReader(releaseInfoFileContent, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));

        if (releaseInfoString != null && !"".equalsIgnoreCase(releaseInfoString)) {
            try {
                return objectMapper.readValue(releaseInfoString, ReleaseInfo.class);
            } catch (JsonProcessingException e) {
                LOGGER.error("Unable to get release information from zip: ", e.getMessage(), e);
            }
        }
        return null;
    }

    private void executePullRequest(String featureBranch) {
        try {
            repo.createPullRequest(releaseInfo.getAuthor() + " wish to release a new schema version", featureBranch, "master", "check");
        } catch (IOException e) {
            LOGGER.error("Unable to create a pull request: ", e.getMessage(), e);
        }
    }

    private String createBranch() {
        String branchName = "refs/heads/" + releaseInfo.getAuthor() + "-" + System.currentTimeMillis();
        branchName = branchName.replace(' ', '-');
        try {
            GHRef masterRef = repo.getRef("heads/master");
            repo.createRef(branchName, masterRef.getObject().getSha());
            return branchName;
        } catch (IOException e) {
            LOGGER.error("Unable to create temporary feature branch: ", e.getMessage(), e);
        }
        return null;
    }

    private void commitFile(String githubDirName, String fileName, String sha, InputStream fileContent, String branch) throws IOException {
        LOGGER.info("#########################################");
        LOGGER.info(" - Uploading to repo: " + repo.getName());
        LOGGER.info(" - Branch name: " + branch);
        LOGGER.info(" - File output path: " + githubDirName + "/" + fileName);

        String stringFileContent = new BufferedReader(
                new InputStreamReader(fileContent, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));

        //Create content
        GHContentBuilder content = repo.createContent()
                .message("Updated new release content from Admiral")
                .path(githubDirName + "/" + fileName)
                .branch(branch)
                .content(stringFileContent);

        if (sha != null) {
            content.sha(sha);
        }

        try {
            LOGGER.info(" - Uploading...");
            GHCommit commit = content.commit().getCommit();
            LOGGER.info(" - Upload successful at: " + commit.getCommitDate());
            LOGGER.info("#########################################");

        } catch (IOException e) {
            LOGGER.error("Commit failed! ", e.getMessage(), e);
        }
    }
}
