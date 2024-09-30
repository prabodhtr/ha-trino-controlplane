FROM envoyproxy/envoy:v1.31.0

# volume mounted during docker run having envoy.yaml
RUN mkdir -p /etc/envoy

COPY entrypoint.sh /

RUN chmod 500 /entrypoint.sh

RUN apt-get update && \
    apt-get install gettext -y

ENTRYPOINT ["/entrypoint.sh"]