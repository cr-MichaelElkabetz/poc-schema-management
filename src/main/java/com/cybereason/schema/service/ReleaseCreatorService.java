package com.cybereason.schema.service;

import java.io.InputStream;

public interface ReleaseCreatorService {
    String createNewRelease(InputStream fileContent);
}
