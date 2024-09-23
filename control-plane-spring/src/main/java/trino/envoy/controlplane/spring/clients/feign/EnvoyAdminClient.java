package trino.envoy.controlplane.spring.clients.feign;

import feign.RequestLine;

public interface EnvoyAdminClient {

    @RequestLine("GET /clusters")
    String getClusters();
}
