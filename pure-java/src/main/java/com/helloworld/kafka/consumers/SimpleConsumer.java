package com.helloworld.kafka.consumers;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;


import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.kafka.common.serialization.StringDeserializer;


public class SimpleConsumer {
	
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(final String[] args) throws Exception {
        final String topic = "test-topic";
        
        String configFile = "./config/default.properties";
        log.info("Default config file "+configFile);
        if (args.length > 0){
        	configFile = args[0];
            log.info("Customized config file "+configFile);
        }

        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put("key.deserializer", StringDeserializer.class);
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        // Define grupo y offset
    	final String consumerGroup = MethodHandles.lookup().lookupClass()+"Group";
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 5);

        try (final Consumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Arrays.asList(topic));
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> kafkaRecord : records) {
                    String miTopic = kafkaRecord.topic();
                    String key = kafkaRecord.key();
                    String value = kafkaRecord.value();
                    System.out.println(
                            String.format("Consumed event from topic %s: key = %-10s value = %s", miTopic, key, value));
                }
                //System.out.println("Fin del bloque");
            }
        }
    }

}