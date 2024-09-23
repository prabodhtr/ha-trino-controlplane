package trino.controlplane;

import com.google.common.collect.ImmutableList;
import io.envoyproxy.controlplane.cache.v3.SimpleCache;
import io.envoyproxy.controlplane.cache.v3.Snapshot;
import io.envoyproxy.envoy.config.cluster.v3.Cluster;
import io.envoyproxy.envoy.config.listener.v3.Listener;
import trino.common.models.Observer;
import trino.controlplane.endpoint.ClusterListener;
import trino.controlplane.xds.CdsFactory;
import trino.controlplane.xds.LdsFactory;

import java.time.LocalDateTime;
import java.util.List;

import static trino.controlplane.constants.Constants.GROUP;

public class SnapshotRefresher implements Observer {

    ClusterListener clusterListener;
    SimpleCache<String> cache;

    private List<Cluster> clusters;
    private List<Listener> listeners;

    public SnapshotRefresher(ClusterListener clusterListener, SimpleCache<String> cache){
        this.cache = cache;
        clusters = CdsFactory.builder().build();
        listeners = LdsFactory.builder()
                .addTrinoListener(8000)
                .build();

        this.clusterListener = clusterListener;
        this.clusterListener.addObserver(this);

        updateSnapshot();
    }

    @Override
    public void update() {
        updateClusters();
        updateListeners();
        updateSnapshot();
    }

    private void updateClusters(){
        CdsFactory latestClusters = CdsFactory.builder();
        clusterListener.getCurrentState()
                .forEach((key, value) -> latestClusters.addCluster(key, key, value));

        clusters = latestClusters.build();
    }

    private void updateListeners(){}

    private void updateSnapshot() {
        cache.setSnapshot(
                GROUP,
                Snapshot.create(
                        clusters,
                        ImmutableList.of(),
                        listeners,
                        ImmutableList.of(),
                        ImmutableList.of(),
                        LocalDateTime.now().toString()));
    }
}
