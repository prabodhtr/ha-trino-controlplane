package trino.envoy.controlplane.spring.config;

import io.envoyproxy.controlplane.cache.v3.SimpleCache;
import io.envoyproxy.controlplane.server.V3DiscoveryServer;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import trino.envoy.controlplane.spring.clients.ClientBuilder;
import trino.envoy.controlplane.spring.clients.feign.EnvoyAdminClient;

import static trino.envoy.controlplane.spring.constants.Constants.GROUP;

@Configuration
public class EnvoyConfig {

    @Value("${envoy.admin.url}")
    String envoyAdminUrl;

    @Getter
    @Value("${envoy.listener.port}")
    Integer listenerPort;

    @Bean
    public SimpleCache<String> snapShotCache(){
        return new SimpleCache<>(node -> GROUP);
    }

    @Bean
    public V3DiscoveryServer v3DiscoveryServer(SimpleCache<String> snapShotCache){
        return new V3DiscoveryServer(snapShotCache);
    }

    @Bean
    public EnvoyAdminClient envoyAdminClient(){
        return ClientBuilder.buildClient(EnvoyAdminClient.class, envoyAdminUrl);
    }
}
