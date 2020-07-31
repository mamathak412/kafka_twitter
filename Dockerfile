FROM openjdk:8-jre-alpine

RUN mkdir /app

COPY target/kafka-twitter-1.0-SNAPSHOT.jar /app/app.jar

CMD java -jar /app/app.ja