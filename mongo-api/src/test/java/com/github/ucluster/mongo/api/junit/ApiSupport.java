package com.github.ucluster.mongo.api.junit;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.servlet.ServletRegistration;
import org.glassfish.grizzly.servlet.WebappContext;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;

import static javax.ws.rs.client.Entity.entity;

public class ApiSupport {
    @Inject
    ClientConfigurator clientConfigurator;

    @Inject
    SetUp setUp;

    @Inject
    @Named("server_uri")
    private String serverUri;

    private JerseyTest test;

    @Before
    public void setUp() throws Exception {
        test = new JerseyTest() {
            private ResourceConfig application;

            @Override
            protected Application configure() {
                return getApplication();
            }

            @Override
            protected URI getBaseUri() {
                return URI.create(serverUri);
            }

            @Override
            protected void configureClient(ClientConfig config) {
                super.configureClient(config);
                clientConfigurator.config(config);
            }

            @Override
            protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
                return new TestContainerFactory(getApplication());
            }

            private ResourceConfig getApplication() {
                if (application == null) {
                    application = ResourceConfig.forApplicationClass(InjectorBasedRunner.ApiTestResourceConfig.class);
                }
                return application;
            }
        };
        test.setUp();
        setUp.before();
    }

    @After
    public void tearDown() throws Exception {
        test.tearDown();
    }

    public interface ClientConfigurator {
        void config(ClientConfig config);
    }

    protected WebTarget target(String uri) {
        return test.target(uri);
    }

    protected WebTarget target(URI uri) {
        return test.target(uri.getPath());
    }

    public Response post(String uri, Object json) {
        return target(uri).request()
                .post(entity(json, MediaType.APPLICATION_JSON_TYPE));
    }

    public Response put(String uri, Object json) {
        return target(uri).request()
                .put(entity(json, MediaType.APPLICATION_JSON_TYPE));
    }

    public Response delete(String uri) {
        return target(uri).request()
                .delete();
    }

    public JsonContext json(Response response) {
        final String json = response.readEntity(String.class);
        final DocumentContext context = JsonPath.parse(json);
        return new JsonContext(context);
    }

    public static class JsonContext {
        private DocumentContext context;

        public JsonContext(DocumentContext context) {
            this.context = context;
        }

        public Object path(String path) {
            try {
                return context.read(path);
            } catch (PathNotFoundException e) {
                return null;
            }
        }
    }


    public Response get(String uri) {
        return target(uri).request().get();
    }

    public static class TestContainerFactory implements org.glassfish.jersey.test.spi.TestContainerFactory {
        private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(TestContainerFactory.class.getName());
        private ResourceConfig application;

        public TestContainerFactory(ResourceConfig application) {
            this.application = application;
        }

        @Override
        public org.glassfish.jersey.test.spi.TestContainer create(URI baseUri, DeploymentContext deploymentContext) {
            return new TestContainer(baseUri, application);
        }

        private static class TestContainer implements org.glassfish.jersey.test.spi.TestContainer {
            private final URI baseUri;
            private HttpServer server;
            private static final Logger logger = LoggerFactory.getLogger(TestContainer.class.getName());

            private TestContainer(URI baseUri, ResourceConfig resourceConfig) {
                this.baseUri = baseUri;

                WebappContext context = new WebappContext("ginkgo-test-container");

                ServletRegistration servlet = context.addServlet("Servlet", new ServletContainer(resourceConfig));
                servlet.addMapping("/*");

                server = GrizzlyHttpServerFactory.createHttpServer(baseUri);
                context.deploy(server);
            }

            @Override
            public ClientConfig getClientConfig() {
                ClientConfig clientConfig = new ClientConfig();
                return clientConfig.register(new LoggingFilter(LOGGER, true));
            }

            @Override
            public URI getBaseUri() {
                return baseUri;
            }

            @Override
            public void start() {
                logger.info("Starting JettyTestContainer...");
                try {
                    server.start();
                } catch (Exception e) {
                    throw new TestContainerException(e);
                }
            }

            @Override
            public void stop() {
                logger.info("Stopping TestContainer...");
                try {
                    this.server.shutdownNow();
                } catch (Exception ex) {
                    logger.info("Error Stopping TestContainer...", ex);
                }
            }
        }
    }

    interface SetUp {
        void before() throws IOException;
    }
}
