package trino.controlplane;

import io.envoyproxy.controlplane.cache.v3.SimpleCache;
import io.envoyproxy.controlplane.server.V3DiscoveryServer;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import trino.controlplane.cache.QueryCache;
import trino.controlplane.endpoint.ClusterListener;
import trino.controlplane.endpoint.ClusterSelector;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static trino.controlplane.constants.Constants.GROUP;

public class ControlPlane {

    static final Logger logger = LoggerFactory.getLogger(ControlPlane.class.getName());

    private Server server;

    private void run() throws IOException {
        SimpleCache<String> cache = new SimpleCache<>(node -> GROUP);
        ClusterListener clusterListener = new ClusterListener();

        new SnapshotRefresher(clusterListener, cache);

        V3DiscoveryServer v3DiscoveryServer = new V3DiscoveryServer(cache);

        server = NettyServerBuilder.forPort(12345)
                .addService(v3DiscoveryServer.getAggregatedDiscoveryServiceImpl())
                .addService(v3DiscoveryServer.getClusterDiscoveryServiceImpl())
                .addService(v3DiscoveryServer.getEndpointDiscoveryServiceImpl())
                .addService(v3DiscoveryServer.getListenerDiscoveryServiceImpl())
                .addService(v3DiscoveryServer.getRouteDiscoveryServiceImpl())
                .addService(clusterListener)
                .addService(new ClusterSelector(new QueryCache(clusterListener)))
                .build()
                .start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Server shutting down!!!");
            try {
                ControlPlane.this.stop();
            } catch (InterruptedException ie) {
                ie.printStackTrace(System.err);
            }
        }));

    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] arg) throws IOException, InterruptedException {
        ControlPlane server = new ControlPlane();
        server.run();
        logger.info("Server started!");
        server.blockUntilShutdown();
    }
}