import io.grpc.*;
import trino.controlplane.endpoint.ClusterHBRequest;
import trino.controlplane.endpoint.HBResponse;
import trino.controlplane.endpoint.HeartBeatListenerGrpc;

import java.util.concurrent.TimeUnit;

public class TestClusterListenerClient {

    private final HeartBeatListenerGrpc.HeartBeatListenerBlockingStub blockingStub;


    // 'channel' here is a Channel, not a ManagedChannel, so it is not this code's responsibility to
    // shut it down.

    // Passing Channels to code makes code easier to test and makes it easier to reuse Channels.
    public TestClusterListenerClient(Channel channel) {
        this.blockingStub = HeartBeatListenerGrpc.newBlockingStub(channel);
    }

    public void sendHB(){
        ClusterHBRequest request = ClusterHBRequest.newBuilder().setClusterHost("trino_1").setClusterPort(8080).build();
        HBResponse response;

        try{
            response = blockingStub.isAlive(request);
        } catch (StatusRuntimeException sre){
            sre.printStackTrace();
            return;
        }

        System.out.println(response.getMessage());
    }

    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = Grpc.newChannelBuilder("localhost:12345", InsecureChannelCredentials.create()).build();

        try{
            TestClusterListenerClient client = new TestClusterListenerClient(channel);
            client.sendHB();
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
