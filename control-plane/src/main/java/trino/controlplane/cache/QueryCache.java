package trino.controlplane.cache;

import com.google.common.base.Strings;
import com.google.common.collect.Iterators;
import trino.common.models.Observer;
import trino.controlplane.endpoint.ClusterListener;

import java.util.*;

public class QueryCache implements Observer {
    
    List<String> clusters;
    Iterator<String> iterator;
    Map<String, String> cache;
    ClusterListener clusterListener;

    public QueryCache(ClusterListener subject){
        clusters = new ArrayList<>();
        iterator = Iterators.cycle(clusters);
        cache = CacheBuilder.newCacheBuilder().build();
        clusterListener = subject;

        clusterListener.addObserver(this);
    }
    
    @Override
    public void update() {
        clusters = new ArrayList<>(clusterListener.getCurrentState().keySet());
        iterator = Iterators.cycle(clusters);
    }

    public String getCluster(String queryId) {
        if (Strings.isNullOrEmpty(queryId)) {
            return getCluster();
        }

        return Optional.ofNullable(cache.get(queryId))
                .orElseThrow(() -> new RuntimeException(String.format("Query ID '%s' not found in cache!!", queryId)));
    }

    public String getCluster() {
        try {
            return iterator.next();
        } catch (NoSuchElementException nse){
            throw new NoSuchElementException("No clusters found to route query!!");
        }
    }

    public void saveCluster(String queryId, String cluster) {
        cache.put(queryId, cluster);
    }
}
