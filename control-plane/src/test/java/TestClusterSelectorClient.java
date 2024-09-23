import io.grpc.*;
import trino.controlplane.endpoint.*;

import java.util.concurrent.TimeUnit;

public class TestClusterSelectorClient {

    private final ClusterSelectorGrpc.ClusterSelectorBlockingStub blockingStub;

    // 'channel' here is a Channel, not a ManagedChannel, so it is not this code's responsibility to
    // shut it down.

    // Passing Channels to code makes code easier to test and makes it easier to reuse Channels.
    public TestClusterSelectorClient(Channel channel) {
        this.blockingStub = ClusterSelectorGrpc.newBlockingStub(channel);
    }

    public void getCluster(String queryId){
        SelectClusterRequest.Builder request = SelectClusterRequest.newBuilder();
        if(queryId != null){
            request.setQueryId(queryId);
        }
        SelectClusterResponse response;

        try{
            response = blockingStub.getClusterForQuery(request.build());
        } catch (StatusRuntimeException sre){
            sre.printStackTrace();
            return;
        }

        System.out.println(response.getCluster());
    }

    public void saveCluster(){
        SaveClusterRequest request = SaveClusterRequest.newBuilder().setCluster("abc").setQueryId("1234").build();

        try{
            blockingStub.saveCluster(request);
        } catch (StatusRuntimeException sre){
            sre.printStackTrace();
            return;
        }

        System.out.println("Cluster 'abc' saved");
    }

    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = Grpc.newChannelBuilder("localhost:12345", InsecureChannelCredentials.create()).build();

        try{
            TestClusterSelectorClient client = new TestClusterSelectorClient(channel);
            client.getCluster(null);
            client.saveCluster();
            client.getCluster("1234");
            client.getCluster("");
            client.getCluster("invalid");
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
