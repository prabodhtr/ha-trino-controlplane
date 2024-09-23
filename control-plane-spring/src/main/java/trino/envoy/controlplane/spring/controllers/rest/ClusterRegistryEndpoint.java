package trino.envoy.controlplane.spring.controllers.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import trino.envoy.controlplane.spring.registry.ClusterRegistry;

@RestController
@RequestMapping("/discovery")
@RequiredArgsConstructor
public class ClusterRegistryEndpoint {

    private final ClusterRegistry clusterRegistry;

    @PostMapping("/register")
    public ResponseEntity<String> registerCluster(@RequestParam("name") String name, @RequestParam("host") String host, @RequestParam("port") Integer port){
        clusterRegistry.register(name, host, port);
        return ResponseEntity.ok(String.format("Announced cluster %s with host %s and port %d", name, host, port));
    }
}
