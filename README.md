# Fraud Detection Engine

Motor de detecção de transações suspeitas em tempo real desenvolvido com **Java, Spring Boot, Apache Kafka e Redis**.

O projeto recebe eventos de transações via Kafka, aplica regras de fraude, gera alertas internos, envia notificações externas e encaminha falhas não recuperáveis para uma Dead Letter Topic (DLT).

## Arquitetura

```text
┌─────────────┐       ┌─────────────────┐       ┌──────────────────────┐
│  Simulator  │──────▶│ transactions.v1 │──────▶│     Fraud Engine     │
└─────────────┘       └─────────────────┘       └──────────┬───────────┘
                                                          │
                         ┌────────────────┬────────────────┼────────────────┐
                         │                │                │                │
                         ▼                ▼                ▼                ▼
                  ┌────────────┐   ┌─────────────┐  ┌─────────────┐  ┌──────────────┐
                  │   Redis    │   │   Regras    │  │  Métricas   │  │   Alertas    │
                  │            │   │             │  │             │  │              │
                  │Idempotência│   │ Valor alto  │  │ Micrometer  │  │ Kafka + SMTP │
                  │ Velocidade │   │ Velocidade  │  │  Actuator   │  │              │
                  └────────────┘   │  Horário    │  └─────────────┘  └──────┬───────┘
                                   │  incomum    │                          │
                                   └──────┬──────┘                    ┌─────┴─────┐
                                          │                            │           │
                                   erro após retries                   ▼           ▼
                                          │                     fraud-alerts.v1  E-mail
                                          ▼
                               ┌─────────────────────┐
                               │ transactions.v1.DLT │
                               └─────────────────────┘
```

O repositório está organizado como um monorepo:

```text
fraud-detection-engine/
├── .github/
│   └── workflows/
│       └── ci.yml
├── apps/
│   ├── fraud-engine/       # Consumer e motor de fraude
│   └── simulator/          # Producer para testes
├── libs/
│   └── event-contracts/    # Contratos Avro
├── schemas/                # Schemas dos eventos
├── scripts/                # Scripts de inicialização
├── docker-compose.yml
└── pom.xml                 # Maven aggregator
```

## Tópicos Kafka

A aplicação utiliza os seguintes tópicos:

| Tópico | Uso |
|---|---|
| `transactions.v1` | Entrada das transações |
| `transactions.v1.DLT` | Eventos que falharam após as tentativas de processamento |
| `fraud-alerts.v1` | Alertas internos de transações suspeitas |

Os tópicos são criados automaticamente pelo serviço `kafka-init` durante a inicialização da infraestrutura com Docker Compose.

## Regras implementadas

| Regra | Identificador | Descrição |
|---|---|---|
| **Transação de valor alto** | `TRANSACAO_VALOR_ALTO` | Detecta transações acima do limite configurado |
| **Muitas transações em curto período** | `MUITAS_TRANSACOES_CURTO_PERIODO` | Detecta muitas transações da mesma conta dentro de uma janela de tempo |
| **Transação em horário incomum** | `TRANSACAO_HORARIO_INCOMUM` | Detecta transações realizadas em horários considerados incomuns |

As principais configurações podem ser alteradas através do `application.properties`:

```properties
fraud.rules.high-value.limit=20000.00

fraud.rules.velocity.max-transactions=5
fraud.rules.velocity.window=60s

fraud.rules.unusual-hour.start=0
fraud.rules.unusual-hour.end=5
fraud.rules.unusual-hour.zone=America/Sao_Paulo
```

O limite da regra **Transação de valor alto** também pode ser alterado em runtime:

```http
PUT /admin/regras/transacao-valor-alto/limite
Content-Type: application/json

{
  "limite": 25000.00
}
```

Valores `null`, iguais a `0` ou negativos são rejeitados com `HTTP 400`.

## Alertas

Quando uma transação é identificada como suspeita, o `FraudAlertService` cria um alerta contendo os dados da transação e as regras acionadas.

