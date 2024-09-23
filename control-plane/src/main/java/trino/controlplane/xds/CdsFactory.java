package trino.controlplane.xds;

import io.envoyproxy.envoy.config.cluster.v3.Cluster;
import io.envoyproxy.envoy.config.core.v3.Address;
import io.envoyproxy.envoy.config.core.v3.SocketAddress;
import io.envoyproxy.envoy.config.endpoint.v3.ClusterLoadAssignment;
import io.envoyproxy.envoy.config.endpoint.v3.Endpoint;
import io.envoyproxy.envoy.config.endpoint.v3.LbEndpoint;
import io.envoyproxy.envoy.config.endpoint.v3.LocalityLbEndpoints;

import java.util.ArrayList;
import java.util.List;

/**
 * Refer <a href="https://github.com/envoyproxy/java-control-plane/blob/main/cache/src/main/java/io/envoyproxy/controlplane/cache/TestResources.java">TestResources.class</a>
 * at envoyproxy github page
 */
public class CdsFactory {

    private final List<Cluster> clusters;

    private CdsFactory(List<Cluster> clusters){
        this.clusters = clusters;
    }

    public static CdsFactory builder() {
        List<Cluster> clusters = new ArrayList<>();
        return new CdsFactory(clusters);
    }

    public CdsFactory addCluster(String clusterName, String address, int port){
        clusters.add(Cluster.newBuilder()
                .setName(clusterName)
                .setType(Cluster.DiscoveryType.STRICT_DNS)
                .setLoadAssignment(
                        ClusterLoadAssignment.newBuilder()
                                .setClusterName(clusterName)
                                .addEndpoints(
                                        LocalityLbEndpoints.newBuilder()
                                                .addLbEndpoints(LbEndpoint.newBuilder()
                                                        .setEndpoint(Endpoint.newBuilder()
                                                                .setAddress(Address.newBuilder()
                                                                        .setSocketAddress(SocketAddress.newBuilder()
                                                                                .setAddress(address)
                                                                                .setPortValue(port)))))))
                .build());
        return this;
    }

    public List<Cluster> build(){
        return clusters;
    }
}
