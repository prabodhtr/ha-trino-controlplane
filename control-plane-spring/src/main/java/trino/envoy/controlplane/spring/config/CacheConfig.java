package trino.envoy.controlplane.spring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class CacheConfig {

    @Bean
    public Map<String, String> queryCache() {
        return new HashMap<>();
    }

    @Bean
    @ConfigurationProperties(prefix = "cluster.selector.filter-out")
    public List<String> clustersToFilter(){
        return new ArrayList<>();
    }
}
