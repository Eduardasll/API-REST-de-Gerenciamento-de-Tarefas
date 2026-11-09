# API REST de Gerenciamento de Tarefas

API feita com **Java 17** e **Spring Boot** para cadastrar, consultar, atualizar e excluir
tarefas. É um CRUD completo, testável pelo Postman, usando banco **H2** (em memória) por padrão.

---

## Funcionalidades

- Criar tarefa (título, descrição, prioridade e prazo opcional)
- Listar todas as tarefas
- Buscar tarefa por ID
- Atualizar uma tarefa
- Alterar o status (PENDENTE, EM_ANDAMENTO, CONCLUIDA)
- Excluir tarefa
- Filtrar por status

## Tecnologias

Java 17 • Spring Boot 3 • Spring Web • Spring Data JPA • Bean Validation • H2 • PostgreSQL • Maven

---

## Como rodar no IntelliJ IDEA

> Pré-requisitos: **JDK 17** (ou 21) e **IntelliJ IDEA**. O Maven já vem no IntelliJ e o banco H2
> roda em memória — não é preciso instalar banco de dados.

1. `File > Open...` e selecione a pasta do projeto (a que tem o `pom.xml`). O IntelliJ baixa as
   dependências sozinho.
2. Confira o SDK 17 em `File > Project Structure > Project`.
3. Abra `src/main/java/br/com/portfolio/tarefas/TarefasApplication.java` e clique em **Run**.
4. A API sobe em `http://localhost:8080`. O console do banco fica em
   `http://localhost:8080/h2-console` (JDBC `jdbc:h2:mem:tarefasdb`, usuário `sa`, sem senha).

---

## Endpoints

Endereço local: `http://localhost:8080`

| Método | Rota | Descrição | Sucesso |
|---|---|---|---|
| `POST` | `/tarefas` | Cadastrar tarefa | `201 Created` |
| `GET` | `/tarefas` | Listar tarefas | `200 OK` |
| `GET` | `/tarefas/{id}` | Buscar uma tarefa | `200 OK` |
| `PUT` | `/tarefas/{id}` | Atualizar dados | `200 OK` |
| `PATCH` | `/tarefas/{id}/status` | Alterar status | `200 OK` |
| `DELETE` | `/tarefas/{id}` | Excluir | `204 No Content` |
| `GET` | `/tarefas?status=PENDENTE` | Filtrar por status | `200 OK` |

---

## Exemplos de requisições

### Criar — `POST /tarefas`
```json
{
  "titulo": "Estudar Spring Boot",
  "descricao": "Fazer minha primeira API REST",
  "prioridade": "ALTA",
  "prazo": "2027-01-20"
}
```
Resposta `201 Created`:
```json
{
  "id": 1,
  "titulo": "Estudar Spring Boot",
  "descricao": "Fazer minha primeira API REST",
  "status": "PENDENTE",
  "prioridade": "ALTA",
  "prazo": "2027-01-20",
  "criadaEm": "2027-01-10T10:00:00"
}
```

### Alterar status — `PATCH /tarefas/1/status`
```json
{ "status": "EM_ANDAMENTO" }
```

### Buscar inexistente — `GET /tarefas/999`
Resposta `404`:
```json
{ "mensagem": "Tarefa nao encontrada" }
```

### Erro de validação — `POST /tarefas` sem título
Resposta `400`:
```json
{
  "mensagem": "Dados invalidos",
  "erros": { "titulo": "O titulo e obrigatorio" }
}
```

---

## Regras de negócio

1. Título obrigatório, de 3 a 100 caracteres.
2. Descrição opcional, até 500 caracteres.
3. Prioridade: `BAIXA`, `MEDIA` ou `ALTA`.
4. Toda tarefa nasce com `status = PENDENTE` e `criadaEm` automático.
5. Status pode mudar entre `PENDENTE`, `EM_ANDAMENTO` e `CONCLUIDA`.
6. ID inexistente retorna `404`; dados inválidos retornam `400`.
7. Lista vazia retorna `[]`; exclusão retorna `204` sem corpo.

---

## Banco de dados

- **H2 (padrão):** em memória, sem instalação. Já funciona ao rodar.
- **PostgreSQL (opcional):** no `application.properties`, comente o bloco do H2 e descomente o do
  PostgreSQL. Use variáveis de ambiente (`DB_NAME`, `DB_USER`, `DB_PASSWORD`) para as credenciais —
  nunca coloque senhas no código.

---

## Estrutura do projeto

```
src/main/java/br/com/portfolio/tarefas/
├── TarefasApplication.java        # ponto de entrada
├── controller/TarefaController.java   # rotas HTTP
├── service/TarefaService.java         # regras de negócio
├── repository/TarefaRepository.java   # acesso ao banco (JPA)
├── model/                             # entidade e enums
│   ├── Tarefa.java
│   ├── StatusTarefa.java
│   └── Prioridade.java
├── dto/                               # objetos de entrada e saída
│   ├── TarefaRequest.java
│   ├── AtualizarStatusRequest.java
│   └── TarefaResponse.java
└── exception/                         # tratamento de erros
    ├── TarefaNaoEncontradaException.java
    └── ManipuladorDeErros.java
```
