# Fraud Detection Engine

Motor de detecção de transações suspeitas em tempo real desenvolvido com **Java, Spring Boot, Apache Kafka e Redis**.

O projeto recebe eventos de transações via Kafka, aplica regras de fraude e encaminha falhas não recuperáveis para uma Dead Letter Topic (DLT).

## Arquitetura

```text
┌─────────────┐       ┌─────────────────┐       ┌──────────────────────┐
│  Simulator  │──────▶│ transactions.v1 │──────▶│     Fraud Engine     │
└─────────────┘       └─────────────────┘       └──────────┬───────────┘
                                                          │
                                    ┌─────────────────────┼─────────────────────┐
                                    │                     │                     │
                                    ▼                     ▼                     ▼
                              ┌────────────┐       ┌─────────────┐       ┌─────────────┐
                              │   Redis    │       │   Regras    │       │  Métricas   │
                              │            │       │             │       │             │
                              │Idempotência│       │ Valor alto  │       │ Micrometer  │
                              │ Velocidade │       │ Velocidade  │       │  Actuator   │
                              └────────────┘       │  Horário    │       └─────────────┘
                                                   │  incomum    │
                                                   └──────┬──────┘
                                                          │
                                                   erro após retries
                                                          │
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
│       ├── build.yml
│       ├── test.yml
│       └── coverage.yml
├── apps/
│   ├── fraud-engine/       # Consumer e motor de fraude
│   └── simulator/          # Producer para testes
├── libs/
│   └── event-contracts/    # Contratos Avro
├── schemas/                # Schemas dos eventos
├── docker-compose.yml
└── pom.xml                 # Maven aggregator
```

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

O projeto possui pipelines independentes no GitHub Actions para validar build, testes e cobertura de código.

| Pipeline | Responsabilidade |
|---|---|
| **Build** | Compila e empacota o monorepo sem executar os testes |
| **Tests** | Executa a suíte automatizada de testes |
| **Coverage** | Executa os testes, gera o relatório JaCoCo e valida a cobertura mínima |

Os workflows estão disponíveis em:

```text
.github/workflows/
├── build.yml
├── test.yml
└── coverage.yml
```

A pipeline de cobertura utiliza **JaCoCo** e exige no mínimo:

```text
90% de cobertura de linhas
```

Cobertura atual do `fraud-engine`:

| Métrica | Cobertura |
|---|---:|
| Instruções | **93%** |
| Linhas | **93%** |
| Branches | **83%** |

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

### 2. Compilar e executar os testes

O projeto possui um Maven aggregator, portanto todo o monorepo pode ser validado com:

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

### 3. Executar o Fraud Engine

```bash
cd apps/fraud-engine
mvn spring-boot:run
```

### 4. Publicar transações

Em outro terminal:

```bash
cd apps/simulator
mvn spring-boot:run
```

O simulator publica eventos Avro no tópico:

```text
transactions.v1
```

## Stack

| Tecnologia | Uso |
|---|---|
| Java | Linguagem principal |
| Spring Boot | Aplicações |
| Apache Kafka | Processamento de eventos |
| Apache Avro | Contrato dos eventos |
| Schema Registry | Gerenciamento dos schemas |
| Redis | Idempotência e regra de velocidade |
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
Os eventos são processados de forma assíncrona e possuem contrato definido através de Avro e Schema Registry.

**Redis**  
Utilizado para controle de idempotência e para o contador com TTL da regra de muitas transações em curto período.

**Regras independentes**  
Cada regra implementa `FraudRule`, permitindo adicionar novas regras sem alterar o fluxo principal do consumer.

**Retry + DLT**  
Falhas de processamento são submetidas a novas tentativas. Eventos que continuam falhando são enviados para uma Dead Letter Topic para análise posterior.

**Métricas**  
O processamento é instrumentado com Micrometer, permitindo acompanhar volume de transações, suspeitas identificadas, regras acionadas, envios para DLT e tempo de processamento.

**CI**  
Build, testes e cobertura são validados em pipelines independentes, facilitando a identificação de falhas e evitando regressões.

**Cobertura de testes**  
O JaCoCo gera o relatório de cobertura e impede que o build de coverage seja aprovado caso a cobertura de linhas do `fraud-engine` fique abaixo de **90%**.