package com.twitter.kafka;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Properties;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterProducer {

	static Logger logger = Logger.getLogger(TwitterProducer.class);

	private String consumerKey = "R3fPqwzoSoEeNyehNqlxzAEnm";
	private String consumerSecret = "YzbiLmyUKtQL4bsKhfYVR4nYXqT0RkTp0NPM4IQjeKzk9Qn4rQ";
	private String token = "1231845131549986816-LCuyythvWqdQrzJUX4Vx8OFLGA2rTV";
	private String secret = "a9LKZeZbRLOWzmYu5WVVSdYVhfDvP5Ri6cKdY3bDLWqvd";
	private String[] filters = {"devops,cicd,jenkins,docker,kubernetes"};

	public TwitterProducer() {
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();
		trustEveryone();
		new TwitterProducer().run();
	}

	public void run() {
		logger.info("Setup");
		// create Kafka producer
		KafkaProducer<String, String> producer = createKafkaProducer();

		//configure twitter stream
		configureTwitterStream(producer);
		
		//add a shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread(()-> {
			logger.info("Stopping application...");
			logger.info("Closing producer");
			producer.close();
			logger.info("Done..");
		}));

		logger.info("End of application");
	}

	private KafkaProducer<String, String> createKafkaProducer() {
		String bootStrapServers = "127.0.0.1:9092";
		// create Producer properties
		Properties properties = new Properties();
		properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
		properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		
		//create sage Producer
		properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
		properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
		properties.setProperty(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
		properties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");
		
		//high throughput producer (at the expense of a bit of latency and CPU usage)
		properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
		properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "20");
		properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32 * 1024));

		// create the producer
		KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);
		return producer;
	}

	private void configureTwitterStream(KafkaProducer<String, String> producer) {
		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
		configurationBuilder.setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerSecret)
				.setOAuthAccessToken(token).setOAuthAccessTokenSecret(secret);

		TwitterStream twitterStream = new TwitterStreamFactory(configurationBuilder.build()).getInstance();

		twitterStream.addListener(new StatusListener() {
			public void onStatus(Status status) {
				String statusText = status.getText().toLowerCase();
				producer.send(new ProducerRecord<>("first_topic", null, statusText), new Callback() {
					
					public void onCompletion(RecordMetadata recordMetadata, Exception e) {
						if(e != null) {
							logger.error("Something went wrong" ,e);
						}
					}
				});
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

	public static void trustEveryone() {
		try {
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, new X509TrustManager[] { new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}
			} }, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
		} catch (Exception e) { // should never happen
			e.printStackTrace();
		}
	}

}
