FROM openjdk:17-jdk-alpine
MAINTAINER prabodhtr

RUN mkdir -p /etc/controlplane/config

COPY trino-control-plane.jar trino-control-plane.jar

# appliation.yaml file will be mounted in the spring.config.location while starting docker
ENTRYPOINT sh -c 'java -Dspring.config.location=/etc/controlplane/config/ -Dspring.profiles.active="$ACTIVE_PROFILE" -jar trino-control-plane.jar'