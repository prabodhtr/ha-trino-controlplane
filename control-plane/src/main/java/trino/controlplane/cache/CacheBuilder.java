package trino.controlplane.cache;

import java.util.HashMap;
import java.util.Map;

public class CacheBuilder {

    private final Map<String, String> cache;

    private CacheBuilder(){
        cache = new HashMap<>();
    }

    public static CacheBuilder newCacheBuilder(){
        return new CacheBuilder();
    }

    public Map<String, String> build(){
        return this.cache;
    }
}
