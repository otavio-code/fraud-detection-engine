# Ambiente Kafka

O Kafka será utilizado como entrada dos eventos de transação processados.
E deverão ser processados pelo motor de fraude.

O Broker é executado localmente com o docker-compose, ficando disponível em "localhost:9092".

## Tópicos

### transactions.v1

Tópico de entrada das transações que serão analisadas pelo motor de fraude.

Configuração utilizada no ambiente local:

- 3 partições
- 1 fator de replicação

O tópico pode ser criado manualmente com:

    docker exec -it fraud-kafka /opt/kafka/bin/kafka-topics.sh \
      --bootstrap-server localhost:9092 \
      --create \
      --topic transactions.v1 \
      --partitions 3 \
      --replication-factor 1

A chave da mensagem deve representar a conta (`id_conta`). Dessa forma, transações da mesma conta são direcionadas para a mesma partição, preservando a ordenação dentro dela.

### transactions.v1.DLT

Tópico utilizado para armazenar eventos que não puderam ser processados pelo `fraud-engine` mesmo após as tentativas de retry.

No ambiente local, utilizamos a mesma quantidade de partições do tópico de origem:

- Partições: 3
- Fator de replicação: 1

Criação:

    docker exec -it fraud-kafka /opt/kafka/bin/kafka-topics.sh \
      --bootstrap-server localhost:9092 \
      --create \
      --topic transactions.v1.DLT \
      --partitions 3 \
      --replication-factor 1
