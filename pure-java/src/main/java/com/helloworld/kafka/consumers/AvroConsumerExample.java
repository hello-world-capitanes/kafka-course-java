package com.helloworld.kafka.consumers;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;


public class AvroConsumerExample {
    public static void main(String[] args) {
        // Configuración del consumidor
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "avro-consumer-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroDeserializer");
        props.put("schema.registry.url", "http://localhost:8085"); // URL del Schema Registry
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Crear consumidor
        KafkaConsumer<GenericRecord, GenericRecord> consumer = new KafkaConsumer<>(props);
        final String topic = "test-topic-avro";

        // Suscribirse al topic
        consumer.subscribe(Collections.singletonList(topic));

        // Procesar mensajes
        try {
            while (true) {
                ConsumerRecords<GenericRecord, GenericRecord> records = consumer.poll(Duration.ofMillis(100));
                records.forEach(message -> {
                    System.out.println("Clave: "+ message.key().get("key")+" Nombre: "+ message.value().get("name"));
                });
            }
        } finally {
            consumer.close();
        }
    }
}
