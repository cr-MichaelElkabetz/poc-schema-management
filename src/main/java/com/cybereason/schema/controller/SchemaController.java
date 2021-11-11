package com.cybereason.schema.controller;

import com.cybereason.schema.service.SchemaService;
import io.javalin.Javalin;
import io.javalin.http.ContentType;

import java.io.InputStream;

public class SchemaController {
    public SchemaController(Javalin app, SchemaService schemaService) {
        app.get("/health", ctx -> {
            ctx.res.setContentType("application/json");
            ctx.res.setStatus(200);
            ctx.result("{\"message\":\"ok\"}");
        });

        app.get("/releases/{repository}", ctx -> {
            ctx.res.setContentType("application/json");
            String repositoryName = ctx.pathParam("repository");
            String response = schemaService.getRepositoryReleases(repositoryName);
            if ("ERROR".equalsIgnoreCase(response)) {
                ctx.result("{\"Error Message\":\"Failed to get repository releases:" + repositoryName + "\"}");
            } else {
                ctx.result(response);
            }
            ctx.res.setStatus(200);
        });

        app.get("/release/{repository}/{releaseTag}/{releaseID}", ctx -> {
            ctx.res.setContentType(String.valueOf(ContentType.APPLICATION_OCTET_STREAM));
            String releaseTag = ctx.pathParam("releaseTag");
            ctx.res.addHeader("Content-disposition", "attachment;filename=schema-release-" + releaseTag + ".zip");
            String repositoryName = ctx.pathParam("repository");
            Long releaseID = Long.valueOf(ctx.pathParam("releaseID"));

            InputStream releaseFile = schemaService.getRelease(repositoryName, releaseID);

            if (releaseFile == null) {
                ctx.result("{\"message\":\"Release delivery failed\"}");
            } else {
                ctx.result(releaseFile);
            }
            ctx.res.setStatus(200);
        });

        app.get("/release-info/{repository}/{releaseID}", ctx -> {
            ctx.res.setContentType("application/json");
            String repositoryName = ctx.pathParam("repository");
            Long releaseID = Long.valueOf(ctx.pathParam("releaseID"));

            String response = schemaService.getReleaseInfo(repositoryName, releaseID);
            if (response == null || "ERROR".equalsIgnoreCase(response)) {
                ctx.result("{\"message\":\"Unable to get release info\"}");
            } else {
                ctx.result(response);
            }
            ctx.res.setStatus(200);
        });

        app.post("/pr/{repository}", ctx -> {
            ctx.res.setContentType("application/json");
            String repositoryName = ctx.pathParam("repository");

            InputStream fileContent = ctx.bodyAsInputStream();

            String response = schemaService.processRelease(repositoryName, fileContent, false);
            if ("ERROR".equalsIgnoreCase(response)) {
                ctx.result("{\"message\":\"Processing failed\"}");
            } else {
                ctx.result("{\"message\":\"" + response + "\"}");
            }
            ctx.res.setStatus(200);
        });

        app.post("/release/{repository}", ctx -> {
            ctx.res.setContentType("application/json");
            String repositoryName = ctx.pathParam("repository");

            InputStream fileContent = ctx.bodyAsInputStream();

            String response = schemaService.processRelease(repositoryName, fileContent, true);
            if ("ERROR".equalsIgnoreCase(response)) {
                ctx.result("{\"message\":\"Processing failed\"}");
            } else {
                ctx.result("{\"message\":\"" + response + "\"}");
            }
            ctx.res.setStatus(200);
        });
    }
}
