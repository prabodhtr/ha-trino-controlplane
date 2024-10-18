package trino.envoy.controlplane.spring;

import com.google.common.collect.ImmutableList;
import io.envoyproxy.controlplane.cache.v3.SimpleCache;
import io.envoyproxy.controlplane.cache.v3.Snapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import trino.common.models.Observer;
import trino.envoy.controlplane.spring.xds.ClusterDiscoveryService;
import trino.envoy.controlplane.spring.xds.ListenerDiscoveryService;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

import static trino.envoy.controlplane.spring.constants.Constants.GROUP;

/**
 * This is typical java <code>Observer</code> that listens for a notification from {@link ClusterDiscoveryService}
 * and {@link ListenerDiscoveryService}, on discovery config updates.
 * On receiving the notification, it updates the envoy snapshot cache with the latest service configs,
 * obtained from the above classes, which is then pulled in by envoy router during next poll
 */
@Component
@RequiredArgsConstructor
public class SnapshotRefresher implements Observer {

    private final ClusterDiscoveryService cds;
    private final ListenerDiscoveryService lds;
    private final SimpleCache<String> snapShotCache;

    @PostConstruct
    public void init(){
        cds.addObserver(this);
        lds.addObserver(this);
        updateSnapshot();
    }

    @Override
    public void update() {
        updateSnapshot();
    }

    private void updateSnapshot() {
        snapShotCache.setSnapshot(
                GROUP,
                Snapshot.create(
                        cds.getState(),
                        ImmutableList.of(),
                        lds.getState(),
                        ImmutableList.of(),
                        ImmutableList.of(),
                        LocalDateTime.now().toString()));
    }
}
