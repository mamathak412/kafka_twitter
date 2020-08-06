package com.twitter.kafka.services;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

@Service
public class Producer {
	
	private static final Logger logger = LoggerFactory.getLogger(Producer.class);
	private static final String TOPIC = "twitter";
	private String[] filters = {"devops,cicd,jenkins,docker,kubernetes"};
	private String consumerKey = "R3fPqwzoSoEeNyehNqlxzAEnm";
	private String consumerSecret = "YzbiLmyUKtQL4bsKhfYVR4nYXqT0RkTp0NPM4IQjeKzk9Qn4rQ";
	private String token = "1231845131549986816-LCuyythvWqdQrzJUX4Vx8OFLGA2rTV";
	private String secret = "a9LKZeZbRLOWzmYu5WVVSdYVhfDvP5Ri6cKdY3bDLWqvd";
	
	private final KafkaTemplate<String, Object> kafkaTemplate;
	
	private final String topicName = "twitter";
    private final int messagesPerRequest = 10;
    private CountDownLatch latch;
    
    public Producer(
            final KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

	public void sendMessage() {
		//logger.info(String.format("$$ -> Producing message --> %s", message));
		configureTwitterStream(this.kafkaTemplate);
		//this.kafkaTemplate.send(TOPIC, message);
	}
	
	public void configureTwitterStream(KafkaTemplate<String, Object> producer) {
		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
		configurationBuilder.setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerSecret)
				.setOAuthAccessToken(token).setOAuthAccessTokenSecret(secret).setDebugEnabled(true);

		TwitterStream twitterStream = new TwitterStreamFactory(configurationBuilder.build()).getInstance();

		twitterStream.addListener(new StatusListener() {
			public void onStatus(Status status) {
				String statusText = status.getText().toLowerCase();
				producer.send(TOPIC, statusText);
				latch = new CountDownLatch(messagesPerRequest);
		        IntStream.range(0, messagesPerRequest)
		                .forEach(i -> producer.send(topicName, i,
		                		statusText, i));
		        try {
					latch.await(60, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			@Override
			public void onException(Exception ex) {
				ex.printStackTrace();
			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
				logger.info("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());

			}

			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStallWarning(StallWarning warning) {
				logger.info("Got stall warning:" + warning);

			}
		});

		FilterQuery tweetFilterQuery = new FilterQuery(); // See
		tweetFilterQuery.track(filters); // OR on keywords
		tweetFilterQuery.language(new String[] { "en" });
		twitterStream.filter(tweetFilterQuery);
	}

}
