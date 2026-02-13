# Hour Manager Backend

Fundação do domínio do sistema Work Hours Dashboard: entidades, configuração de fechamento, entradas e ajustes de horas. Sem lógica de cálculo ou dashboard.

## Estrutura

- **Domínio** (`application.core.domains`): `SystemConfig`, `HourEntry`, `HourAdjustment`
- **Engine de período** (`application.core.period`): `ClosurePeriodEngine` (cálculo início/fim do período), `PeriodBounds`
- **Engine de cálculo** (`application.core.calculation`): `PeriodCalculationService` (totais e breakdown por semana), `PeriodBalance`, `PeriodCalculationResult`, `WeekInPeriod`
- **Projeção** (`application.core.projection`): `DashboardProjectionService` (consome só o serviço de cálculo), `DashboardProjection`
- **Portas** (`application.ports`): repositórios (output) e use case de configuração (input)
- **Adaptadores** (`adapters`): entidades JPA, repositórios JPA, controller REST
- **Config** (`config`): beans para repositórios e use case

## Como rodar localmente

### Pré-requisitos

- Java 21
- Gradle (ou use a IDE para abrir o projeto e rodar)

### Build

```bash
cd hour-manager-backend
gradle build -x test
```

### Executar a aplicação

```bash
gradle bootRun
```

A aplicação sobe em **http://localhost:8080**. O banco H2 fica em `./data/hourmanager` (criado na primeira execução). Console H2: **http://localhost:8080/h2-console** (JDBC URL: `jdbc:h2:file:./data/hourmanager`, user: `sa`, password em branco).

### Testar a configuração

1. **Obter configuração atual** (sem config retorna 204):
   ```bash
   curl http://localhost:8080/api/v1/system-config
   ```

2. **Salvar configuração de fechamento** (ex.: dia 21 a 20):
   ```bash
   curl -X PUT http://localhost:8080/api/v1/system-config \
     -H "Content-Type: application/json" \
     -d "{\"closureStartDay\":21,\"closureEndDay\":20}"
   ```

3. **Obter novamente** (deve retornar a config salva com `id` e `createdAt`).

### Endpoints

| Método | Caminho | Descrição |
|--------|---------|-----------|
| GET | `/api/v1/system-config` | Retorna a configuração ativa ou 204 se não houver |
| PUT | `/api/v1/system-config` | Cria ou atualiza a configuração (body: `closureStartDay`, `closureEndDay`, 1–31) |
| GET | `/api/v1/period/current` | Período atual (ou para `?date=yyyy-MM-dd`) com base na config de fechamento |
| GET | `/api/v1/period/balance` | Cálculo do período atual: total trabalhado, total ajustado, saldo (derivado, não persistido) |
| GET | `/api/v1/dashboard/projection` | Projeção completa para o dashboard (período, totais, progresso, semanas) |
| POST | `/api/v1/entries` | Cria entrada manual de horas (body: `entryDate`, `hours` > 0, `description` opcional) |
| GET | `/api/v1/entries` | Lista entradas. `?periodCurrent=true` = só do período atual; `?start=&end=` = por intervalo |
| GET | `/api/v1/entries/{id}` | Busca entrada por ID |
| POST | `/api/v1/adjustments` | Cria ajuste (filler) (body: `adjustmentDate`, `deltaHours` ≠ 0, `description` opcional) |
| GET | `/api/v1/adjustments` | Lista ajustes. `?periodCurrent=true` ou `?start=&end=` |
| GET | `/api/v1/adjustments/{id}` | Busca ajuste por ID |

## Engine de período

O `ClosurePeriodEngine` calcula o período de fechamento que contém uma data de referência, usando apenas a configuração (dia início e dia fim). Regras:

- **Calendário** é a base absoluta (meses com 28, 29, 30 ou 31 dias).
- **Período atravessando meses** (`closureStartDay` > `closureEndDay`, ex.: 21 a 20): do dia X do mês M ao dia Y do mês M+1.
- **Período no mesmo mês** (`closureStartDay` ≤ `closureEndDay`, ex.: 1 a 31): do dia X ao dia Y do mesmo mês.
- Dia inexistente no mês (ex.: 31 em fevereiro) é ajustado para o último dia do mês.

