package trino.envoy.controlplane.spring.constants;

public class Constants {
    public static final String GROUP = "key";

    public static final String FILTER_HTTP_CONNECTION_MANAGER = "envoy.http_connection_manager";
    public static final String FILTER_ENVOY_ROUTER = "envoy.filters.http.router";
    public static final String FILE_ACCESS_LOG = "envoy.access_loggers.file";

    public static final String X_REQUEST_ID = "x-request-id";

}
