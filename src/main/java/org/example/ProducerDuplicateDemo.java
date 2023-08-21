package org.example;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProducerDuplicateDemo {
    private static final Logger logger = Logger.getLogger(ProducerDuplicateDemo.class.getName());

    private final Producer<String, String> producer;
    final String outTopic;

    public ProducerDuplicateDemo(final Producer<String, String> producer,
                                    final String topic) {
        this.producer = producer;
        outTopic = topic;
    }

    public void produce(final String message) {
        final String[] parts = message.split("-");
        final String key, value;
        if (parts.length > 1) {
            key = parts[0];
            value = parts[1];
        } else {
            key = null;
            value = parts[0];
        }
        final ProducerRecord<String, String> producerRecord = new ProducerRecord<>(outTopic, key, value);
        producer.send(producerRecord,
                (recordMetadata, e) -> {
                    if(e != null) {
                        logger.log(Level.SEVERE, "data null");
                    } else {
                        System.out.println("key/value " + key + "/" + value + "\twritten to topic[partition] " + recordMetadata.topic() + "[" + recordMetadata.partition() + "] at offset " + recordMetadata.offset());
                    }
                }
        );
    }

    public void shutdown() {
        producer.close();
    }

    public static Properties loadProperties(String fileName) throws IOException {
        final Properties envProps = new Properties();
        final FileInputStream input = new FileInputStream(fileName);
        envProps.load(input);
        input.close();

        return envProps;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            throw new IllegalArgumentException(
                    "This program takes two arguments: the path to an environment configuration file and" +
                            "the path to the file with records to send");
        }

        final Properties props = ProducerDuplicateDemo.loadProperties(args[0]);

        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        props.put(ProducerConfig.CLIENT_ID_CONFIG, "myApp");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        final String topic = props.getProperty("output.topic.name");
        final Producer<String, String> producer = new KafkaProducer<>(props);
        final ProducerDuplicateDemo producerApp = new ProducerDuplicateDemo(producer, topic);

        String filePath = args[1];
        try {
            List<String> linesToProduce = Files.readAllLines(Paths.get(filePath));
            linesToProduce.stream()
                    .filter(l -> !l.trim().isEmpty())
                    .forEach(producerApp::produce);
            System.out.println("Offsets and timestamps committed in batch from " + filePath);
        } catch (IOException e) {
            System.err.printf("Error reading file %s due to %s %n", filePath, e);
        } finally {
            producerApp.shutdown();
        }
    }
}