O alerta é enviado para dois canais:

```text
                    FraudAlertService
                           │
              ┌────────────┴────────────┐
              │                         │
              ▼                         ▼
   InternalAlertPublisher    CustomerNotificationService
              │                         │
              ▼                         ▼
 KafkaInternalAlertPublisher EmailCustomerNotificationService
              │                         │
              ▼                         ▼
      fraud-alerts.v1               SMTP / E-mail
```

### Alerta interno

O `KafkaInternalAlertPublisher` publica o alerta no tópico:

```text
fraud-alerts.v1
```

Esse tópico pode ser consumido por outros sistemas internos interessados em transações suspeitas.

Uma única transação pode acionar várias regras, mas gera um único alerta contendo todas as regras identificadas.

Exemplo:

```json
{
  "idAlerta": "7df34b35-9a52-4af7-a48a-53a3fc73af24",
  "idEvento": "evento-001",
  "idTransacao": "transacao-001",
  "idCliente": "cliente-001",
  "regras": [
    "TRANSACAO_VALOR_ALTO",
    "TRANSACAO_HORARIO_INCOMUM"
  ],
  "dataHora": "2026-08-24T10:00:00Z"
}
```

### Notificação externa

O `EmailCustomerNotificationService` envia uma notificação por e-mail utilizando SMTP.

No ambiente local é utilizado o **Mailpit**, permitindo testar o envio de e-mails sem depender de um provedor externo.

Exemplo de notificação:

```text
Assunto: Alerta de transação suspeita

Uma transação suspeita foi identificada.

Id do alerta: 7df34b35-9a52-4af7-a48a-53a3fc73af24
Id do evento: evento-001
Id da transação: transacao-001
Id do cliente: cliente-001
Regras identificadas: TRANSACAO_VALOR_ALTO, TRANSACAO_HORARIO_INCOMUM
Data/hora: 2026-08-24T10:00:00Z
```

A interface do Mailpit fica disponível em:

```text
http://localhost:8025
```

## Idempotência e tratamento de falhas

O Redis é utilizado para impedir que o mesmo evento seja processado mais de uma vez.

Ao receber uma transação, o Fraud Engine tenta adquirir o processamento utilizando o `idEvento`.

A tentativa pode retornar:

- `ADQUIRIDO`: o evento foi adquirido e pode ser processado;
- `PROCESSANDO`: o evento já está sendo processado;
- `PROCESSADO`: o evento já foi processado anteriormente.

Quando um evento é adquirido, o Redis registra o estado:

```text
PROCESSANDO
```

Após o processamento ser concluído com sucesso, o estado é alterado para:

```text
PROCESSADO
```

`ADQUIRIDO` representa o resultado da tentativa de aquisição e não um estado armazenado no Redis.

Se ocorrer uma falha durante o processamento, o controle de idempotência é liberado para permitir uma nova tentativa.

Para falhas no consumo Kafka, o Fraud Engine realiza **3 retries com intervalo de 2 segundos**. Caso o processamento continue falhando, o evento é enviado para:

```text
transactions.v1.DLT
```

## Observabilidade

A aplicação utiliza **Micrometer + Spring Boot Actuator**.

São coletadas métricas para:

- transações processadas;
- transações suspeitas;
- suspeitas por regra;
- eventos enviados para DLT;
- tempo de processamento.

Endpoints disponíveis:

```text
GET /actuator/health
GET /actuator/metrics
GET /actuator/prometheus
```

## CI e cobertura de testes

O projeto possui um único workflow de integração contínua no GitHub Actions:

```text
.github/workflows/ci.yml
```

O workflow possui três jobs:

```text
┌─────────┐
│  Build  │───┐
└─────────┘   │
              ├──▶ Coverage
┌─────────┐   │
│  Tests  │───┘
└─────────┘
```

`Build` e `Tests` são executados em paralelo. O job `Coverage` é executado somente após a conclusão com sucesso dos dois jobs anteriores.