### Validar manualmente

1. Subir a aplicação e salvar uma config (ex.: 21 a 20):
   ```bash
   curl -X PUT http://localhost:8080/api/v1/system-config -H "Content-Type: application/json" -d "{\"closureStartDay\":21,\"closureEndDay\":20}"
   ```

2. **Período para hoje**:
   ```bash
   curl http://localhost:8080/api/v1/period/current
   ```
   Resposta ex.: `{"start":"2025-01-21","end":"2025-02-20"}` (conforme a data do dia).

3. **Período para uma data fixa** (sem mudar o relógio):
   ```bash
   curl "http://localhost:8080/api/v1/period/current?date=2023-02-15"
   ```
   Com config 21–20, deve retornar `{"start":"2023-01-21","end":"2023-02-20"}` (15 de fev está nesse período).

   Outros exemplos para conferir:
   - `?date=2023-02-25` → `start`: 2023-02-21, `end`: 2023-03-20
   - `?date=2023-04-10` → 2023-03-21 a 2023-04-20
   - `?date=2023-06-20` → 2023-05-21 a 2023-06-20

4. **Testes unitários** (meses 28/30/31 dias e períodos atravessando meses):
   ```bash
   gradle test --tests "br.com.hourmanager.application.core.period.ClosurePeriodEngineTest"
   ```

## Engine de cálculo

O `PeriodCalculationService` é o único ponto de cálculo: a partir do período (bounds), lê entradas e ajustes no intervalo, soma e devolve totais e saldo. Nenhum resultado é persistido; o dashboard sempre deriva os valores.

- **Total trabalhado**: soma de `hours` de todas as entradas cuja `entryDate` está entre início e fim do período (inclusive).
- **Total ajustado**: soma de `deltaHours` de todos os ajustes cuja `adjustmentDate` está no período.
- **Saldo do período**: `totalWorked + totalAdjusted`.

**Testes unitários:**
```bash
gradle test --tests "br.com.hourmanager.application.core.calculation.PeriodCalculationServiceTest"
```

### Exemplo de saída do cálculo

Com período **2025-01-21** a **2025-02-20**, 3 entradas (8 + 6,5 + 8 = 22,5 h) e 2 ajustes (+40 e -2 = +38 h), o `GET /api/v1/period/balance` retorna:

```json
{
  "periodStart": "2025-01-21",
  "periodEnd": "2025-02-20",
  "totalWorked": 22.5,
  "totalAdjusted": 38,
  "balance": 60.5
}
```

- `totalWorked`: só entradas no período.
- `totalAdjusted`: só ajustes no período.
- `balance`: 22,5 + 38 = 60,5 (sempre derivado, nunca gravado).

Para testar: config 21–20, criar as entradas e ajustes do “Criar dados de teste rapidamente” e depois `curl http://localhost:8080/api/v1/period/balance`.

## Camada de projeção (dashboard)

O `DashboardProjectionService` gera o objeto final para o frontend. Ele **consome apenas** o `PeriodCalculationService`. Não há persistência na camada de projeção.

- **period**: início, fim e total de dias do período.
- **totals**: totalWorked, totalAdjusted, balance.
- **progress**: daysElapsed, totalDays, percentageElapsed (0–1).
- **weeks**: semanas naturais (seg–dom) que interceptam o período, com totalWorked, totalAdjusted, balance por semana.

Endpoint: **GET /api/v1/dashboard/projection** (opcional: `?date=yyyy-MM-dd`).

### Exemplo de JSON final

Período 2025-01-21 a 2025-02-20, referência 2025-02-10, 3 entradas (22,5 h) e 2 ajustes (+40, -2):

