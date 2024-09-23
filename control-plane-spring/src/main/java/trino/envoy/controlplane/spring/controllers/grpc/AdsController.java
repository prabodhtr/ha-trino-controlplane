package trino.envoy.controlplane.spring.controllers.grpc;

import io.envoyproxy.controlplane.server.V3DiscoveryServer;
import io.envoyproxy.envoy.service.discovery.v3.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class AdsController extends AggregatedDiscoveryServiceGrpc.AggregatedDiscoveryServiceImplBase {

    private final V3DiscoveryServer v3DiscoveryServer;

    public StreamObserver<DiscoveryRequest> streamAggregatedResources(StreamObserver<DiscoveryResponse> responseObserver) {
        return v3DiscoveryServer.getAggregatedDiscoveryServiceImpl().streamAggregatedResources(responseObserver);
    }

    public StreamObserver<DeltaDiscoveryRequest> deltaAggregatedResources(StreamObserver<DeltaDiscoveryResponse> responseObserver) {
        return v3DiscoveryServer.getAggregatedDiscoveryServiceImpl().deltaAggregatedResources(responseObserver);
    }}
