package trino.controlplane.endpoint;

import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import trino.common.models.Observable;
import trino.common.models.Observer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClusterListener extends HeartBeatListenerGrpc.HeartBeatListenerImplBase implements Observable {

    static final Logger logger = LoggerFactory.getLogger(ClusterListener.class.getName());

    Map<String, Integer> currentState;
    Map<String, Integer> snapshotState;
    List<Observer> observers;

    public Map<String, Integer> getCurrentState() {
        return currentState;
    }

    public ClusterListener() {
        observers = new ArrayList<>();
        currentState = new HashMap<>();
        snapshotState = new HashMap<>();
    }

    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        observers.forEach(observer -> observer.update());
    }

    @Override
    public void isAlive(ClusterHBRequest req, StreamObserver<HBResponse> responseObserver) {
        HBResponse reply = HBResponse.newBuilder().setMessage("Hello!").build();
        currentState.put(req.getClusterHost(), req.getClusterPort());
        notifyIfRequired();

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    private void notifyIfRequired() {
        if (!currentState.equals(snapshotState)) {
            snapshotState = SerializationUtils.clone((HashMap<String, Integer>) currentState);
            notifyObservers();
        }
    }


}
