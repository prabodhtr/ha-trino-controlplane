package trino.controlplane.xds;

import com.google.protobuf.Any;
import io.envoyproxy.envoy.config.accesslog.v3.AccessLog;
import io.envoyproxy.envoy.config.core.v3.Address;
import io.envoyproxy.envoy.config.core.v3.DataSource;
import io.envoyproxy.envoy.config.core.v3.SocketAddress;
import io.envoyproxy.envoy.config.listener.v3.Filter;
import io.envoyproxy.envoy.config.listener.v3.FilterChain;
import io.envoyproxy.envoy.config.listener.v3.Listener;
import io.envoyproxy.envoy.config.route.v3.*;
import io.envoyproxy.envoy.extensions.access_loggers.file.v3.FileAccessLog;
import io.envoyproxy.envoy.extensions.filters.http.lua.v3.Lua;
import io.envoyproxy.envoy.extensions.filters.http.lua.v3.LuaPerRoute;
import io.envoyproxy.envoy.extensions.filters.http.router.v3.Router;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpFilter;
import trino.common.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class LdsFactory {

    private final List<Listener> listeners;

    static final String FILTER_HTTP_CONNECTION_MANAGER = "envoy.http_connection_manager";
    static final String FILTER_ENVOY_ROUTER = "envoy.filters.http.router";
    static final String FILE_ACCESS_LOG = "envoy.access_loggers.file";


    private LdsFactory(List<Listener> listeners) {
        this.listeners = listeners;
    }

    public static LdsFactory builder() {
        return new LdsFactory(new ArrayList<>());
    }

    public List<Listener> build() {
        return listeners;
    }

    public LdsFactory addTrinoListener(int port) {
        listeners.add(
                Listener.newBuilder()
                        .setName("trino_listener")
                        .setAddress(Address.newBuilder()
                                .setSocketAddress(SocketAddress.newBuilder()
                                        .setAddress("0.0.0.0")
                                        .setPortValue(port)))
                        .addFilterChains(FilterChain.newBuilder()
                                .addFilters(Filter.newBuilder()
                                        .setName(FILTER_HTTP_CONNECTION_MANAGER)
                                        .setTypedConfig(Any.pack(createConnectionManager()))))
                        .build()
        );
        return this;
    }

    private HttpConnectionManager createConnectionManager() {
        return HttpConnectionManager.newBuilder()
                .setCodecType(HttpConnectionManager.CodecType.AUTO)
                .setStatPrefix("ingress_http")
                .setRouteConfig(RouteConfiguration.newBuilder()
                        .setName("trino_route")
                        .addVirtualHosts(VirtualHost.newBuilder()
                                .setName("backend")
                                .addDomains("*")
                                .addRoutes(Route.newBuilder()
                                        .setMatch(RouteMatch.newBuilder()
                                                .setPrefix("/v1/statement").build())
                                        .setRoute(RouteAction.newBuilder()
                                                .setClusterHeader("X-Cluster-Id").build())
                                        .putTypedPerFilterConfig("envoy_on_request", Any.pack(createLuaPerRoute())))))
                .addAccessLog(AccessLog.newBuilder()
                        .setName(FILE_ACCESS_LOG)
                        .setTypedConfig(Any.pack(createFileAccessLogger())))
                .addHttpFilters(HttpFilter.newBuilder()
                        .setName("envoy_on_request")
                        .setTypedConfig(Any.pack(Lua.newBuilder().build())))
                .addHttpFilters(HttpFilter.newBuilder()
                        .setName(FILTER_ENVOY_ROUTER)
                        .setTypedConfig(Any.pack(Router.newBuilder().build())))
                .build();
    }

    private FileAccessLog createFileAccessLogger() {
        return FileAccessLog.newBuilder()
                .setPath("/var/log/envoy-logs.log").build();
    }

    private LuaPerRoute createLuaPerRoute() {
        return LuaPerRoute.newBuilder()
                .setSourceCode(DataSource.newBuilder().setInlineString(Utils.readFileToString("request.lua")))
                .build();
    }


}
