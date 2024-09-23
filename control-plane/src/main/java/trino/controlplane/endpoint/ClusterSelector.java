package trino.controlplane.endpoint;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import trino.controlplane.cache.QueryCache;

public class ClusterSelector extends ClusterSelectorGrpc.ClusterSelectorImplBase {

    static final Logger logger = LoggerFactory.getLogger(ClusterSelector.class.getName());
    QueryCache queryCache;

    public ClusterSelector(QueryCache queryCache){
        this.queryCache = queryCache;
    }

    @Override
    public void getClusterForQuery(SelectClusterRequest request, StreamObserver<SelectClusterResponse> responseObserver){
        SelectClusterResponse response = SelectClusterResponse.newBuilder()
                .setCluster(queryCache.getCluster(request.getQueryId()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void saveCluster(SaveClusterRequest request, StreamObserver<EmptyResponse> responseObserver){

        queryCache.saveCluster(request.getQueryId(), request.getCluster());

        responseObserver.onNext(EmptyResponse.newBuilder().build());
        responseObserver.onCompleted();
    }
}
