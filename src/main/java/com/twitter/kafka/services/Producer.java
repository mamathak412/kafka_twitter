package com.twitter.kafka.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	public void sendMessage() {
		//logger.info(String.format("$$ -> Producing message --> %s", message));
		configureTwitterStream(this.kafkaTemplate);
		//this.kafkaTemplate.send(TOPIC, message);
	}
	
	public void configureTwitterStream(KafkaTemplate<String, String> producer) {
		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
		configurationBuilder.setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerSecret)
				.setOAuthAccessToken(token).setOAuthAccessTokenSecret(secret);

		TwitterStream twitterStream = new TwitterStreamFactory(configurationBuilder.build()).getInstance();

		twitterStream.addListener(new StatusListener() {
			public void onStatus(Status status) {
				String statusText = status.getText().toLowerCase();
				producer.send(TOPIC, statusText);
				/*producer.sendMessage(new ProducerRecord<>("first_topic", null, statusText), new Callback() {
					
					public void onCompletion(RecordMetadata recordMetadata, Exception e) {
						if(e != null) {
							logger.error("Something went wrong" ,e);
						}
					}
				});*/
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
