# Barbershop API (Java + Spring Boot)

API REST para gestão multi-tenant de barbearias, profissionais e agendamentos, desenvolvida com Java moderno e Spring Boot.
O projeto implementa regras de negócio completas, controle de acesso por perfil, cálculo inteligente de disponibilidade e persistência relacional, seguindo boas práticas de arquitetura em camadas.

## Visão técnica

- **Stack:** Java 21, Spring Boot 4, Spring MVC, Spring Security, Spring Data JPA (Hibernate), PostgreSQL, JWT (jjwt), OpenAPI (springdoc).
- **Arquitetura:** camadas claras (Controller → Service → Repository), DTOs para entrada/saída e entidades JPA com lifecycle controlado.
- **Segurança:** autenticação stateless com access/refresh token e filtro JWT; autorização por papéis (`CLIENT`, `BARBER`, `OWNER`, `ADMIN`).
- **Integrações:** login com Google via verificação de `idToken`.

## Como eu implementei

- **Fluxo de autenticação:** registro/login geram access + refresh tokens; refresh valida tipo e expiração do token; JWT carrega `publicId` e `role`.
- **Segurança por papel:** rotas protegidas com `@PreAuthorize`, garantindo limites entre cliente, barbeiro e dono da barbearia.
- **Controle de disponibilidade:** cálculo de slots considera horários de trabalho, exceções de agenda (OPEN/BLOCK) e agendamentos existentes, evitando sobreposição.
- **Regra de duração:** duração do serviço precisa ser múltiplo do `slotMinutes`; profissional pode sobrescrever duração/preço do serviço.
- **Boas práticas:** DTOs isolam payloads, entidades encapsulam estado, serviços concentram regras e validações.

## Regras de negócio por entidade

### User
- Usuário possui papel (`CLIENT`, `BARBER`, `OWNER`, `ADMIN`) e status ativo.
- Login normal usa senha com hash; login Google exige email verificado.
- JWT sempre é emitido com `publicId` e `role` para autorização consistente.

### Barbershop
- Cada barbearia tem `slotMinutes` (por padrão 20) usado em toda regra de agendamento.
- Dono (`OWNER`) é o usuário responsável pela barbearia.
- Dados principais: nome, contato, descrição e logo.

### Professional
- Profissional pertence a uma barbearia e pode estar ativo/inativo.
- Pode ter vínculo com um usuário (ex.: login do barbeiro) e perfil (nome, bio, avatar).
- Profissional inativo não pode receber agendamentos.

### Service
- Serviços possuem nome, descrição, preço e duração padrão em minutos.
- Serviço sempre pertence a uma barbearia específica.

### ProfessionalService
- Vínculo entre profissional e serviço define se o serviço está ativo para aquele profissional.
- Pode sobrescrever **preço** e **duração** do serviço padrão.
- Duração final precisa respeitar múltiplo do `slotMinutes` da barbearia.

### ProfessionalWorkingHours
- Define blocos de horário de trabalho por dia da semana (ex.: segunda 09:00–17:00).
- Apenas horários ativos entram no cálculo de disponibilidade.
- Permite múltiplos blocos no mesmo dia.

### ProfessionalScheduleException
- Exceções permitem abrir (`OPEN`) ou bloquear (`BLOCK`) janelas específicas.
- São aplicadas após os horários fixos e antes de validar conflitos com agendamentos.

### Appointment
- Agendamento precisa estar alinhado ao `slotMinutes` e respeitar duração do serviço.
- Conflitos são impedidos por verificação de sobreposição de horários.
- Status controlados: `SCHEDULED`, `COMPLETED`, `CANCELED`, `NO_SHOW`.

## Como executar

1. Configure o PostgreSQL e ajuste `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=********
app.jwt.secret=...
app.oauth.google.client-id=...
```

2. Execute a aplicação:

```bash
./mvnw spring-boot:run
```

### Swagger UI:

```
http://localhost:8080/swagger-ui/index.html