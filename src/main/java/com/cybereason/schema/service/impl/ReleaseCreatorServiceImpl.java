package com.cybereason.schema.service.impl;

import com.cybereason.schema.model.ReleaseInfo;
import com.cybereason.schema.service.ReleaseCreatorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kohsuke.github.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class ReleaseCreatorServiceImpl extends ReleaseServiceAbstract implements ReleaseCreatorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseCreatorServiceImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final GHRepository repo;

    private ReleaseInfo releaseInfo;

    public ReleaseCreatorServiceImpl(GHRepository repo) {
        this.repo = repo;
    }

    @Override
    public String createPR(InputStream fileContent, boolean autoMerge) {
        extractReleaseFiles(fileContent);
        getReleaseInfo();
        String featureBranch = createBranch();

        if (featureBranch != null) {
            commitFiles(featureBranch);
            GHPullRequest pullRequest = performPullRequest(featureBranch);
            if (autoMerge == true) {
                mergePullRequest(pullRequest);
                String releaseTag = createNewRelease();
                LOGGER.info("A new release created successfully, release tag: " + releaseTag);
                return "A new release created successfully";
            } else {
                LOGGER.info("A pull request created successfully");
                return "Pull request created successfully";
            }
        }
        return "Unable to create a new release";
    }

    private void commitFiles(String featureBranch) {
        String shaInfo = null;
        if (releaseInfoFileContent != null) {
            try {
                shaInfo = repo.getFileContent("schema/src/main/resources/release-info.json").getSha();
            } catch (IOException e) {
                LOGGER.error("Error occurred while tried to commit release-info file", e.getMessage(), e);
            }
            try {
                commitFile("schema/src/main/resources", "release-info.json", shaInfo, releaseInfoFileContent, featureBranch);
            } catch (IOException e) {
                LOGGER.error("Error occurred while tried to commit release-info file", e.getMessage(), e);
            }
        }
        if (featureDefinitionsContent != null) {
            try {
                shaInfo = repo.getFileContent("schema/src/main/resources/feature.definitions").getSha();
            } catch (IOException e) {
                LOGGER.error("Error occurred while tried to commit feature.definitions file", e.getMessage(), e);
            }
            try {
                commitFile("schema/src/main/resources", "feature.definitions", shaInfo, featureDefinitionsContent, featureBranch);
            } catch (IOException e) {
                LOGGER.error("Error occurred while tried to commit feature.definitions file", e.getMessage(), e);
            }
        }
        if (dynamicDefinitionsContent != null) {
            try {
                shaInfo = repo.getFileContent("schema/src/main/resources/dynamic.definitions").getSha();
            } catch (IOException e) {
                LOGGER.error("Error occurred while tried to commit dynamic.definitions file", e.getMessage(), e);
            }
            try {
                commitFile("schema/src/main/resources", "dynamic.definitions", shaInfo, dynamicDefinitionsContent, featureBranch);
            } catch (IOException e) {
                LOGGER.error("Error occurred while tried to commit dynamic.definitions file", e.getMessage(), e);
            }
        }
        if (deliveryDefinitionsContent != null) {
            try {
                shaInfo = repo.getFileContent("schema/src/main/resources/delivery.definitions").getSha();
            } catch (IOException e) {
                LOGGER.error("Error occurred while tried to commit delivery.definitions file", e.getMessage(), e);
            }
            try {
                commitFile("schema/src/main/resources", "delivery.definitions", shaInfo, deliveryDefinitionsContent, featureBranch);
            } catch (IOException e) {
                LOGGER.error("Error occurred while tried to commit delivery.definitions file", e.getMessage(), e);
            }
        }
    }

    public void getReleaseInfo() {
        if (releaseInfoFileContent == null) {
            return;
        }

        try {
            Scanner s = new Scanner(releaseInfoFileContent).useDelimiter("\\A");
            String releaseInfoString = s.hasNext() ? s.next() : "";

            releaseInfoFileContent.reset();

            if (releaseInfoString != null && !"".equalsIgnoreCase(releaseInfoString)) {
                releaseInfoFileContent.reset();
                releaseInfo = objectMapper.readValue(releaseInfoString, ReleaseInfo.class);
            }
        } catch (IOException e) {
            LOGGER.error("Unable to get release information from zip: ", e.getMessage(), e);
        }
    }

    private GHPullRequest performPullRequest(String featureBranch) {
        try {
            GHPullRequest pullRequest = repo.createPullRequest(releaseInfo.getAuthor() + " wish to release a new schema version", featureBranch, "master", "");
            LOGGER.info("Pull request created successfully");
            return pullRequest;
        } catch (IOException e) {
            LOGGER.error("Unable to create a pull request: ", e.getMessage(), e);
        }
        return null;
    }

    private void mergePullRequest(GHPullRequest pullRequest) {
        try {
            pullRequest.merge("Auto merge schema release");
            LOGGER.info("Auto merge pull request successfully");
        } catch (IOException e) {
            LOGGER.error("Unable to merge a pull request: ", e.getMessage(), e);
        }
    }

    private String createNewRelease() {
        try {
            GHRelease lastRelease = repo.getReleases().get(0);
            String releaseTag = increaseReleaseVersion(lastRelease);
            repo.createRelease(releaseTag).create();
            return releaseTag;
        } catch (IOException e) {
            LOGGER.error("Unable to merge a pull request: ", e.getMessage(), e);
        }
        return null;
    }

    private String increaseReleaseVersion(GHRelease lastRelease) {
        String lastReleaseTag = lastRelease.getTagName();
        String[] fn = lastReleaseTag.split("\\.");
        if (Integer.valueOf(fn[1]) > 50) {
            fn[0] = String.valueOf(Integer.valueOf(fn[0]) + 1);
            fn[1] = String.valueOf(0);
        } else {
            fn[1] = String.valueOf(Integer.valueOf(fn[1]) + 1);
        }
        return String.join(".", fn);
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

        Scanner s = new Scanner(fileContent).useDelimiter("\\A");
        String stringFileContent = s.hasNext() ? s.next() : "";

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