```json
{
  "period": {
    "start": "2025-01-21",
    "end": "2025-02-20",
    "totalDays": 31
  },
  "totals": {
    "totalWorked": 22.5,
    "totalAdjusted": 38,
    "balance": 60.5
  },
  "progress": {
    "daysElapsed": 21,
    "totalDays": 31,
    "percentageElapsed": 0.6774193548387096
  },
  "weeks": [
    {
      "weekStart": "2025-01-20",
      "weekEnd": "2025-01-26",
      "totalWorked": 14.5,
      "totalAdjusted": 38,
      "balance": 52.5
    },
    {
      "weekStart": "2025-01-27",
      "weekEnd": "2025-02-02",
      "totalWorked": 0,
      "totalAdjusted": 0,
      "balance": 0
    },
    {
      "weekStart": "2025-02-03",
      "weekEnd": "2025-02-09",
      "totalWorked": 8,
      "totalAdjusted": 0,
      "balance": 8
    },
    {
      "weekStart": "2025-02-10",
      "weekEnd": "2025-02-16",
      "totalWorked": 0,
      "totalAdjusted": 0,
      "balance": 0
    }
  ]
}
```

## Migrations

Flyway em `src/main/resources/db/migration`:

- **V1__create_system_config_and_events.sql**: tabelas `system_config`, `hour_entries`, `hour_adjustments`.

## Registro de horas

- **Entradas**: data, quantidade de horas (positiva), descrição opcional. Associadas ao período pelo intervalo de datas (engine de período).
- **Ajustes (fillers)**: data, delta (positivo ou negativo, não pode ser zero), descrição opcional. Mesma associação por data.
- Listagens aceitam `?periodCurrent=true` (usa config + engine para filtrar pelo período atual) ou `?start=yyyy-MM-dd&end=yyyy-MM-dd` (estrutura pronta para projeções).
- Validações: `entryDate`/`adjustmentDate` obrigatórios; `hours` ≥ 0,01; `deltaHours` ≠ 0; `description` até 500 caracteres.

### Criar dados de teste rapidamente

1. **Configurar fechamento** (ex.: 21 a 20):
   ```bash
   curl -s -X PUT http://localhost:8080/api/v1/system-config -H "Content-Type: application/json" -d "{\"closureStartDay\":21,\"closureEndDay\":20}"
   ```

2. **Algumas entradas de horas** (ajuste as datas para o período atual; ex.: período jan/21–fev/20):
   ```bash
   curl -s -X POST http://localhost:8080/api/v1/entries -H "Content-Type: application/json" -d "{\"entryDate\":\"2025-01-22\",\"hours\":8,\"description\":\"Desenvolvimento\"}"
   curl -s -X POST http://localhost:8080/api/v1/entries -H "Content-Type: application/json" -d "{\"entryDate\":\"2025-01-23\",\"hours\":6.5,\"description\":\"Reuniões\"}"
   curl -s -X POST http://localhost:8080/api/v1/entries -H "Content-Type: application/json" -d "{\"entryDate\":\"2025-02-10\",\"hours\":8,\"description\":\"Desenvolvimento\"}"
   ```

3. **Um ajuste inicial** (ex.: saldo conhecido ao começar no meio do período):
   ```bash
   curl -s -X POST http://localhost:8080/api/v1/adjustments -H "Content-Type: application/json" -d "{\"adjustmentDate\":\"2025-01-21\",\"deltaHours\":40,\"description\":\"Saldo inicial do período\"}"
   ```
   Ajuste negativo (correção):
   ```bash
   curl -s -X POST http://localhost:8080/api/v1/adjustments -H "Content-Type: application/json" -d "{\"adjustmentDate\":\"2025-01-25\",\"deltaHours\":-2,\"description\":\"Correção dupla contagem\"}"
   ```

4. **Listar só do período atual**:
   ```bash
   curl -s "http://localhost:8080/api/v1/entries?periodCurrent=true"
   curl -s "http://localhost:8080/api/v1/adjustments?periodCurrent=true"
   ```

5. **PowerShell** (Windows), mesmo fluxo:
   ```powershell
   Invoke-RestMethod -Uri "http://localhost:8080/api/v1/system-config" -Method Put -Body '{"closureStartDay":21,"closureEndDay":20}' -ContentType "application/json"
   Invoke-RestMethod -Uri "http://localhost:8080/api/v1/entries" -Method Post -Body '{"entryDate":"2025-01-22","hours":8,"description":"Desenvolvimento"}' -ContentType "application/json"
   Invoke-RestMethod -Uri "http://localhost:8080/api/v1/entries?periodCurrent=true" -Method Get
   ```

## Próximos passos (fora do escopo desta etapa)

- Projeções e indicador de risco
- Dashboard e indicadores visuais
