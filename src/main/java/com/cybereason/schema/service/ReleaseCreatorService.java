package com.cybereason.schema.service;

import java.io.InputStream;

public interface ReleaseCreatorService {
    String createPR(InputStream fileContent, boolean autoMerge);
}
