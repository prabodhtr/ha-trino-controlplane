package trino.envoy.controlplane.spring.controllers.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import trino.envoy.controlplane.spring.registry.ClusterRegistry;

@RestController
@RequestMapping("/registry")
@RequiredArgsConstructor
public class ClusterRegistryEndpoint {

    private final ClusterRegistry clusterRegistry;

    @PostMapping
    public ResponseEntity<String> register(@RequestParam("name") String name, @RequestParam("host") String host, @RequestParam("port") Integer port) {
        clusterRegistry.register(name, host, port);
        return ResponseEntity.ok(String.format("Registered cluster %s with host %s and port %d", name, host, port));
    }

    @DeleteMapping
    public ResponseEntity<String> unRegister(@RequestParam("name") String name) {
        clusterRegistry.unRegister(name);
        return ResponseEntity.ok(String.format("Un-registered cluster %s", name));
    }
}
