package com.cybereason.schema.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public abstract class ReleaseServiceAbstract {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseProviderServiceImpl.class);

    InputStream releaseInfoFileContent;
    InputStream featureDefinitionsContent;
    InputStream dynamicDefinitionsContent;
    InputStream deliveryDefinitionsContent;

    void extractReleaseFiles(InputStream releaseZipBall) {
        ZipInputStream zin = new ZipInputStream(releaseZipBall);
        StringBuilder releaseInfoContentBuilder = new StringBuilder();
        StringBuilder featureDefinitionsContentBuilder = new StringBuilder();
        StringBuilder dynamicDefinitionsContentBuilder = new StringBuilder();
        StringBuilder deliveryDefinitionsContentBuilder = new StringBuilder();

        ZipEntry entry;
        byte[] buffer = new byte[1024];
        int read;

        try {
            while ((entry = zin.getNextEntry()) != null) {
                if (entry.getName() != null && entry.getName().contains("release-info.json")) {
                    while ((read = zin.read(buffer, 0, 1024)) >= 0) {
                        releaseInfoContentBuilder.append(new String(buffer, 0, read));
                    }
                } else if (entry.getName() != null && entry.getName().contains("feature.definitions")) {
                    while ((read = zin.read(buffer, 0, 1024)) >= 0) {
                        featureDefinitionsContentBuilder.append(new String(buffer, 0, read));
                    }
                } else if (entry.getName() != null && entry.getName().contains("dynamic.definitions")) {
                    while ((read = zin.read(buffer, 0, 1024)) >= 0) {
                        dynamicDefinitionsContentBuilder.append(new String(buffer, 0, read));
                    }
                } else if (entry.getName() != null && entry.getName().contains("delivery.definitions")) {
                    while ((read = zin.read(buffer, 0, 1024)) >= 0) {
                        deliveryDefinitionsContentBuilder.append(new String(buffer, 0, read));
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Unable to find release content information: ", e.getMessage(), e);
        }

        if (releaseInfoContentBuilder.length() > 0)
            this.releaseInfoFileContent = new ByteArrayInputStream(releaseInfoContentBuilder.toString().getBytes());

        if (featureDefinitionsContentBuilder.length() > 0)
            this.featureDefinitionsContent = new ByteArrayInputStream(featureDefinitionsContentBuilder.toString().getBytes());

        if (dynamicDefinitionsContentBuilder.length() > 0)
            this.dynamicDefinitionsContent = new ByteArrayInputStream(dynamicDefinitionsContentBuilder.toString().getBytes());

        if (deliveryDefinitionsContentBuilder.length() > 0)
            this.deliveryDefinitionsContent = new ByteArrayInputStream(deliveryDefinitionsContentBuilder.toString().getBytes());
    }
}
