package trino.envoy.controlplane.spring.clients;

import feign.Feign;
import feign.okhttp.OkHttpClient;

public class ClientBuilder {

    public static <T> T buildClient(Class<T> clazz, String targetUrl){
        return Feign.builder()
                .client(new OkHttpClient())
                .target(clazz, targetUrl);
    }
}
