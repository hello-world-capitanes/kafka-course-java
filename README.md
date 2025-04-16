# Instrucciones para ejecutar los ejemplos de Kafka

Este documento proporciona instrucciones sobre cómo utilizar el sistema de lanzamiento para los ejemplos de Kafka.

## Requisitos previos

- Docker y Docker Compose instalados
- Maven instalado
- Java 8 o superior instalado

## Estructura del proyecto

El proyecto contiene varios ejemplos de productores y consumidores de Kafka, tanto con Spring como con Java puro:

- **spring-java/**: Ejemplos usando Spring Framework
  - **simpleproducer/**: Productor simple
  - **simpleconsumer/**: Consumidor simple
  - **asyncproducer/**: Productor asíncrono
  - **jsonasyncproducer/**: Productor asíncrono con JSON
  - **exactlyonceconsumer/**: Consumidor con garantía exactly-once
  - **delivery-garantees/**: Ejemplos de garantías de entrega
    - **at-least-once/**: Garantía at-least-once
    - **at-most-once/**: Garantía at-most-once
    - **exactly-once/**: Garantía exactly-once

- **pure-java/**: Ejemplos usando Java puro (sin Spring)

## Sistema de lanzamiento

Se ha implementado un script `run.sh` en la raíz del proyecto para facilitar la ejecución de los ejemplos. Este script permite:

1. Iniciar y detener los contenedores Docker de Kafka
2. Ejecutar diferentes tipos de productores
3. Ejecutar diferentes tipos de consumidores

### Comandos disponibles

```bash
# Mostrar ayuda
./run.sh help

# Iniciar contenedores Docker
./run.sh start-docker

# Detener contenedores Docker
./run.sh stop-docker

# Ejecutar un productor
./run.sh producer [tipo]

# Ejecutar un consumidor
./run.sh consumer [tipo]
```

### Tipos de productores disponibles

- `simple`: Productor simple
- `async`: Productor asíncrono
- `json-async`: Productor asíncrono con JSON
- `at-least-once`: Productor con garantía at-least-once
- `at-most-once`: Productor con garantía at-most-once
- `exactly-once`: Productor con garantía exactly-once

### Tipos de consumidores disponibles

- `simple`: Consumidor simple
- `exactly-once`: Consumidor con garantía exactly-once
- `at-least-once`: Consumidor con garantía at-least-once
- `at-most-once`: Consumidor con garantía at-most-once

### Ejemplos de uso

```bash
# Iniciar los contenedores Docker
./run.sh start-docker

# Ejecutar un productor simple
./run.sh producer simple

# Ejecutar un consumidor con garantía at-least-once
./run.sh consumer at-least-once

# Detener los contenedores Docker
./run.sh stop-docker
```

## Cambios realizados

1. **Unificación de la configuración Docker**: Todos los ejemplos ahora utilizan la configuración definida en `spring-java/docker-compose.yml`.

2. **Externalización de propiedades**: Las propiedades que estaban hardcodeadas en los archivos Java ahora se han movido a archivos de configuración.

3. **Estandarización de proyectos**: Los proyectos en `delivery-garantees` se han estandarizado para seguir la misma estructura que el resto de ejemplos.

4. **Sistema de lanzamiento**: Se ha implementado un script `run.sh` para facilitar la ejecución de los ejemplos.

## Notas adicionales

- Todos los ejemplos utilizan el mismo broker Kafka que se inicia con Docker Compose.
- Para alternar entre diferentes ejemplos, simplemente detenga la aplicación actual (Ctrl+C) y ejecute otro ejemplo con el script `run.sh`.
- Los productores esperan entrada del usuario en formato `clave:valor`.
