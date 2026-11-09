# API REST de Gerenciamento de Tarefas

Uma API (Interface de Programação de Aplicações) feita com **Java** e **Spring Boot** para
cadastrar, consultar, atualizar e excluir tarefas. Este README foi escrito para quem está
começando: além de explicar como rodar o projeto, ele explica **cada pedaço do código** e os
**conceitos** por trás dele (API, REST, Spring, JPA, DDD e Clean Code).

---

## Índice

1. [O que este projeto faz](#1-o-que-este-projeto-faz)
2. [Conceitos essenciais (para leigos)](#2-conceitos-essenciais-para-leigos)
3. [Como rodar no IntelliJ IDEA](#3-como-rodar-no-intellij-idea)
4. [A arquitetura em camadas](#4-a-arquitetura-em-camadas)
5. [Explicação arquivo por arquivo](#5-explicação-arquivo-por-arquivo)
6. [Contrato da API (endpoints)](#6-contrato-da-api-endpoints)
7. [Exemplos de requisições](#7-exemplos-de-requisições)
8. [Regras de negócio](#8-regras-de-negócio)
9. [Usando o banco H2 e migrando para PostgreSQL](#9-usando-o-banco-h2-e-migrando-para-postgresql)
10. [Glossário rápido](#10-glossário-rápido)

---

## 1. O que este projeto faz

Imagine um caderno de tarefas. Você pode:

- **Criar** uma tarefa (título, descrição, prioridade e um prazo opcional).
- **Listar** todas as tarefas.
- **Buscar** uma tarefa específica pelo número (ID).
- **Atualizar** os dados de uma tarefa.
- **Mudar o status** de uma tarefa (PENDENTE, EM_ANDAMENTO ou CONCLUIDA).
- **Excluir** uma tarefa.
- **Filtrar** tarefas por status.

A diferença é que, em vez de clicar em botões, outros programas (como o **Postman** ou um site)
conversam com a aplicação **enviando mensagens pela internet** (HTTP) e recebendo respostas em
**JSON** (um formato de texto para troca de dados).

---

## 2. Conceitos essenciais (para leigos)

### O que é uma API?
API é um "garçom" entre dois programas. Você (cliente) faz um **pedido** e a API traz a
**resposta**. Aqui o pedido é uma requisição HTTP e a resposta vem em JSON.

### O que é REST?
REST é um **estilo** de organizar APIs. A ideia central: tudo é um **recurso** (aqui, "tarefas")
e usamos os **verbos do HTTP** para agir sobre ele:

| Verbo HTTP | Significado | Exemplo neste projeto |
|---|---|---|
| `GET` | Ler/consultar | listar ou buscar tarefas |
| `POST` | Criar | cadastrar uma tarefa nova |
| `PUT` | Substituir/atualizar | trocar todos os dados de uma tarefa |
| `PATCH` | Atualizar um pedaço | mudar só o status |
| `DELETE` | Apagar | excluir uma tarefa |

### O que é JSON?
Um texto estruturado, fácil de ler por humanos e máquinas. Exemplo:
```json
{ "titulo": "Estudar Spring", "prioridade": "ALTA" }
```

### O que é Spring e Spring Boot?
**Spring** é um conjunto de ferramentas que facilita criar aplicações Java grandes.
**Spring Boot** é o Spring "já configurado": ele liga um servidor web, conecta ao banco e
monta tudo sozinho, para você focar na lógica. Ele usa muita **anotação** — aquelas palavras
com `@` na frente (ex.: `@RestController`) que dão instruções ao Spring.

### O que é JPA / Hibernate?
**JPA** é a "regra" do Java para transformar **objetos** (classes) em **linhas de tabela** no
banco de dados, sem você escrever SQL manualmente. O **Hibernate** é quem implementa essa regra
nos bastidores. Assim, salvar um objeto `Tarefa` vira um `INSERT` automaticamente.

### O que é DDD (Domain-Driven Design)?
É "projetar o software ao redor do domínio" — ou seja, ao redor do **problema real** que estamos
resolvendo. Neste projeto, o **domínio** é "tarefa". Por isso temos uma classe `Tarefa` que
representa o conceito do mundo real, e enums (`StatusTarefa`, `Prioridade`) que descrevem seus
estados possíveis usando a **linguagem do negócio** — e não códigos soltos como `1` ou `2`.
A ideia do DDD é que o código "fale a mesma língua" de quem usa o sistema.

### O que é Clean Code?
"Código limpo" é código fácil de ler e manter. Princípios aplicados aqui:

- **Nomes claros**: `buscarPorId`, `alterarStatus` dizem exatamente o que fazem.
- **Cada classe com uma responsabilidade** (Single Responsibility): o Controller só lida com
  HTTP, o Service só com regras, o Repository só com banco.
- **Separar entrada/saída do banco** usando DTOs, para não misturar as coisas.
- **Tratamento de erros centralizado**, sem repetir código.

---

## 3. Como rodar no IntelliJ IDEA

> Pré-requisitos: **JDK 17** (ou 21) instalado e **IntelliJ IDEA** (a versão Community é grátis).
> O **Maven** já vem embutido no IntelliJ, e o banco **H2** roda em memória — você **não precisa
> instalar banco de dados** para testar.

1. **Abrir o projeto**: `File > Open...` e selecione a pasta deste projeto (a que contém o
   `pom.xml`). O IntelliJ reconhece que é um projeto Maven e baixa as dependências sozinho
   (pode levar alguns minutos na primeira vez).
2. **Conferir o JDK**: `File > Project Structure > Project` e selecione o SDK 17 (ou 21).
3. **Rodar**: abra `src/main/java/br/com/portfolio/tarefas/TarefasApplication.java`, clique na
   seta verde ao lado do método `main` e escolha **Run**.
4. **Verificar**: no console deve aparecer algo como *"Started TarefasApplication"*. A API estará
   no ar em `http://localhost:8080`.
5. **Testar**: use o **Postman** (ou o cliente HTTP do próprio IntelliJ) para enviar as
   requisições da seção [Exemplos](#7-exemplos-de-requisições). Você também pode abrir o console
   do banco em `http://localhost:8080/h2-console` (URL JDBC: `jdbc:h2:mem:tarefasdb`,
   usuário `sa`, sem senha).

---

## 4. A arquitetura em camadas

O projeto segue uma arquitetura em **camadas**, onde cada camada tem um papel e conversa apenas
com a camada vizinha. Isso deixa o código organizado e fácil de testar.

```
Postman / Navegador
        │  (requisição HTTP + JSON)
        ▼
┌───────────────────┐
│    Controller     │  recebe a requisição, define as rotas e os códigos HTTP
└─────────┬─────────┘
          ▼
┌───────────────────┐
│     Service       │  aplica as regras de negócio
└─────────┬─────────┘
          ▼
┌───────────────────┐
│    Repository     │  salva e busca dados (via JPA)
└─────────┬─────────┘
          ▼
   Banco de Dados (H2 ou PostgreSQL)
```

Pelo caminho temos apoios:
- **Model** (`Tarefa`, enums): representa o domínio e as tabelas.
- **DTOs** (`TarefaRequest`, `TarefaResponse`...): o formato de entrada e saída da API.
- **Exception**: tratamento de erros padronizado.

---

## 5. Explicação arquivo por arquivo

### `TarefasApplication.java` — o ponto de partida
```
@SpringBootApplication
public class TarefasApplication { public static void main(...) { SpringApplication.run(...) } }
```
É aqui que a aplicação **começa**. A anotação `@SpringBootApplication` liga o "piloto automático"
do Spring: ele procura suas classes, configura o servidor web e conecta ao banco. O método
`main` é o que o Java executa primeiro.

### `model/StatusTarefa.java` e `model/Prioridade.java` — os enums
Um **enum** é uma lista fixa de valores possíveis. Em vez de guardar o status como um texto
qualquer (sujeito a erro de digitação), só permitimos `PENDENTE`, `EM_ANDAMENTO` e `CONCLUIDA`.
Isso é **DDD na prática**: usamos a linguagem do negócio dentro do código.

### `model/Tarefa.java` — a entidade
É a classe que representa **uma tarefa** e, ao mesmo tempo, **uma linha da tabela** `tarefas`.
As anotações explicam ao JPA como mapear:
- `@Entity` / `@Table(name = "tarefas")`: "esta classe vira uma tabela".
- `@Id` + `@GeneratedValue`: o `id` é a chave e é gerado automaticamente pelo banco.
- `@Column`: define regras da coluna (ex.: `titulo` não pode ser nulo, limite de 100).
- `@Enumerated(EnumType.STRING)`: salva o enum como texto (`"ALTA"`) em vez de número.
- `@PrePersist`: o método `aoCriar()` roda **antes de salvar pela primeira vez**, definindo o
  `status` inicial (`PENDENTE`) e a data `criadaEm`. Assim garantimos a regra de negócio "toda
  tarefa nasce pendente" **sem depender do cliente**.

Os métodos `get`/`set` (getters e setters) são a forma padrão do Java de ler e alterar os campos.

### `repository/TarefaRepository.java` — o acesso ao banco
É uma **interface** (um contrato, sem corpo de código). Ao escrever
`extends JpaRepository<Tarefa, Long>`, ganhamos de graça métodos como `save`, `findById`,
`findAll` e `deleteById` — o Spring Data **implementa tudo sozinho**. Também declaramos
`findByStatus(...)`: o Spring entende o nome do método e cria a consulta
`WHERE status = ?` automaticamente. Isso é produtividade sem escrever SQL.

### `dto/TarefaRequest.java` — dados que entram
**DTO** = Data Transfer Object (objeto de transferência de dados). É o formato que o cliente
**envia** ao criar/atualizar uma tarefa. Por que não usar a entidade `Tarefa` diretamente?
Porque o cliente **não deve** poder definir o `id`, o `status` inicial ou a data de criação —
esses são controlados pelo servidor. As anotações **validam** a entrada:
- `@NotBlank`: o título não pode ser vazio.
- `@Size(min = 3, max = 100)`: tamanho permitido.
- `@NotNull`: a prioridade é obrigatória.

Se a validação falhar, o Spring devolve **400 Bad Request** automaticamente.

### `dto/AtualizarStatusRequest.java` — só o status
Um DTO pequeno, usado no `PATCH`, que carrega apenas o novo `status`.

### `dto/TarefaResponse.java` — dados que saem
É o formato que a API **devolve**. Foi feito com `record` (recurso do Java moderno): uma classe
**imutável** e enxuta, ideal para transportar dados. O método `de(...)` **converte** uma entidade
`Tarefa` nesse objeto de resposta. Separar entrada e saída do banco é uma boa prática de
**Clean Code**: a API decide exatamente o que mostrar.

### `service/TarefaService.java` — as regras de negócio
É o **cérebro** da aplicação. O Controller não decide nada sozinho: ele pede ao Service.
Aqui acontecem operações como criar, atualizar, buscar e, quando a tarefa não existe, lançar a
exceção de "não encontrada". Repare na **injeção de dependência por construtor**:
```
public TarefaService(TarefaRepository repository) { this.repository = repository; }
```
Nós não criamos o repositório com `new`; o Spring **entrega** uma instância pronta. Isso deixa o
código desacoplado e fácil de testar (princípio importante de Clean Code e do Spring).

### `controller/TarefaController.java` — as rotas HTTP
É a **porta de entrada** da API. Cada método vira um endpoint:
- `@RestController` + `@RequestMapping("/tarefas")`: todas as rotas começam com `/tarefas`.
- `@PostMapping`, `@GetMapping`, `@PutMapping`, `@PatchMapping`, `@DeleteMapping`: ligam o método
  ao verbo HTTP.
- `@PathVariable`: pega um valor da URL (ex.: o `1` em `/tarefas/1`).
- `@RequestParam`: pega um parâmetro da query (ex.: `?status=PENDENTE`).
- `@RequestBody`: transforma o JSON recebido em um objeto Java.
- `@Valid`: dispara a validação do DTO antes de executar o método.
- `ResponseEntity`: permite controlar o **código HTTP** da resposta (201, 204 etc.).

O Controller **não tem regra de negócio** — ele só traduz HTTP ↔ Java e delega ao Service.

### `exception/TarefaNaoEncontradaException.java` — erro de "não achei"
Uma exceção personalizada, lançada quando buscam um ID que não existe. Carrega a mensagem
"Tarefa nao encontrada".

### `exception/ManipuladorDeErros.java` — tratamento central de erros
Com `@RestControllerAdvice`, esta classe **intercepta os erros** de toda a API e devolve
respostas padronizadas em JSON:
- `TarefaNaoEncontradaException` → **404 Not Found**.
- Falha de validação (`@Valid`) → **400 Bad Request** com a lista de campos inválidos.
- JSON malformado → **400 Bad Request**.

Centralizar isso evita repetir `try/catch` em todo lugar — de novo, Clean Code.

### `application.properties` — configuração
Diz ao Spring qual banco usar. Por padrão, o **H2 em memória** (nada para instalar). Há também um
bloco comentado mostrando como apontar para o **PostgreSQL**.

### `pom.xml` — as dependências (Maven)
O **Maven** é o gerenciador de dependências e de build. O `pom.xml` lista as "peças" que o projeto
usa: Spring Web (API), Spring Data JPA (banco), Validation (validações), H2 e PostgreSQL (drivers
de banco) e as ferramentas de teste. O IntelliJ lê esse arquivo e baixa tudo automaticamente.

---

## 6. Contrato da API (endpoints)

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

## 7. Exemplos de requisições

### Criar uma tarefa — `POST /tarefas`
Corpo (JSON):
```json
{
  "titulo": "Estudar Spring Boot",
  "descricao": "Fazer minha primeira API REST",
  "prioridade": "ALTA",
  "prazo": "2027-01-20"
}
```
Resposta — `201 Created`:
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
> Os valores de `id` e `criadaEm` são gerados pela aplicação.

### Listar / filtrar — `GET /tarefas` ou `GET /tarefas?status=PENDENTE`
Resposta — `200 OK`: uma lista JSON (ou `[]` se não houver tarefas).

### Buscar por ID — `GET /tarefas/1`
Resposta — `200 OK` com a tarefa, ou `404` se não existir:
```json
{ "mensagem": "Tarefa nao encontrada" }
```

### Atualizar — `PUT /tarefas/1`
Mesmo corpo do `POST`. Resposta — `200 OK` com os dados atualizados.

### Alterar status — `PATCH /tarefas/1/status`
```json
{ "status": "EM_ANDAMENTO" }
```
Resposta — `200 OK` com a tarefa já atualizada.

### Excluir — `DELETE /tarefas/1`
Resposta — `204 No Content` (sem corpo). Se o ID não existir, `404`.

### Exemplo de erro de validação — `POST /tarefas` com título vazio
Resposta — `400 Bad Request`:
```json
{
  "mensagem": "Dados invalidos",
  "erros": { "titulo": "O titulo e obrigatorio" }
}
```

---

## 8. Regras de negócio

1. O **título** é obrigatório e deve ter de **3 a 100** caracteres.
2. A **descrição** é opcional, com no máximo **500** caracteres.
3. A **prioridade** deve ser `BAIXA`, `MEDIA` ou `ALTA`.
4. Toda tarefa **nasce** com `status = PENDENTE` e a data `criadaEm` é preenchida automaticamente.
5. O **status** pode mudar entre `PENDENTE`, `EM_ANDAMENTO` e `CONCLUIDA`.
6. Operações em um **ID inexistente** retornam `404`.
7. Dados inválidos retornam `400 Bad Request`.
8. Uma lista vazia retorna `[]`, não erro.
9. A exclusão bem-sucedida retorna `204`, sem corpo.

---

## 9. Usando o banco H2 e migrando para PostgreSQL

### H2 (padrão, já funciona)
O H2 é um banco **em memória**: rápido para desenvolver e some quando a aplicação para. Não exige
instalação. Console web: `http://localhost:8080/h2-console` (JDBC `jdbc:h2:mem:tarefasdb`,
usuário `sa`, senha em branco).

### PostgreSQL (opcional)
1. Instale o PostgreSQL e crie um banco (ex.: `tarefas`).
2. No `application.properties`, comente as linhas do H2 e **descomente** o bloco do PostgreSQL.
3. Informe usuário/senha por **variáveis de ambiente** (`DB_NAME`, `DB_USER`, `DB_PASSWORD`) —
   **nunca** coloque senhas no código ou no GitHub.
4. Rode novamente e repita os testes.

---

## 10. Glossário rápido

| Termo | Significado simples |
|---|---|
| CRUD | Criar, consultar, atualizar e excluir registros. |
| REST | Estilo de organizar APIs usando HTTP. |
| JSON | Formato de texto para trocar dados. |
| Endpoint | Um endereço + ação da API (ex.: `GET /tarefas`). |
| Controller | Recebe a requisição HTTP e devolve a resposta. |
| Service | Contém as regras de negócio. |
| Repository | Faz o acesso aos dados. |
| Entity | Classe que representa uma tabela do banco. |
| DTO | Objeto usado para receber ou devolver dados na API. |
| JPA / Hibernate | Mapeiam objetos Java para tabelas sem escrever SQL. |
| Enum | Lista fixa de valores possíveis. |
| Anotação (`@`) | Instrução para o Spring/JPA sobre como tratar a classe/método. |
| Injeção de dependência | O Spring entrega objetos prontos, sem usar `new`. |
| H2 | Banco simples, em memória, para desenvolvimento. |

---

**Tecnologias:** Java 17 • Spring Boot 3 • Spring Web • Spring Data JPA • Bean Validation •
H2 • PostgreSQL • Maven.
