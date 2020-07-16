FROM java:8
WORKDIR /
ADD target/kafka-twitter-1.0-SNAPSHOT.jar kafka-twitter-1.0-SNAPSHOT.jar
EXPOSE 8082
CMD java -cp kafka-twitter-1.0-SNAPSHOT.jar com.twitter.kafka.TwitterProducer
