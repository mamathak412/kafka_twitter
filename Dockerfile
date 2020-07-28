FROM openjdk:8-jre-alpine

RUN mkdir /app

COPY target/BookStore-1.0-SNAPSHOT.jar /app/app.jar

CMD java -jar /app/app.jar