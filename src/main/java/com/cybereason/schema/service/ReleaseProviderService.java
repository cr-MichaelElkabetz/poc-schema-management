package com.cybereason.schema.service;

import java.io.InputStream;

public interface ReleaseProviderService {
    InputStream getReleaseContent(Long releaseID);

    String getReleaseInfo(Long releaseID);
}
