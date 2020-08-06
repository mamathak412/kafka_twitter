FROM openjdk:8-jre-alpine

RUN mkdir /app

RUN pwd

RUN /usr/lib/jvm/java-1.8-openjdk/bin/keytool -import -alias ALEInternationalCertificate -file ALE-ROOT-CERTIFICATE.cer -keystore /usr/lib/jvm/java-1.8-openjdk/jre/lib/security/cacerts -storepass changeit

RUN update-ca-certificates --fresh

COPY target/kafka-twitter-1.0-SNAPSHOT.jar /app/app.jar

CMD java -jar /app/app.jar