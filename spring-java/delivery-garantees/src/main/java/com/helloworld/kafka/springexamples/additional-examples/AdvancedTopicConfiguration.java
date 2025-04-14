package com.helloworld.kafka.springexamples.additional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

/**
 * Ejemplo de configuración avanzada de tópicos Kafka
 * Este ejemplo muestra cómo configurar tópicos con diferentes configuraciones
 * para optimizar el rendimiento y la fiabilidad según el caso de uso.
 */
@SpringBootApplication
@EnableKafka
public class AdvancedTopicConfiguration {

    public static void main(String[] args) {
        SpringApplication.run(AdvancedTopicConfiguration.class, args);
    }

    /**
     * Configuración de propiedades globales para tópicos
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        // Configuración global para todos los tópicos creados por esta aplicación
        configs.put("cleanup.policy", "delete"); // Política de limpieza: delete o compact
        configs.put("delete.retention.ms", "86400000"); // Retención de segmentos borrados: 1 día
        return new KafkaAdmin(configs);
    }

    /**
     * Tópico optimizado para alta disponibilidad y durabilidad
     * Adecuado para datos críticos que no pueden perderse
     */
    @Bean
    public NewTopic highDurabilityTopic() {
        return TopicBuilder.name("high-durability-topic")
                .partitions(6)
                .replicas(3) // Alta replicación para mayor durabilidad
                .config("min.insync.replicas", "2") // Mínimo de réplicas sincronizadas
                .config("unclean.leader.election.enable", "false") // Evitar elección de líder no sincronizado
                .config("retention.ms", "604800000") // Retención de 7 días
                .build();
    }

    /**
     * Tópico optimizado para alto rendimiento
     * Adecuado para datos de alta velocidad donde se puede tolerar cierta pérdida
     */
    @Bean
    public NewTopic highThroughputTopic() {
        return TopicBuilder.name("high-throughput-topic")
                .partitions(24) // Muchas particiones para paralelismo
                .replicas(2) // Menos réplicas para mayor rendimiento
                .config("min.insync.replicas", "1") // Mínimo de réplicas sincronizadas
                .config("compression.type", "lz4") // Compresión para reducir uso de red
                .config("retention.ms", "86400000") // Retención de 1 día
                .build();
    }

    /**
     * Tópico optimizado para compactación de logs
     * Adecuado para datos de tipo clave-valor donde solo importa el último valor
     */
    @Bean
    public NewTopic compactedTopic() {
        return TopicBuilder.name("compacted-topic")
                .partitions(6)
                .replicas(3)
                .config("cleanup.policy", "compact") // Política de compactación
                .config("min.compaction.lag.ms", "3600000") // Esperar 1 hora antes de compactar
                .config("max.compaction.lag.ms", "86400000") // Compactar después de 1 día máximo
                .config("delete.retention.ms", "86400000") // Retención después de borrado
                .config("segment.ms", "86400000") // Tamaño de segmento
                .build();
    }

    /**
     * Tópico para mensajes transitorios
     * Adecuado para datos efímeros como métricas o logs temporales
     */
    @Bean
    public NewTopic transientTopic() {
        return TopicBuilder.name("transient-topic")
                .partitions(12)
                .replicas(2)
                .config("retention.ms", "3600000") // Retención corta de 1 hora
                .config("segment.bytes", "107374182") // Segmentos más pequeños (100MB)
                .config("compression.type", "zstd") // Compresión agresiva
                .build();
    }
}
