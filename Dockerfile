FROM alpine/git as clone
ARG url
WORKDIR /app
RUN git clone ${url}

FROM maven:3.5-jdk-8-alpine as builder
ARG project 
WORKDIR /app
COPY --from=clone /app/${project} /app
RUN mvn package

FROM openjdk:8-jre-alpine
ARG artifactid
ARG version
ENV artifact ${artifactid}-${version}.jar
WORKDIR /app
ENV MASTERIP=127.0.0.1
ENV ID=0
ENV TZ="Europe/Rome"
COPY --from=builder /app/target/${artifact} /app

CMD /usr/bin/java -jar ${artifact} -m $MASTERIP -id $ID
