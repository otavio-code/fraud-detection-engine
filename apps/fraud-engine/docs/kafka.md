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