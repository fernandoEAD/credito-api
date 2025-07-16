# 🏦 Crédito API - Sistema de Consulta de Créditos Tributários

API REST para consulta de créditos tributários com sistema de auditoria em tempo real usando Apache Kafka.

## 🏗️ Arquitetura do Sistema

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Backend       │    │   PostgreSQL    │
│   Angular       │───▶│   Spring Boot   │───▶│   Database      │
│   Port: 4200    │    │   Port: 8080    │    │   Port: 5432    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐    ┌─────────────────┐
                       │   Apache Kafka  │    │   Zookeeper     │
                       │   Port: 9092    │───▶│   Port: 2181    │
                       │   (Auditoria)   │    │   (Coordenação) │
                       └─────────────────┘    └─────────────────┘
```

## 🚀 Como Executar o Projeto

### Pré-requisitos
- Docker e Docker Compose
- JDK 17+ (para desenvolvimento)
- Maven 3.6+ (para desenvolvimento)

### 1. Executar com Docker (Recomendado)

```bash
# Clonar o repositório
git clone <seu-repositorio>
cd credito-api/backend

# Executar todos os serviços
docker-compose up -d

# Verificar se todos os serviços estão rodando
docker-compose ps
```

### 2. Executar em Desenvolvimento

```bash
# Executar apenas banco e Kafka
docker-compose up -d postgres zookeeper kafka

# Executar a aplicação
mvn spring-boot:run

# Ou compilar e executar
mvn clean package
java -jar target/credito-api-0.0.1-SNAPSHOT.jar
```

### 3. Executar Testes

```bash
# Todos os testes
mvn test

# Testes específicos
mvn test -Dtest=CreditoServiceTest
mvn test -Dtest=AuditoriaPublisherTest
```

## 📡 Endpoints da API

### Base URL: `http://localhost:8080`

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/creditos/{numeroNfse}` | Lista créditos por número da NFS-e |
| GET | `/api/creditos/credito/{numeroCredito}` | Busca crédito específico |

### Exemplos de Uso

```bash
# Buscar créditos por NFS-e
curl "http://localhost:8080/api/creditos/7891011"

# Buscar crédito específico
curl "http://localhost:8080/api/creditos/credito/123456"
```

### Respostas da API

```json
{
  "id": 1,
  "numeroCredito": "123456",
  "numeroNfse": "7891011",
  "dataConstituicao": "2024-02-25",
  "tipoCredito": "ISSQN",
  "valorFaturado": 30000.00,
  "baseCalculo": 25000.00,
  "aliquota": 5.0,
  "valorIssqn": 1500.75,
  "valorDeducao": 5000.00,
  "simplesNacional": true
}
```

## 🔍 Sistema de Auditoria com Apache Kafka

### 📋 O que é e Para Que Serve

O sistema Kafka implementado serve para **auditoria em tempo real** de todas as consultas realizadas na API. Cada vez que alguém faz uma consulta, um evento é automaticamente enviado para o Kafka contendo:

- **Identificador único** da consulta
- **Endpoint** acessado
- **Parâmetros** da consulta
- **Timestamp** da operação
- **Informações técnicas** (método HTTP, status da resposta, etc.)

### 🏗️ Como Foi Implementado

#### 1. **Interceptor Automático** (`AuditoriaInterceptor`)
```java
// Captura AUTOMATICAMENTE todas as requisições para /api/creditos/*
@Override
public boolean preHandle(HttpServletRequest request, 
                        HttpServletResponse response, 
                        Object handler) {
    // Cria evento de auditoria
    // Envia para Kafka
    return true;
}
```

#### 2. **Publisher Kafka** (`AuditoriaPublisher`)
```java
@Service
public class AuditoriaPublisher {
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    public void publicarEventoAuditoria(ConsultaAuditoriaEvent evento) {
        // Serializa evento para JSON
        // Envia para tópico Kafka
        // Trata erros graciosamente
    }
}
```

#### 3. **Evento de Auditoria** (`ConsultaAuditoriaEvent`)
```java
{
  "id": "uuid-unico",
  "endpoint": "/api/creditos/123456",
  "parametro": "numeroCredito=123456",
  "metodo": "GET",
  "timestamp": "2024-07-16T09:30:00Z",
  "statusResposta": 200,
  "tempoProcessamento": 150,
  "userAgent": "Mozilla/5.0...",
  "ipOrigemString": "192.168.1.100"
}
```

### 🎯 Benefícios do Sistema de Auditoria

1. **📊 Monitoramento em Tempo Real**
   - Rastreia todas as consultas instantaneamente
   - Permite análise de padrões de uso

2. **🔒 Segurança e Compliance**
   - Registra quem acessou quais dados e quando
   - Facilita auditorias de segurança

3. **📈 Analytics e Business Intelligence**
   - Identifica endpoints mais utilizados
   - Analisa performance da API
   - Detecta picos de uso

4. **🚨 Alertas e Monitoramento**
   - Pode gerar alertas para uso anômalo
   - Monitora performance em tempo real

### 🔧 Configuração do Kafka

#### Tópico: `consultas-auditoria`
- **Partições**: 3 (para paralelismo)
- **Replicação**: 1 (ambiente desenvolvimento)
- **Retenção**: 7 dias

#### Producer Configuration:
```properties
# Configuração para alta confiabilidade
acks=all                    # Aguarda confirmação de todas as réplicas
retries=3                   # Tentativas em caso de falha
enable.idempotence=true     # Garante que mensagens não sejam duplicadas
linger.ms=1                 # Agrupa mensagens para melhor performance
```

## 🐳 Serviços Docker

| Serviço | Porta | Descrição |
|---------|--------|-----------|
| **backend** | 8080 | API Spring Boot |
| **postgres** | 5432 | Banco de dados principal |
| **frontend** | 4200 | Interface Angular |
| **zookeeper** | 2181 | Coordenação do Kafka |
| **kafka** | 9092 | Sistema de mensageria |

### Verificar Saúde dos Serviços

```bash
# Status dos containers
docker-compose ps

