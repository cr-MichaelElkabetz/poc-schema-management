package com.cybereason.schema.service.impl;

import com.cybereason.schema.constant.Constants;
import com.cybereason.schema.service.ReleaseProviderService;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ReleaseProviderServiceImpl extends ReleaseServiceAbstract implements ReleaseProviderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseProviderServiceImpl.class);
    private final GHRepository repo;


    public ReleaseProviderServiceImpl(GHRepository repo) {
        this.repo = repo;
    }

    @Override
    public InputStream getReleaseContent(Long releaseID) {
        InputStream releaseZipFile = getReleaseZipFile(releaseID);

        if (releaseZipFile != null) {
            extractReleaseFiles(releaseZipFile);

            if (releaseInfoFileContent != null || featureDefinitionsContent != null || dynamicDefinitionsContent != null || deliveryDefinitionsContent != null) {
                return zipReleaseContent();
            }
        }

        return null;
    }

    @Override
    public String getReleaseInfo(Long releaseID) {
        InputStream releaseZipFile = getReleaseZipFile(releaseID);

        if (releaseZipFile != null) {
            extractReleaseFiles(releaseZipFile);
        }

        if (this.releaseInfoFileContent != null) {
            StringBuilder textBuilder = new StringBuilder();
            try (Reader reader = new BufferedReader(new InputStreamReader
                    (this.releaseInfoFileContent, Charset.forName(StandardCharsets.UTF_8.name())))) {
                int c = 0;
                while ((c = reader.read()) != -1) {
                    textBuilder.append((char) c);
                }
                return textBuilder.toString();
            } catch (IOException e) {
                LOGGER.error("Unable to get release information: ", e.getMessage(), e);
            }
        }
        return null;
    }

    private InputStream getReleaseZipFile(Long releaseID) {
        GHRelease release;
        String zipURL = null;
        try {
            release = repo.getRelease(releaseID);
            if (release != null && release.getZipballUrl() != null) {
                zipURL = release.getZipballUrl();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (zipURL != null) {
            OkHttpClient client = new OkHttpClient.Builder().build();

            Request request = new Request.Builder()
                    .url(zipURL)
                    .header("Authorization", "Bearer " + Constants.GITHUB_TOKEN)
                    .build();

            Call call = client.newCall(request);

            Response response = null;
            try {
                response = call.execute();
                InputStream targetStream = response.body().byteStream();
                return targetStream;
            } catch (IOException e) {
                LOGGER.error("Unable to get release zip file: ", e.getMessage(), e);
            }
        }
        return null;
    }

    private InputStream zipReleaseContent() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(out);


        if (this.releaseInfoFileContent != null) {
            ZipEntry releaseInfoEntry = new ZipEntry("release-info.json");
            try {
                zipOut.putNextEntry(releaseInfoEntry);
                byte[] bytes = new byte[1024];
                int length;
                while ((length = this.releaseInfoFileContent.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                zipOut.closeEntry();
                this.releaseInfoFileContent.close();
            } catch (IOException e) {
                LOGGER.error("Unable to get file content, (release-info.json): ", e.getMessage(), e);
            }
        }

        if (this.featureDefinitionsContent != null) {
            ZipEntry featureDefinitionsEntry = new ZipEntry("feature.definitions");
            try {
                zipOut.putNextEntry(featureDefinitionsEntry);
                byte[] bytes = new byte[1024];
                int length;
                while ((length = this.featureDefinitionsContent.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                zipOut.closeEntry();
                this.featureDefinitionsContent.close();
            } catch (IOException e) {
                LOGGER.error("Unable to get file content, (feature.definitions): ", e.getMessage(), e);
            }
        }

        if (this.dynamicDefinitionsContent != null) {
            ZipEntry dynamicDefinitionsEntry = new ZipEntry("dynamic.definitions");
            try {
                zipOut.putNextEntry(dynamicDefinitionsEntry);
                byte[] bytes = new byte[1024];
                int length;
                while ((length = this.dynamicDefinitionsContent.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                zipOut.closeEntry();
                this.dynamicDefinitionsContent.close();
            } catch (IOException e) {
                LOGGER.error("Unable to get file content, (dynamic.definitions): ", e.getMessage(), e);
            }
        }

        if (this.deliveryDefinitionsContent != null) {
            ZipEntry deliveryDefinitionsEntry = new ZipEntry("delivery.definitions");
            try {
                zipOut.putNextEntry(deliveryDefinitionsEntry);
                byte[] bytes = new byte[1024];
                int length;
                while ((length = this.deliveryDefinitionsContent.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                zipOut.closeEntry();
                this.deliveryDefinitionsContent.close();
            } catch (IOException e) {
                LOGGER.error("Unable to get file content, (delivery.definitions): ", e.getMessage(), e);
            }
        }

        try {
            zipOut.close();
        } catch (IOException e) {
            LOGGER.error("Error occurred while closing zip output stream: ", e.getMessage(), e);
        }
        return new ByteArrayInputStream(out.toByteArray());
    }
}
