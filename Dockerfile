FROM openjdk:8-jre-alpine

RUN mkdir /app

RUN apk update && apk add ca-certificates && rm -rf /var/cache/apk/*

COPY ./jssecacerts /usr/lib/jvm/java-1.8-openjdk/jre/lib/security

RUN update-ca-certificates

COPY target/kafka-twitter-1.0-SNAPSHOT.jar /app/app.jar

CMD java -jar /app/app.jar