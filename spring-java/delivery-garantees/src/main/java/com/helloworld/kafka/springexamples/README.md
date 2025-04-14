# Informe: Garantías de Entrega en Apache Kafka

## Introducción

Este informe documenta la implementación de ejemplos para demostrar las diferentes garantías de entrega en Apache Kafka utilizando Spring Boot y Spring Kafka. El objetivo es proporcionar ejemplos prácticos que ilustren cómo configurar y utilizar las tres garantías de entrega principales:

1. **AT-MOST-ONCE**: Los mensajes se entregan como máximo una vez, pero pueden perderse.
2. **AT-LEAST-ONCE**: Los mensajes se entregan al menos una vez, pero pueden duplicarse.
3. **EXACTLY-ONCE**: Los mensajes se entregan exactamente una vez, sin pérdidas ni duplicados.

Además, se incluyen ejemplos adicionales con características avanzadas para mejorar la robustez, el rendimiento y la monitorización de las aplicaciones Kafka.

## Garantías de Entrega en Kafka

### AT-MOST-ONCE (Como Máximo Una Vez)

La garantía de entrega at-most-once prioriza el rendimiento sobre la fiabilidad. En este modelo:

- Los mensajes se entregan como máximo una vez, pero pueden perderse.
- Es adecuado para casos donde la pérdida ocasional de datos es aceptable (ej. métricas, logs).
- Ofrece el mayor rendimiento pero la menor fiabilidad.

**Configuración del Productor:**
- `acks=0`: No espera confirmación del broker.
- `retries=0`: No reintenta envíos fallidos.
- `max.in.flight.requests.per.connection=1`: Una solicitud a la vez.
- Tiempos de espera cortos.

**Configuración del Consumidor:**
- `enable.auto.commit=true`: Commit automático de offsets.
- `auto.commit.interval.ms=100`: Commit frecuente.
- `auto.offset.reset=latest`: Comenzar desde el último offset.

### AT-LEAST-ONCE (Al Menos Una Vez)

La garantía de entrega at-least-once prioriza la fiabilidad sobre la exactitud. En este modelo:

- Los mensajes se entregan al menos una vez, pero pueden duplicarse.
- Es adecuado para casos donde no se puede perder ningún dato, pero se pueden manejar duplicados.
- Ofrece un buen equilibrio entre rendimiento y fiabilidad.

**Configuración del Productor:**
- `acks=all`: Espera confirmación del líder y réplicas.
- `retries=Integer.MAX_VALUE`: Reintentos ilimitados.
- `max.in.flight.requests.per.connection=5`: Permite múltiples solicitudes en vuelo.
- `enable.idempotence=false`: No garantiza idempotencia.

**Configuración del Consumidor:**
- `enable.auto.commit=false`: Desactiva commit automático.
- `auto.offset.reset=earliest`: Comienza desde el principio si no hay offset.
- Confirmación manual después del procesamiento.

### EXACTLY-ONCE (Exactamente Una Vez)

La garantía de entrega exactly-once proporciona la máxima fiabilidad y exactitud. En este modelo:

- Los mensajes se entregan exactamente una vez, sin pérdidas ni duplicados.
- Es adecuado para casos críticos donde tanto la pérdida como la duplicación son inaceptables.
- Ofrece la mayor fiabilidad pero el menor rendimiento.

**Configuración del Productor:**
- `enable.idempotence=true`: Garantiza que los mensajes no se dupliquen.
- `acks=all`: Espera confirmación del líder y réplicas.
- `transactional.id=<id>`: Habilita transacciones para garantizar atomicidad.
- `retries=Integer.MAX_VALUE`: Reintentos ilimitados.

**Configuración del Consumidor:**
- `enable.auto.commit=false`: Desactiva commit automático.
- `isolation.level=read_committed`: Solo lee mensajes confirmados.
- `auto.offset.reset=earliest`: Comienza desde el principio si no hay offset.
- Confirmación manual dentro de transacciones.

## Implementación de Ejemplos

### Estructura del Proyecto

El proyecto está organizado en cuatro directorios principales:

1. **at-least-once**: Ejemplos para la garantía de entrega AT-LEAST-ONCE.
2. **at-most-once**: Ejemplos para la garantía de entrega AT-MOST-ONCE.
3. **exactly-once**: Ejemplos para la garantía de entrega EXACTLY-ONCE.
4. **additional-examples**: Ejemplos adicionales con características avanzadas.

### Ejemplo AT-MOST-ONCE

#### Productor (AtMostOnceProducer.java)

```java
// Envío con garantía AT-MOST-ONCE (fire and forget)
template.send("at-most-once-topic", data[0], data[1]);
log.info("Mensaje enviado sin esperar confirmación: clave={}, valor={}", data[0], data[1]);
```