| Job | Responsabilidade |
|---|---|
| **Build** | Compila e empacota o monorepo sem executar os testes |
| **Tests** | Executa a suíte automatizada de testes |
| **Coverage** | Gera o relatório JaCoCo e valida a cobertura mínima |

A validação de cobertura utiliza **JaCoCo** e exige no mínimo:

```text
90% de cobertura de linhas
```

Cobertura atual do `fraud-engine`:

| Métrica | Cobertura |
|---|---:|
| Instruções | **93%** |
| Linhas | **93%** |
| Branches | **84%** |

O pacote de alertas possui atualmente:

```text
92% de cobertura de instruções
100% de cobertura de branches
```

O relatório HTML gerado pelo JaCoCo fica disponível em:

```text
apps/fraud-engine/target/site/jacoco/index.html
```

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

## Stack

| Tecnologia | Uso |
|---|---|
| Java | Linguagem principal |
| Spring Boot | Aplicações |
| Apache Kafka | Processamento de eventos e alertas internos |
| Apache Avro | Contrato dos eventos de transação |
| Schema Registry | Gerenciamento dos schemas |
| Redis | Idempotência e regra de velocidade |
| SMTP / Spring Mail | Notificação externa |
| Mailpit | Teste local de envio de e-mails |
| Micrometer | Métricas |
| Spring Boot Actuator | Exposição das métricas e health check |
| JaCoCo | Cobertura de testes |
| GitHub Actions | Integração contínua |
| JUnit 5 / Mockito | Testes |
| Maven | Build |
| Docker Compose | Infraestrutura local |

## Testes

Os testes cobrem os principais componentes do motor:

```text
IdempotencyService
FraudRuleEngine
HighValueTransactionRule
TransactionVelocityRule
UnusualHourTransactionRule
TransactionConsumer
FraudMetrics
RegraFraudeAdminController
FraudAlertService
KafkaInternalAlertPublisher
EmailCustomerNotificationService
```

Para executar somente os testes:

```bash
mvn test
```

Para executar a validação completa do monorepo, incluindo testes e coverage:

```bash
mvn clean verify
```

## Decisões técnicas

**Kafka + Avro**  
Os eventos de transação são processados de forma assíncrona e possuem contrato definido através de Avro e Schema Registry.

**Redis**  
Utilizado para controle de idempotência e para o contador com TTL da regra de muitas transações em curto período.

**Regras independentes**  
Cada regra implementa `FraudRule`, permitindo adicionar novas regras sem alterar o fluxo principal do consumer.

**Alertas internos**  
Transações suspeitas geram alertas internos publicados de forma assíncrona no tópico Kafka `fraud-alerts.v1`.

**Notificação externa**  
O cliente é notificado através de e-mail. No ambiente local, o Mailpit simula o servidor SMTP e permite visualizar as mensagens enviadas.

**Separação dos canais**  
O alerta interno e a notificação externa possuem contratos independentes através de `InternalAlertPublisher` e `CustomerNotificationService`, permitindo substituir ou adicionar implementações sem alterar o fluxo principal de detecção.

**Retry + DLT**  
Falhas de processamento são submetidas a novas tentativas. Eventos que continuam falhando são enviados para uma Dead Letter Topic para análise posterior.

**Métricas**  
O processamento é instrumentado com Micrometer, permitindo acompanhar volume de transações, suspeitas identificadas, regras acionadas, envios para DLT e tempo de processamento.

**Infraestrutura reproduzível**  
Kafka, Redis, Schema Registry e Mailpit são inicializados através do Docker Compose. A criação dos tópicos e o registro do schema também são automatizados durante a inicialização.

**CI**  
Build e testes são executados em paralelo. Após a conclusão dos dois jobs, o job de coverage valida a cobertura do projeto.

**Cobertura de testes**  
O JaCoCo gera o relatório de cobertura e impede que o job de coverage seja aprovado caso a cobertura de linhas do `fraud-engine` fique abaixo de **90%**.