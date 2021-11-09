package com.cybereason.schema.service;

import java.io.InputStream;

public interface ReleaseCreatorService {
    String createPullRequest(InputStream fileContent);
}
