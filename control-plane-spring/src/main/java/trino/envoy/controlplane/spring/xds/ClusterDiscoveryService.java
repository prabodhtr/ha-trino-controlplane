package trino.envoy.controlplane.spring.xds;

import com.google.protobuf.Duration;
import com.google.protobuf.UInt32Value;
import io.envoyproxy.envoy.config.cluster.v3.Cluster;
import io.envoyproxy.envoy.config.core.v3.Address;
import io.envoyproxy.envoy.config.core.v3.HealthCheck;
import io.envoyproxy.envoy.config.core.v3.SocketAddress;
import io.envoyproxy.envoy.config.endpoint.v3.ClusterLoadAssignment;
import io.envoyproxy.envoy.config.endpoint.v3.Endpoint;
import io.envoyproxy.envoy.config.endpoint.v3.LbEndpoint;
import io.envoyproxy.envoy.config.endpoint.v3.LocalityLbEndpoints;
import org.springframework.stereotype.Component;
import trino.common.models.ClusterInfo;
import trino.common.models.Observer;
import trino.common.models.Subject;
import trino.envoy.controlplane.spring.registry.ClusterRegistry;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>This class takes the role of a java <code>Observable</code> class that holds the list of dynamic {@link Cluster}
 * configuration it its state. Whenever the state is updated, all the observers are duly notified</p>
 *
 * <p>It acts as an <code>Observer</code> that listens for notification from {@link ClusterRegistry}
 * for possible changes in registered cluster list. On receiving a notification,
 * it updates its state by recreating the list of dynamic cluster</p>
 */
@Component
public class ClusterDiscoveryService extends Subject<List<Cluster>> implements Observer {

    private final ClusterRegistry clusterRegistry;

    public ClusterDiscoveryService(ClusterRegistry clusterRegistry) {
        this.clusterRegistry = clusterRegistry;
        this.setState(new ArrayList<>());
    }

    @PostConstruct
    public void subscribe() {
        clusterRegistry.addObserver(this);
    }

    private void addCluster(String clusterName, ClusterInfo clusterInfo) {
        this.getState().add(Cluster.newBuilder()
                .setName(clusterName)
                .setType(Cluster.DiscoveryType.STRICT_DNS)
                .addHealthChecks(HealthCheck.newBuilder()
                        .setTimeout(Duration.newBuilder().setSeconds(5))
                        .setInterval(Duration.newBuilder().setSeconds(60))
                        .setUnhealthyThreshold(UInt32Value.newBuilder().setValue(1))
                        .setHealthyThreshold(UInt32Value.newBuilder().setValue(2))
                        .setHttpHealthCheck(HealthCheck.HttpHealthCheck.newBuilder()
                                .setPath("/v1/info")))
                .setLoadAssignment(
                        ClusterLoadAssignment.newBuilder()
                                .setClusterName(clusterName)
                                .addEndpoints(
                                        LocalityLbEndpoints.newBuilder()
                                                .addLbEndpoints(LbEndpoint.newBuilder()
                                                        .setEndpoint(Endpoint.newBuilder()
                                                                .setAddress(Address.newBuilder()
                                                                        .setSocketAddress(SocketAddress.newBuilder()
                                                                                .setAddress(clusterInfo.getHost())
                                                                                .setPortValue(clusterInfo.getPort())))))))
                .build());
    }

    @Override
    public void update() {
        this.getState().clear();
        clusterRegistry.getState().forEach(this::addCluster);
        notifyObservers();
    }
}
