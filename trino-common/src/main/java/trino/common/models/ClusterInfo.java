package trino.common.models;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
public class ClusterInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    String host;
    Integer port;
    Long lastAnnouncedAt;

    public ClusterInfo(String host, Integer port){
        this.host = host;
        this.port = port;
        this.lastAnnouncedAt = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object object){
        if (object == null || object.getClass() != this.getClass()) {
            return false;
        }

        ClusterInfo newClusterInfo = (ClusterInfo) object;
        return Objects.equals(this.host, newClusterInfo.getHost())
                && Objects.equals(this.port, newClusterInfo.getPort());
    }
}
