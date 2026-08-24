## Executando

### Pré-requisitos

- Java 25
- Maven
- Docker / Docker Compose

### 1. Subir a infraestrutura

Na raiz do projeto:

```bash
docker compose up -d
```

O Docker Compose inicializa:

```text
Kafka
Schema Registry
Redis
Mailpit
```

Também são executados dois serviços de inicialização:

```text
kafka-init
schema-init
```

O `kafka-init` garante a criação automática dos tópicos:

```text
transactions.v1
transactions.v1.DLT
fraud-alerts.v1
```

O `schema-init` registra o schema Avro utilizado pelas transações no Schema Registry.

#### Recriando o ambiente do zero

Caso queira remover os containers, volumes e dados locais antes de iniciar novamente:

```bash
docker compose down -v
docker compose up -d
```

> O comando `docker compose down -v` remove também os volumes do ambiente. Utilize-o apenas quando quiser iniciar a infraestrutura do zero.

### 2. Validar a infraestrutura

Após subir os containers:

```bash
docker compose ps -a
```

O resultado esperado é semelhante a:

```text
fraud-kafka             running / healthy
fraud-redis             running / healthy
fraud-schema-registry   running / healthy
fraud-mailpit           running
fraud-kafka-init        Exited (0)
fraud-schema-init       Exited (0)
```

Os containers `fraud-kafka-init` e `fraud-schema-init` executam apenas tarefas de inicialização e encerram após concluí-las.

Por isso, o status:

```text
Exited (0)
```

é esperado e indica que a inicialização foi concluída com sucesso.

Para confirmar a criação dos tópicos Kafka:

```bash
docker exec -it fraud-kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --list
```

Entre os tópicos disponíveis devem estar:

```text
transactions.v1
transactions.v1.DLT
fraud-alerts.v1
```

### 3. Compilar e executar os testes

O projeto possui um Maven aggregator, portanto todo o monorepo pode ser validado a partir da raiz:

```bash
mvn clean verify
```

O Maven Reactor executará os módulos:

```text
event-contracts
fraud-engine
simulator
```

O comando `verify` também gera o relatório JaCoCo e valida a cobertura mínima configurada.

### 4. Executar o Fraud Engine

Em um terminal:

```bash
cd apps/fraud-engine
mvn spring-boot:run
```

O Fraud Engine iniciará o consumer responsável pelo tópico:

```text
transactions.v1
```

### 5. Publicar transações

Em outro terminal:

```bash
cd apps/simulator
mvn spring-boot:run
```

O simulator publica eventos Avro no tópico:

```text
transactions.v1
```

O fluxo esperado é:

```text
Simulator
    ↓
transactions.v1
    ↓
Fraud Engine
    ↓
Regras de fraude
    │
    ├── fraud-alerts.v1
    │
    └── E-mail
```

### 6. Consumir alertas internos

Para acompanhar os alertas internos publicados pelo Fraud Engine:

```bash
docker exec -it fraud-kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic fraud-alerts.v1 \
  --from-beginning
```

Quando uma transação suspeita for identificada, o alerta deverá aparecer nesse tópico.

### 7. Visualizar notificações externas

Abra o Mailpit no navegador:

```text
http://localhost:8025
```

Os e-mails gerados pelo `EmailCustomerNotificationService` estarão disponíveis na caixa de entrada.

Com isso é possível validar os dois canais:

```text
Alerta interno  → Kafka / fraud-alerts.v1
Alerta externo  → SMTP / Mailpit
```

### 8. Validar a aplicação

Com o Fraud Engine em execução, o health check pode ser consultado em:

```text
http://localhost:8080/actuator/health
```

As métricas Prometheus ficam disponíveis em:

```text
http://localhost:8080/actuator/prometheus
```