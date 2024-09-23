package trino.envoy.controlplane.spring.service;

import com.google.common.collect.Iterators;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import trino.common.models.Observer;
import trino.envoy.controlplane.spring.exceptions.CacheException;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * <p>This is a typical java <code>Observer</code> class that listens for a notification from
 * {@link ClusterHealthService}
 * on change in healthy cluster list, whereupon it updates the iterator that decides query routing</p>
 *
 * <p>It also manages a cache that maps actively running queries against their upstream clusters to make the session
 * <i>sticky</i> throughout the query lifecycle</p>
 */
@Slf4j
@Component
public class ClusterSelectorService implements Observer {

    private final Map<String, String> queryCache;
    private final ClusterHealthService clusterHealthService;

    private Iterator<String> iterator;

    public ClusterSelectorService(ClusterHealthService clusterHealthService) {
        this.clusterHealthService = clusterHealthService;
        this.queryCache = new HashMap<>();
        this.iterator = Iterators.cycle(new ArrayList<>());
    }

    @PostConstruct
    public void subscribe() {
        clusterHealthService.addObserver(this);
    }

    /**
     * Update clusters to route whenever {@link ClusterHealthService} sends a notification informing a change in healthy cluster list
     */
    @Override
    public void update() {
        iterator = Iterators.cycle(clusterHealthService.getState());
    }

    public String getCluster(String queryId) {
        if (queryId == null) {
            return getCluster();
        }
        return Optional.ofNullable(queryCache.get(queryId))
                .orElseThrow(() -> {
                    log.error("Query ID '{}' not found in cache!!", queryId);
                    return new CacheException(String.format("Query ID '%s' not found in cache!!", queryId));
                });
    }

    public String getCluster() {
        try {
            return iterator.next();
        } catch (NoSuchElementException nse) {
            log.error("No clusters found to route query!!");
            throw new CacheException("No clusters found to route query!!");
        }
    }

    public void saveCluster(String queryId, String cluster) {
        queryCache.put(queryId, cluster);
    }

    public List<String> getAvailableClusters(){
        return clusterHealthService.getState();
    }
}
