package com.cybereason.schema;

import com.cybereason.schema.client.impl.GithubClientImpl;
import com.cybereason.schema.controller.SchemaController;
import com.cybereason.schema.service.impl.SchemaServiceImpl;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Javalin app = Javalin.create()
                .start(8181);
//        TODO: when using Docker please change the path to /static/index.html
        app._conf.addSinglePageRoot("/", "./src/main/resources/static/index.html", Location.EXTERNAL);
//        TODO: when using Docker please change the path to /static/
        app._conf.addStaticFiles("./src/main/resources/static/", Location.EXTERNAL);
        new SchemaController(app, new SchemaServiceImpl(new GithubClientImpl()));
        LOGGER.info("*** Schema Management Server is up and running ***");
    }
}
