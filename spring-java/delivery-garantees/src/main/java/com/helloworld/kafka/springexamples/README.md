# Informe: Garantías de Entrega en Apache Kafka

## Introducción

Ejemplos para demostrar las diferentes garantías de entrega en Apache Kafka utilizando Spring Boot y Spring Kafka. El objetivo es proporcionar ejemplos prácticos que ilustren cómo configurar y utilizar las tres garantías de entrega principales:

1. **AT-MOST-ONCE**: Los mensajes se entregan como máximo una vez, pero pueden perderse.
2. **AT-LEAST-ONCE**: Los mensajes se entregan al menos una vez, pero pueden duplicarse.
3. **EXACTLY-ONCE**: Los mensajes se entregan exactamente una vez, sin pérdidas ni duplicados.

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

## Referencias

1. [Documentación oficial de Apache Kafka](https://kafka.apache.org/documentation/)
2. [Documentación de Spring Kafka](https://docs.spring.io/spring-kafka/docs/current/reference/html/)
3. [Exactly-once Semantics is Possible: Here's How Apache Kafka Does it](https://www.confluent.io/blog/exactly-once-semantics-are-possible-heres-how-apache-kafka-does-it/)
4. [Apache Kafka's Exactly-Once Semantics in Spring Cloud Stream Kafka](https://spring.io/blog/2023/10/16/apache-kafkas-exactly-once-semantics-in-spring-cloud-stream-kafka)
5. [Delivery Semantics for Kafka Consumers](https://learn.conduktor.io/kafka/delivery-semantics-for-kafka-consumers/)
