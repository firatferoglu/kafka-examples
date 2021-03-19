package kafka.tutorial2;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author fferoglu
 */
public class TwitterProducer {

    private Logger logger = LoggerFactory.getLogger(TwitterProducer.class);

    private static String BOOTSTRAP_SERVER = "localhost:9092";
    private static String TOPIC = "twitter-tweets";

    public static void main(String[] args) {
        new TwitterProducer().run();
    }

    public void run() {
        KafkaProducer<String, String> producer = createHighThroughputProducer();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("stopping application...");
            logger.info("closing producer...");
            producer.close();
            logger.info("done");
        }));
        createKafkaMessages().forEach(message ->
            producer.send(new ProducerRecord<>(TOPIC, message), (record, exception) -> {
                if (exception != null) {
                    logger.error("The producer gets an exception", exception);
                }
                logger.info("Topic: " + record.topic());
                logger.info("Partiton" + record.partition());
            }));
    }

    public List<String> createKafkaMessages() {
        return IntStream.rangeClosed(1,50)
                .boxed()
                .map(num -> "message-with-snappy" + Integer.toString(num))
                .collect(Collectors.toList());
    }

    public KafkaProducer<String, String> createKafkaProducer() {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return new KafkaProducer<>(properties);
    }

    public KafkaProducer<String, String> createSafeKafkaProducer() {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        properties.setProperty(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
        properties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        return new KafkaProducer<>(properties);
    }

    public KafkaProducer<String, String> createHighThroughputProducer() {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        properties.setProperty(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
        properties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "10000");
        properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(1024*32));
        return new KafkaProducer<>(properties);
    }
}
