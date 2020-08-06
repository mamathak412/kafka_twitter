FROM openjdk:8-jre-alpine

RUN mkdir /app

RUN apk update && apk add ca-certificates && rm -rf /var/cache/apk/*

COPY ./ca.crt /usr/local/share/ca-certificates/ca.crt

COPY ./ca.pem /usr/local/share/ca-certificates/ca.pem

COPY ./key.pem /usr/local/share/ca-certificates/key.pem

RUN update-ca-certificates --fresh

COPY target/kafka-twitter-1.0-SNAPSHOT.jar /app/app.jar

CMD java -jar /app/app.jar