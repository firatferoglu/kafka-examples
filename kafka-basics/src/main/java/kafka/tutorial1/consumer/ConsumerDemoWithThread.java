package kafka.tutorial1.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * @author fferoglu
 */
public class ConsumerDemoWithThread {
    public static final Logger logger = LoggerFactory.getLogger(ConsumerDemoWithThread.class);

    public static void main(String[] args) {
        new ConsumerDemoWithThread().run();
    }

    private void run() {
        String BOOTSTRAP_SERVER = "localhost:9092";
        String TOPIC = "first_topic";
        String CONSUMER_GROUP = "my-sixth-app";

        CountDownLatch latch = new CountDownLatch(1);

        logger.info("Creating the consumer thread");
        ConsumerRunnable consumerRunnable = new ConsumerRunnable(BOOTSTRAP_SERVER, CONSUMER_GROUP, TOPIC, latch);
        Thread thread = new Thread(consumerRunnable);
        thread.start();

        Runtime.getRuntime().addShutdownHook(new Thread( () -> {
            logger.info("Caught shotdown hook");
            consumerRunnable.shutdown();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.info("Application has exited");
        }));

        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("Application got interrupted", e);
        } finally {
            logger.info("Application is closing");
        }
    }

    public class ConsumerRunnable implements Runnable {
        private final Logger logger = LoggerFactory.getLogger(ConsumerRunnable.class);

        private CountDownLatch latch;
        private KafkaConsumer<String, String> consumer;

        public ConsumerRunnable(String bootstrapServer, String groupId, String topic, CountDownLatch latch) {
            Properties properties = new Properties();
            properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
            properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            consumer = new KafkaConsumer<>(properties);
            consumer.subscribe(Collections.singleton(topic));
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                    for (ConsumerRecord<String, String> record: records) {
                        logger.info("Key: " + record.key() + " --- Topic" + record.topic());
                        logger.info("Partition: " + record.partition() + " --- Offset: " + record.offset());
                    }
                }
            }catch (WakeupException e) {
                logger.info("Received shutdown signal!");
            } finally {
                consumer.close();
                latch.countDown();
            }
        }

        public void shutdown() {
            // interrupt consumer.poll() and it'll throw WakeUpException
            consumer.wakeup();
        }
    }
}
