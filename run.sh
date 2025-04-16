#!/bin/bash

# Script para lanzar ejemplos de Kafka
# Autor: Manus
# Fecha: 14/04/2025

# Colores para mejor visualización
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Directorio base
BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$BASE_DIR"

# Función para mostrar ayuda
show_help() {
    echo -e "${BLUE}Sistema de lanzamiento para ejemplos de Kafka${NC}"
    echo ""
    echo "Uso: ./run.sh [comando] [tipo]"
    echo ""
    echo "Comandos disponibles:"
    echo "  start-docker    - Inicia los contenedores Docker de Kafka"
    echo "  stop-docker     - Detiene los contenedores Docker de Kafka"
    echo "  producer        - Ejecuta un productor"
    echo "  consumer        - Ejecuta un consumidor"
    echo "  performance     - Ejecuta una comparación entre modos de garantía de entrega"
    echo "  help            - Muestra esta ayuda"
    echo ""
    echo "Tipos de productores disponibles:"
    echo "  simple          - Productor simple"
    echo "  async           - Productor asíncrono"
    echo "  json-async      - Productor asíncrono con JSON"
    echo "  at-least-once   - Productor con garantía at-least-once"
    echo "  at-most-once    - Productor con garantía at-most-once"
    echo "  exactly-once    - Productor con garantía exactly-once"
    echo ""
    echo "Tipos de consumidores disponibles:"
    echo "  simple          - Consumidor simple"
    echo "  exactly-once    - Consumidor con garantía exactly-once"
    echo "  at-least-once   - Consumidor con garantía at-least-once"
    echo "  at-most-once    - Consumidor con garantía at-most-once"
    echo ""
    echo "Ejemplos:"
    echo "  ./run.sh start-docker"
    echo "  ./run.sh producer simple"
    echo "  ./run.sh consumer at-least-once"
    echo "  ./run.sh stop-docker"
}

# Función para iniciar Docker
start_docker() {
    echo -e "${YELLOW}Iniciando contenedores Docker de Kafka...${NC}"
    docker compose -f docker/compose-kraft.yml up -d
    echo -e "${GREEN}Contenedores Docker iniciados correctamente.${NC}"
    echo -e "${YELLOW}Esperando 10 segundos para que los servicios estén disponibles...${NC}"
    sleep 10
    echo -e "${GREEN}¡Listo! Los servicios de Kafka deberían estar disponibles ahora.${NC}"
}

# Función para detener Docker
stop_docker() {
    echo -e "${YELLOW}Deteniendo contenedores Docker de Kafka...${NC}"
    docker compose -f docker/compose-kraft.yml  down
    echo -e "${GREEN}Contenedores Docker detenidos correctamente.${NC}"
}


# Función para ejecutar el test de rendimiento de las diferentes garantías de entrega
run_performance(){
    echo -e "${YELLOW}Ejecutando productor performance test...${NC}"
    cd "$BASE_DIR/spring-java/delivery-garantees"
    mvn spring-boot:run
}


# Función para ejecutar un productor
run_producer() {
    local type=$1
    
    if [ -z "$type" ]; then
        echo -e "${RED}Error: Debe especificar un tipo de productor.${NC}"
        echo "Ejecute './run.sh help' para ver los tipos disponibles."
        exit 1
    fi
    
    case $type in
        "simple")
            echo -e "${YELLOW}Ejecutando productor simple...${NC}"
            cd "$BASE_DIR/spring-java/simpleproducer"
            mvn spring-boot:run
            ;;
        "async")
            echo -e "${YELLOW}Ejecutando productor asíncrono...${NC}"
            cd "$BASE_DIR/spring-java/asyncproducer"
            mvn spring-boot:run
            ;;
        "json-async")
            echo -e "${YELLOW}Ejecutando productor asíncrono con JSON...${NC}"
            cd "$BASE_DIR/spring-java/jsonasyncproducer"
            mvn spring-boot:run
            ;;
        "at-least-once")
            echo -e "${YELLOW}Ejecutando productor con garantía at-least-once...${NC}"
            cd "$BASE_DIR/spring-java/delivery-garantees"
            mvn spring-boot:run -Dspring-boot.run.main-class=com.helloworld.kafka.springexamples.atleastonce.producer.AtLeastOnceProducer
            ;;
        "at-most-once")
            echo -e "${YELLOW}Ejecutando productor con garantía at-most-once...${NC}"
            cd "$BASE_DIR/spring-java/delivery-garantees"
            mvn spring-boot:run -Dspring-boot.run.main-class=com.helloworld.kafka.springexamples.atmostonce.producer.AtMostOnceProducer
            ;;
        "exactly-once")
            echo -e "${YELLOW}Ejecutando productor con garantía exactly-once...${NC}"
            cd "$BASE_DIR/spring-java/delivery-garantees"
            mvn spring-boot:run -Dspring-boot.run.main-class=com.helloworld.kafka.springexamples.exactlyonce.producer.ExactlyOnceProducer
            ;;
        *)
            echo -e "${RED}Error: Tipo de productor no válido: $type${NC}"
            echo "Ejecute './run.sh help' para ver los tipos disponibles."
            exit 1
            ;;
    esac
}

# Función para ejecutar un consumidor
run_consumer() {
    local type=$1
    
    if [ -z "$type" ]; then
        echo -e "${RED}Error: Debe especificar un tipo de consumidor.${NC}"
        echo "Ejecute './run.sh help' para ver los tipos disponibles."
        exit 1
    fi
    
    case $type in
        "simple")
            echo -e "${YELLOW}Ejecutando consumidor simple...${NC}"
            cd "$BASE_DIR/spring-java/simpleconsumer"
            mvn spring-boot:run
            ;;
        "exactly-once")
            echo -e "${YELLOW}Ejecutando consumidor con garantía exactly-once...${NC}"
            cd "$BASE_DIR/spring-java/delivery-garantees"
            mvn spring-boot:run -Dspring-boot.run.main-class=com.helloworld.kafka.springexamples.exactlyonce.consumer.ExactlyOnceConsumer
            ;;
        "at-least-once")
            echo -e "${YELLOW}Ejecutando consumidor con garantía at-least-once...${NC}"
            cd "$BASE_DIR/spring-java/delivery-garantees"
            mvn spring-boot:run -Dspring-boot.run.main-class=com.helloworld.kafka.springexamples.atleastonce.consumer.AtLeastOnceConsumer
            ;;
        "at-most-once")
            echo -e "${YELLOW}Ejecutando consumidor con garantía at-most-once...${NC}"
            cd "$BASE_DIR/spring-java/delivery-garantees"
            mvn spring-boot:run -Dspring-boot.run.main-class=com.helloworld.kafka.springexamples.atmostonce.consumer.AtMostOnceConsumer
            ;;
        *)
            echo -e "${RED}Error: Tipo de consumidor no válido: $type${NC}"
            echo "Ejecute './run.sh help' para ver los tipos disponibles."
            exit 1
            ;;
    esac
}

# Procesar argumentos
if [ $# -eq 0 ]; then
    show_help
    exit 0
fi

command=$1
shift

case $command in
    "start-docker")
        start_docker
        ;;
    "stop-docker")
        stop_docker
        ;;
    "performance")
        run_performance
        ;;
    "producer")
        run_producer "$1"
        ;;
    "consumer")
        run_consumer "$1"
        ;;
    "help")
        show_help
        ;;
    *)
        echo -e "${RED}Error: Comando no válido: $command${NC}"
        echo "Ejecute './run.sh help' para ver los comandos disponibles."
        exit 1
        ;;
esac

exit 0
