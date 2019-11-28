FROM ubuntu
WORKDIR /app
ADD . /app/auctionmechanism

FROM maven:3.5-jdk-8-alpine
WORKDIR /app
COPY --from=0 /app/auctionmechanism /app
RUN mvn package

FROM openjdk:8-jre-alpine
WORKDIR /app
ENV MASTERIP=127.0.0.1
ENV ID=0
ENV TZ="Europe/Rome"
COPY --from=1 /app/target/auctionmechanism-1.0-jar-with-dependencies.jar /app

CMD /usr/bin/java -jar auctionmechanism-1.0-jar-with-dependencies.jar -m $MASTERIP -id $ID
