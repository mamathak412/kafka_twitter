FROM openjdk:8-jre-alpine

RUN mkdir /app

COPY ./cacerts /usr/lib/jvm/java-1.8-openjdk/jre/lib/security

COPY target/kafka-twitter-1.0-SNAPSHOT.jar /app/app.jar

CMD java -jar /app/app.jar