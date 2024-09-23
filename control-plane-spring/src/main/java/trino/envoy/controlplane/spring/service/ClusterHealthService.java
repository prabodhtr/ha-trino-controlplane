package trino.envoy.controlplane.spring.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import trino.common.models.Subject;
import trino.envoy.controlplane.spring.clients.feign.EnvoyAdminClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

/**
 * This is typical java <code>Observable</code> class whose state stores the list of healthy clusters.
 * List of healthy clusters is periodically refreshed with the envoy admin
 * and the observers are notified in case of any change
 */
@Component
public class ClusterHealthService extends Subject<List<String>> {

    private final EnvoyAdminClient envoyAdminClient;
    private final List<String> clustersToFilter;

    public ClusterHealthService(EnvoyAdminClient envoyAdminClient, List<String> clustersToFilter){
        this.envoyAdminClient = envoyAdminClient;
        this.clustersToFilter = clustersToFilter;
        this.setState(new ArrayList<>());
    }

    @Scheduled(cron = "${cluster.selector.refresh-cron}")
    void refreshHealthyClusters(){
        String clustersStats = envoyAdminClient.getClusters();
        List<String> healthyClusters = extractHealthyClustersFromRawClusterStats(clustersStats);

        if(! new HashSet<>(healthyClusters).equals(new HashSet<>(this.getState()))){
            this.setState(healthyClusters);
            notifyObservers();
        }
    }

    /**
     * @param rawStats raw cluster stats read from envoy admin endpoint. <br>
     * <p>ex: <code>cache::100.0.0.100:9000::health_flags::healthy\ncache::192.168.0.108:9000::hostname::my_localhost</code></p>
     * @return List of clusters whose status is healthy
     */
    private List<String> extractHealthyClustersFromRawClusterStats(String rawStats) {
        List<String> healthyClusters = new ArrayList<>();

        StringTokenizer tokenizer = new StringTokenizer(rawStats, "\n");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.contains("health_flags::healthy")) {
                healthyClusters.add(token.split("::")[0]);
            }
        }

        healthyClusters.removeAll(clustersToFilter);
        return healthyClusters;
    }
}
