package trino.envoy.controlplane.spring.registry;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;
import trino.common.models.ClusterInfo;
import trino.common.models.Subject;
import trino.envoy.controlplane.spring.service.ClusterHealthService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is an <code>Observable</code> class whose state holds the details of clusters who registered against envoy.
 * Whenever a <i>new</i> cluster is registered, all the observers are notified of the change
 */
@Component
public class ClusterRegistry extends Subject<Map<String, ClusterInfo>> {

    Map<String, ClusterInfo> previousState;

    final ClusterHealthService clusterHealthService;

    public ClusterRegistry(ClusterHealthService clusterHealthService) {
        this.clusterHealthService = clusterHealthService;
        this.previousState = new HashMap<>();
        this.setState(new HashMap<>());
    }

    public void register(String name, String host, Integer port) {
        this.getState().put(name, new ClusterInfo(host, port));
        notifyIfRegisteredClustersChanged();
    }

    public void unRegister(String name) {
        this.getState().remove(name);
        notifyIfRegisteredClustersChanged();
    }

    @Scheduled(cron = "${cluster.registry.cleanup-cron}")
    public void refreshRegistryState() {
        List<String> healthyClusters = clusterHealthService.getState();
        Set<String> inActiveClusters = this.getState().keySet()
                .stream()
                .filter(cluster -> !healthyClusters.contains(cluster))
                .collect(Collectors.toSet());

        // remove all healthy clusters from total registered clusters
        this.getState().keySet().removeAll(inActiveClusters);

        notifyIfRegisteredClustersChanged();
    }

    private void notifyIfRegisteredClustersChanged() {
        if (!this.getState().equals(previousState)) {
            previousState = SerializationUtils.clone((HashMap<String, ClusterInfo>) this.getState());
            notifyObservers();
        }
    }

}