Configuración clave:
```properties
spring.kafka.producer.acks=0
spring.kafka.producer.retries=0
spring.kafka.producer.max.in.flight.requests.per.connection=1
spring.kafka.producer.properties.request.timeout.ms=1000
spring.kafka.producer.properties.delivery.timeout.ms=1500
```

#### Consumidor (AtMostOnceConsumer.java)

```java
@KafkaListener(id = "atMostOnceListener", topics = "at-most-once-topic")
public void listen(
    @Payload String message,
    @Headers Map<String, Object> headers) {
    
    try {
        // Procesamos el mensaje inmediatamente
        // En AT-MOST-ONCE, el offset ya se ha confirmado automáticamente
        // antes de procesar el mensaje
        processMessage(message);
        
    } catch (Exception e) {
        // Si ocurre un error durante el procesamiento, el mensaje ya está confirmado
        // y no se volverá a procesar (se pierde)
        log.error("Error al procesar el mensaje: {}. El mensaje se ha perdido.", e.getMessage());
    }
}
```

Configuración clave:
```properties
spring.kafka.consumer.enable-auto-commit=true
spring.kafka.consumer.auto-commit-interval=100ms
spring.kafka.consumer.auto-offset-reset=latest
spring.kafka.listener.ack-mode=BATCH
```

### Ejemplo AT-LEAST-ONCE

#### Productor (AtLeastOnceProducer.java)

```java
// Envío con garantía AT-LEAST-ONCE
template.send("at-least-once-topic", data[0], data[1])
    .whenComplete((result, ex) -> {
        if (ex == null) {
            log.info("Mensaje enviado con éxito: clave={}, valor={}, offset={}",
                    data[0], data[1], result.getRecordMetadata().offset());
        } else {
            log.error("Error al enviar mensaje: clave={}, valor={}, error={}",
                    data[0], data[1], ex.getMessage());
        }
    });
```

Configuración clave:
```properties
spring.kafka.producer.acks=all
spring.kafka.producer.retries=2147483647
spring.kafka.producer.max.in.flight.requests.per.connection=5
spring.kafka.producer.enable.idempotence=false
```

#### Consumidor (AtLeastOnceConsumer.java)

```java
@KafkaListener(id = "atLeastOnceListener", topics = "at-least-once-topic")
public void listen(
    @Payload String message,
    @Headers Map<String, Object> headers,
    Acknowledgment acknowledgment) {
    
    try {
        // Simulamos procesamiento del mensaje
        processMessage(message);
        
        // Confirmamos manualmente el mensaje después de procesarlo correctamente
        // Esto garantiza at-least-once: si falla antes de confirmar, se volverá a procesar
        acknowledgment.acknowledge();
        
    } catch (Exception e) {
        // Si ocurre un error durante el procesamiento, no confirmamos el mensaje
        // Esto hará que el mensaje se vuelva a procesar en el siguiente poll
        log.error("Error al procesar el mensaje: {}", e.getMessage());
        // No llamamos a acknowledgment.acknowledge() para que se reprocese
    }
}
```

Configuración clave:
```properties
spring.kafka.consumer.enable-auto-commit=false
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.listener.ack-mode=MANUAL
```

### Ejemplo EXACTLY-ONCE

#### Productor (ExactlyOnceProducer.java)

```java
// Envío con garantía EXACTLY-ONCE usando transacciones
template.executeInTransaction(operations -> {
    return operations.send("exactly-once-topic", data[0], data[1])
        .whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Mensaje enviado con éxito en transacción: clave={}, valor={}, offset={}",
                        data[0], data[1], result.getRecordMetadata().offset());
            } else {
                log.error("Error al enviar mensaje en transacción: clave={}, valor={}, error={}",
                        data[0], data[1], ex.getMessage());
            }
        });
});
```

Configuración clave:
```properties
spring.kafka.producer.acks=all
spring.kafka.producer.retries=2147483647
spring.kafka.producer.enable.idempotence=true
spring.kafka.producer.transactional.id=exactly-once-producer-tx
```

#### Consumidor (ExactlyOnceConsumer.java)

```java
@KafkaListener(id = "exactlyOnceListener", topics = "exactly-once-topic")
@Transactional
public void listen(
    @Payload String message,
    @Headers Map<String, Object> headers,
    Acknowledgment acknowledgment) {
    
    try {
        // Procesamos el mensaje dentro de una transacción
        processMessage(message);
        
        // Confirmamos manualmente el mensaje después de procesarlo correctamente
        // La confirmación se realiza dentro de la misma transacción
        acknowledgment.acknowledge();
        
    } catch (Exception e) {
        // Si ocurre un error durante el procesamiento, la transacción se revierte
        // y el mensaje no se confirma, lo que garantiza exactly-once
        log.error("Error al procesar el mensaje: {}. La transacción se revertirá.", e.getMessage());
        throw e; // Relanzamos la excepción para que Spring revierta la transacción
    }
}
```

