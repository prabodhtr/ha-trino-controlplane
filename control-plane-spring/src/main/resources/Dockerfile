FROM openjdk:17-jdk-alpine
MAINTAINER prabodhtr

RUN mkdir -p /etc/controlplane/config

COPY trino-control-plane.jar trino-control-plane.jar

ENTRYPOINT sh -c 'java -Dspring.config.location=/etc/controlplane/config/ -Dspring.profiles.active="$ACTIVE_PROFILE" -jar trino-control-plane.jar'