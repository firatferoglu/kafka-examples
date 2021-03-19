package kafka.tutorial1.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * @author fferoglu
 */
public class  ConsumerDemoAssignSeek {
    private static Logger logger = LoggerFactory.getLogger(ConsumerDemoAssignSeek.class);
    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        TopicPartition partition = new TopicPartition("twitter-topic", 1);
        consumer.assign(Collections.singleton(partition));
        consumer.seek(partition, 10);

        while (true) {
            ConsumerRecords<String,String> records = consumer.poll(Duration.ofMillis(100));
            records.forEach(record -> {
                logger.info("Topic Name: " + record.topic());
                logger.info("Partiton: " + record.partition());
                logger.info("Offset: " + record.offset());
            });

        }
    }
}
