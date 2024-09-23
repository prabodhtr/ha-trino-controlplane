package trino.envoy.controlplane.spring.exceptions;

public class CacheException extends RuntimeException {

    public CacheException(){}

    public CacheException(String message){
        super(message);
    }

    public CacheException(String message, Throwable cause) {
        super(message, cause);
    }


}
