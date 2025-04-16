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

### Sugerencias Adicionales

1. **Optimizar configuración de tópicos**: Ajustar parámetros como particiones, factor de replicación y política de retención según el caso de uso.
2. **Implementar circuit breakers**: Utilizar patrones como circuit breaker para manejar fallos temporales en sistemas externos.
3. **Considerar compactación de logs**: Para casos de uso de tipo clave-valor donde solo importa el último valor.
4. **Utilizar herramientas de observabilidad**: Implementar trazabilidad distribuida para seguir el flujo de mensajes a través de múltiples servicios.
5. **Actualizar regularmente**: Mantenerse al día con las últimas versiones de Kafka y Spring Kafka para beneficiarse de mejoras en rendimiento y fiabilidad.


## Referencias

1. [Documentación oficial de Apache Kafka](https://kafka.apache.org/documentation/)
2. [Documentación de Spring Kafka](https://docs.spring.io/spring-kafka/docs/current/reference/html/)
3. [Exactly-once Semantics is Possible: Here's How Apache Kafka Does it](https://www.confluent.io/blog/exactly-once-semantics-are-possible-heres-how-apache-kafka-does-it/)
4. [Apache Kafka's Exactly-Once Semantics in Spring Cloud Stream Kafka](https://spring.io/blog/2023/10/16/apache-kafkas-exactly-once-semantics-in-spring-cloud-stream-kafka)
5. [Delivery Semantics for Kafka Consumers](https://learn.conduktor.io/kafka/delivery-semantics-for-kafka-consumers/)
