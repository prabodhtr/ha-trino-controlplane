package trino.envoy.controlplane.spring.controllers.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import trino.envoy.controlplane.spring.service.ClusterSelectorService;

import java.util.List;

@RestController
@RequestMapping("/cache")
@RequiredArgsConstructor
public class ClusterSelectorEndpoint {

    private final ClusterSelectorService cacheService;

    @PostMapping("/")
    public void saveCluster(@RequestParam("queryId") String queryId, @RequestParam("cluster") String cluster){
        cacheService.saveCluster(queryId, cluster);
    }

    /**
     * @param queryId
     * @return
     * <p>Incase the selected cluster is not healthy or is not valid, envoy will auto retry the request as per
     * <a href="https://www.envoyproxy.io/docs/envoy/latest/configuration/http/http_filters/router_filter#x-envoy-max-retries">x-envoy-max-retries</a>
     * passed from downstream</p>
     */
    @GetMapping("/")
    public ResponseEntity<String> getClusterForQuery(@RequestParam(value = "queryId", required = false) String queryId){
        return ResponseEntity.ok(cacheService.getCluster(queryId));
    }

    @GetMapping("/clusters")
    public ResponseEntity<List<String>> availableClusters(){
        return ResponseEntity.ok(cacheService.getAvailableClusters());
    }
}
