## Spring based envoy control plane for highly available trino clusters

This is a springboot based envoy control plane that hosts cluster and listener discovery services over gRPC. This also acts as a load balancer that routes queries to multiple trino 
clusters depending on their live health, in a round robin fashion, allowing us to resolve the issue of trino's coordinator being a single point of failure and have high availability
