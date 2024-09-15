package org.example;

import io.javalin.Javalin;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicHttpServer {

    // Create a counter metric for Prometheus
    static Counter requests = Counter.builder()
            .name("my_count_total")
            .help("example counter")
            .labelNames("status")
            .register();


    // Initialize logger
    private static final Logger logger = LoggerFactory.getLogger(BasicHttpServer.class);

    public static void main(String[] args) throws Exception {
        // Initialize Prometheus default JVM metrics
        JvmMetrics.builder().register(); // initialize the out-of-the-box JVM metrics

        // Start Prometheus HTTP server for metrics at port 9091
        HTTPServer prometheusServer = HTTPServer.builder()
                .port(9091)
                .buildAndStart();

        // Start the javalin server
        AppServer server = new AppServer();

            logger.info("Starting Jetty server on port 8080...");
        server.start();

        // Shut down Prometheus HTTP server on exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down Prometheus HTTP server...");
            prometheusServer.stop();
        }));
    }

    public static class AppServer {
        final Javalin app;

        public AppServer() {
            app = Javalin.create();
        }

        public void start() {
            app.start(8080);
            app.get("/hello", ctx -> {
                try {
                    requests.labelValues("ok").inc();
                    ctx.result("Hello World");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
