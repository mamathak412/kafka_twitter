FROM openjdk:8-jre-alpine

RUN mkdir /app

RUN apk update && apk add ca-certificates && rm -rf /var/cache/apk/*

COPY ./mycert.crt /usr/local/share/ca-certificates/mycert.crt

RUN update-ca-certificates

COPY target/kafka-twitter-1.0-SNAPSHOT.jar /app/app.jar

CMD java -jar /app/app.jar