# Logs da aplicação
docker-compose logs backend

# Logs do Kafka
docker-compose logs kafka

# Conectar no banco
docker-compose exec postgres psql -U creditouser -d creditodb
```

## 🧪 Testes

### Cobertura de Testes
- ✅ **Testes Unitários**: Service layer e Kafka publisher
- ✅ **Testes de Integração**: API endpoints (configuração em andamento)
- ✅ **Mocks**: Kafka desabilitado durante testes

### Executar Testes Específicos
```bash
# Testes de regra de negócio
mvn test -Dtest=CreditoServiceTest

# Testes do sistema Kafka
mvn test -Dtest=AuditoriaPublisherTest

# Todos os testes unitários funcionais
mvn test -Dtest="CreditoServiceTest,AuditoriaPublisherTest"
```

## 📂 Estrutura do Projeto

```
backend/
├── src/main/java/com/exemplo/credito/
│   ├── controller/          # Controllers REST
│   ├── service/            # Lógica de negócio
│   ├── repository/         # Acesso a dados
│   ├── entity/             # Entidades JPA
│   ├── config/             # Configurações (Kafka, Web)
│   ├── interceptor/        # Interceptador de auditoria
│   └── event/              # Eventos Kafka
├── src/test/               # Testes unitários e integração
├── docker-compose.yml      # Orquestração de serviços
└── Dockerfile             # Imagem da aplicação
```

## 🔐 Variáveis de Ambiente

### Produção
```bash
# Banco de dados
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/creditodb
SPRING_DATASOURCE_USERNAME=creditouser
SPRING_DATASOURCE_PASSWORD=senha123

# Kafka
KAFKA_BOOTSTRAP_SERVERS=kafka:9092
KAFKA_TOPIC_NOME=consultas-auditoria
```

### Desenvolvimento Local
```bash
# Usar application.properties padrão
# Kafka em localhost:9092
# PostgreSQL em localhost:5432
```

## 📊 Monitoramento do Kafka

### Visualizar Mensagens no Tópico
```bash
# Conectar no container Kafka
docker-compose exec kafka bash

# Listar tópicos
kafka-topics --bootstrap-server localhost:9092 --list

# Consumir mensagens do tópico de auditoria
kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic consultas-auditoria --from-beginning
```

### Exemplo de Mensagem de Auditoria
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "endpoint": "/api/creditos/credito/123456",
  "parametro": "numeroCredito=123456",
  "metodo": "GET",
  "statusResposta": 200,
  "quantidadeResultados": 1,
  "timestamp": "2024-07-16T12:30:00.000Z",
  "tempoProcessamento": 145,
  "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
  "ipOrigemString": "192.168.1.100"
}
```

## 🚀 Deploy em Produção

### 1. Configurar Variáveis de Ambiente
```bash
export SPRING_PROFILES_ACTIVE=prod
export KAFKA_BOOTSTRAP_SERVERS=seu-kafka-cluster:9092
export SPRING_DATASOURCE_URL=sua-url-postgresql
```

### 2. Build da Aplicação
```bash
mvn clean package -DskipTests
docker build -t credito-api:latest .
```

### 3. Deploy com Docker Compose
```bash
docker-compose -f docker-compose.prod.yml up -d
```

## 🔧 Troubleshooting

### Problemas Comuns

1. **Kafka não conecta**
   ```bash
   # Verificar se Zookeeper está rodando
   docker-compose logs zookeeper
   
   # Restart dos serviços Kafka
   docker-compose restart zookeeper kafka
   ```

2. **Banco não conecta**
   ```bash
   # Verificar logs do PostgreSQL
   docker-compose logs postgres
   
   # Testar conexão manual
   docker-compose exec postgres psql -U creditouser -d creditodb
   ```

3. **Aplicação não inicia**
   ```bash
   # Verificar logs da aplicação
   docker-compose logs backend
   
   # Verificar se todas as dependências estão rodando
   docker-compose ps
   ```

## 👥 Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

---

## 🎯 Próximas Funcionalidades

- [ ] Dashboard de monitoramento em tempo real
- [ ] Alertas automáticos via Kafka
- [ ] Cache Redis para consultas frequentes
- [ ] API de métricas e analytics
- [ ] Autenticação JWT
- [ ] Rate limiting por usuário 