Configuración clave:
```properties
spring.kafka.consumer.enable-auto-commit=false
spring.kafka.consumer.isolation-level=read_committed
spring.kafka.listener.ack-mode=MANUAL_IMMEDIATE
```

## Ejemplos Adicionales

### EnhancedProducer

Productor con manejo de errores mejorado, reintentos personalizados y monitorización de métricas.

Características principales:
- Medición de latencia de envío
- Reintentos con backoff exponencial
- Registro detallado de métricas

### EnhancedConsumer

Consumidor con manejo de errores mejorado y cola de mensajes muertos (DLQ).

Características principales:
- Validación de mensajes
- Reintentos con contador
- Envío a DLQ después de máximo de reintentos
- Listener específico para mensajes en DLQ

### AdvancedErrorHandlingConfig

Configuración avanzada para manejo de errores y reintentos en consumidores.

Características principales:
- Configuración declarativa de reintentos
- Envío automático a DLQ
- Backoff configurable

### AdvancedTopicConfiguration

Configuración de tópicos optimizados para diferentes casos de uso.

Tipos de tópicos:
- Alta durabilidad
- Alto rendimiento
- Compactación de logs
- Mensajes transitorios

### PerformanceTestingExample

Ejemplo para comparar el rendimiento de las diferentes garantías de entrega.

Características principales:
- Pruebas de rendimiento automatizadas
- Comparación de throughput
- Medición de latencia

## Gestión de Garantías de Entrega

### Cómo Gestionar AT-MOST-ONCE

**En el Productor:**
1. Configurar `acks=0` para no esperar confirmación del broker.
2. Configurar `retries=0` para no reintentar envíos fallidos.
3. Usar tiempos de espera cortos para fallar rápidamente.
4. No utilizar callbacks para verificar el resultado del envío.

**En el Consumidor:**
1. Configurar `enable.auto.commit=true` para commit automático de offsets.
2. Configurar `auto.commit.interval.ms` con un valor bajo para commit frecuente.
3. Usar `auto.offset.reset=latest` para comenzar desde el último offset.
4. No utilizar confirmación manual de mensajes.

**Consideraciones:**
- Adecuado para datos no críticos donde la pérdida es aceptable.
- Ofrece el mayor rendimiento pero la menor fiabilidad.
- Ejemplos: logs, métricas, datos de telemetría.

### Cómo Gestionar AT-LEAST-ONCE

**En el Productor:**
1. Configurar `acks=all` para esperar confirmación del líder y réplicas.
2. Configurar `retries` con un valor alto para reintentar envíos fallidos.
3. Usar callbacks para verificar el resultado del envío y manejar errores.
4. Considerar reintentos personalizados para casos críticos.

**En el Consumidor:**
1. Configurar `enable.auto.commit=false` para desactivar commit automático.
2. Usar `auto.offset.reset=earliest` para comenzar desde el principio si no hay offset.
3. Implementar confirmación manual después del procesamiento exitoso.
4. Manejar excepciones sin confirmar offsets para permitir reprocesamiento.

**Consideraciones:**
- Adecuado para datos importantes donde no se puede perder información.
- Requiere idempotencia en el procesamiento para manejar duplicados.
- Ejemplos: procesamiento de pagos, eventos de negocio, actualizaciones de inventario.

### Cómo Gestionar EXACTLY-ONCE

**En el Productor:**
1. Configurar `enable.idempotence=true` para garantizar que los mensajes no se dupliquen.
2. Configurar `transactional.id` con un valor único para habilitar transacciones.
3. Usar `template.executeInTransaction()` para enviar mensajes dentro de transacciones.
4. Configurar `acks=all` y `retries` con valor alto para máxima fiabilidad.

**En el Consumidor:**
1. Configurar `enable.auto.commit=false` para desactivar commit automático.
2. Configurar `isolation.level=read_committed` para leer solo mensajes confirmados.
3. Usar la anotación `@Transactional` en los métodos de listener.
4. Implementar confirmación manual dentro de la transacción.
5. Propagar excepciones para revertir la transacción en caso de error.

**Consideraciones:**
- Adecuado para datos críticos donde tanto la pérdida como la duplicación son inaceptables.
- Tiene un impacto en el rendimiento debido al overhead de transacciones.
- Requiere una configuración más compleja y cuidadosa.
- Ejemplos: transacciones financieras, actualizaciones de estado críticas, procesamiento de eventos ordenados.

## Conclusiones y Recomendaciones

### Selección de Garantía de Entrega

La elección de la garantía de entrega adecuada depende de los requisitos específicos de la aplicación:

1. **AT-MOST-ONCE**: Cuando se prioriza el rendimiento y la pérdida ocasional de datos es aceptable.
2. **AT-LEAST-ONCE**: Cuando no se puede perder ningún dato y se pueden manejar duplicados.
3. **EXACTLY-ONCE**: Cuando tanto la pérdida como la duplicación de datos son inaceptables.

### Recomendaciones Generales

1. **Comenzar con AT-LEAST-ONCE**: Es un buen punto de partida para la mayoría de las aplicaciones, ofreciendo un equilibrio entre rendimiento y fiabilidad.
2. **Implementar idempotencia**: Diseñar consumidores idempotentes que puedan manejar duplicados, independientemente de la garantía de entrega.
3. **Monitorizar métricas**: Implementar monitorización para detectar problemas como reintentos excesivos, latencia alta o mensajes no procesados.
4. **Utilizar DLQ**: Implementar colas de mensajes muertos para manejar mensajes que no se pueden procesar después de múltiples intentos.
5. **Probar escenarios de fallo**: Simular fallos en productores, consumidores y brokers para verificar que las garantías de entrega funcionan como se espera.

### Sugerencias Adicionales

1. **Optimizar configuración de tópicos**: Ajustar parámetros como particiones, factor de replicación y política de retención según el caso de uso.
2. **Implementar circuit breakers**: Utilizar patrones como circuit breaker para manejar fallos temporales en sistemas externos.
3. **Considerar compactación de logs**: Para casos de uso de tipo clave-valor donde solo importa el último valor.
4. **Utilizar herramientas de observabilidad**: Implementar trazabilidad distribuida para seguir el flujo de mensajes a través de múltiples servicios.
5. **Actualizar regularmente**: Mantenerse al día con las últimas versiones de Kafka y Spring Kafka para beneficiarse de mejoras en rendimiento y fiabilidad.

## Cambios Realizados

1. **Creación de ejemplos para AT-LEAST-ONCE**:
   - Implementación de productor con confirmación de envío
   - Implementación de consumidor con confirmación manual
   - Configuración optimizada para garantizar entrega sin pérdidas

2. **Creación de ejemplos para AT-MOST-ONCE**:
   - Implementación de productor sin espera de confirmación
   - Implementación de consumidor con commit automático
   - Configuración optimizada para máximo rendimiento

3. **Creación de ejemplos para EXACTLY-ONCE**:
   - Implementación de productor con transacciones
   - Implementación de consumidor transaccional
   - Configuración para garantizar procesamiento exactamente una vez

4. **Ejemplos adicionales**:
   - Implementación de productor mejorado con monitorización
   - Implementación de consumidor con DLQ
   - Configuración avanzada de manejo de errores
   - Configuración optimizada de tópicos
   - Ejemplo de pruebas de rendimiento

## Motivos de los Cambios

1. **Demostrar diferentes garantías de entrega**:
   - Proporcionar ejemplos claros y prácticos de cada garantía
   - Ilustrar las diferencias en configuración y comportamiento
   - Facilitar la comprensión de los conceptos teóricos

2. **Mejorar la robustez de las aplicaciones**:
   - Implementar manejo de errores avanzado
   - Proporcionar ejemplos de reintentos y backoff
   - Demostrar el uso de DLQ para mensajes problemáticos

3. **Optimizar el rendimiento**:
   - Proporcionar configuraciones optimizadas para diferentes casos de uso
   - Demostrar el impacto de diferentes configuraciones en el rendimiento
   - Facilitar la selección de la configuración adecuada según los requisitos

4. **Facilitar la monitorización y observabilidad**:
   - Implementar registro detallado de eventos
   - Proporcionar ejemplos de métricas de rendimiento
   - Facilitar la detección y diagnóstico de problemas

## Referencias

1. [Documentación oficial de Apache Kafka](https://kafka.apache.org/documentation/)
2. [Documentación de Spring Kafka](https://docs.spring.io/spring-kafka/docs/current/reference/html/)
3. [Exactly-once Semantics is Possible: Here's How Apache Kafka Does it](https://www.confluent.io/blog/exactly-once-semantics-are-possible-heres-how-apache-kafka-does-it/)
4. [Apache Kafka's Exactly-Once Semantics in Spring Cloud Stream Kafka](https://spring.io/blog/2023/10/16/apache-kafkas-exactly-once-semantics-in-spring-cloud-stream-kafka)
5. [Delivery Semantics for Kafka Consumers](https://learn.conduktor.io/kafka/delivery-semantics-for-kafka-consumers/)
