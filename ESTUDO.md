# Guia de Estudo — Entendendo o Projeto por Dentro

Este arquivo é um material **didático e pessoal** para quem está aprendendo. Ele explica **cada
parte do código** e os **conceitos** por trás dele (API, REST, HTTP, Spring, JPA, DDD e Clean
Code), partindo do zero. Se você só quer rodar e usar o projeto, veja o [README.md](README.md).

---

## Índice

1. [Conceitos essenciais (para leigos)](#1-conceitos-essenciais-para-leigos)
2. [Como o HTTP funciona (verbos e status)](#2-como-o-http-funciona-verbos-e-status)
3. [A arquitetura em camadas](#3-a-arquitetura-em-camadas)
4. [O caminho de uma requisição, passo a passo](#4-o-caminho-de-uma-requisição-passo-a-passo)
5. [Explicação arquivo por arquivo](#5-explicação-arquivo-por-arquivo)
6. [As anotações do Spring e do JPA explicadas](#6-as-anotações-do-spring-e-do-jpa-explicadas)
7. [DDD e Clean Code neste projeto](#7-ddd-e-clean-code-neste-projeto)
8. [Regras de negócio detalhadas](#8-regras-de-negócio-detalhadas)
9. [Testes que você deve fazer](#9-testes-que-você-deve-fazer)
10. [Perguntas de entrevista (com respostas)](#10-perguntas-de-entrevista-com-respostas)
11. [Glossário completo](#11-glossário-completo)

---

## 1. Conceitos essenciais (para leigos)

### O que é uma API?
API (Interface de Programação de Aplicações) é um "garçom" entre dois programas. Você (cliente)
faz um **pedido** e a API traz a **resposta**. Aqui o pedido é uma requisição HTTP e a resposta
vem em JSON. Um **endpoint** é um endereço com uma ação, como `GET /tarefas`.

### O que é REST?
REST é um **estilo** de organizar APIs. A ideia central: tudo é um **recurso** (aqui, "tarefas")
e usamos os **verbos do HTTP** para agir sobre ele. É simples, previsível e muito usado no mercado.

### O que é JSON?
JSON (JavaScript Object Notation) é um texto estruturado, fácil de ler por humanos e máquinas:
```json
{ "titulo": "Estudar Spring", "prioridade": "ALTA" }
```
São pares `"chave": valor`. É o "idioma" que cliente e API usam para conversar.

### O que é Java?
Linguagem de programação **orientada a objetos**: organizamos o código em **classes** (moldes) que
geram **objetos** (instâncias). Cada classe tem **atributos** (dados) e **métodos** (ações).

### O que é Spring e Spring Boot?
**Spring** é um conjunto de ferramentas que facilita criar aplicações Java grandes. **Spring Boot**
é o Spring "já configurado": liga um servidor web, conecta ao banco e monta tudo sozinho, para
você focar na lógica. Ele usa muita **anotação** — palavras com `@` (ex.: `@RestController`) que
dão instruções ao framework.

### O que é Maven?
Ferramenta que **gerencia as dependências** (as bibliotecas que o projeto usa) e **constrói** o
programa. A "lista de compras" fica no arquivo `pom.xml`. O IntelliJ lê esse arquivo e baixa tudo.

### O que é JPA / Hibernate?
**JPA** é a "regra" do Java para transformar **objetos** (classes) em **linhas de tabela** no
banco, sem escrever SQL manualmente. O **Hibernate** implementa essa regra nos bastidores. Salvar
um objeto `Tarefa` vira um `INSERT` automaticamente. A isso chamamos **ORM** (Mapeamento
Objeto-Relacional).

### O que é um banco de dados?
É onde os dados ficam **guardados de forma permanente**, em tabelas (linhas e colunas). Aqui
usamos **H2** (em memória, para desenvolver) e, opcionalmente, **PostgreSQL** (de verdade).

---

## 2. Como o HTTP funciona (verbos e status)

Toda conversa com a API usa **HTTP**. Dois conceitos importam:

### Verbos (o que você quer fazer)
| Verbo | Significado | Exemplo neste projeto |
|---|---|---|
| `GET` | Ler/consultar | listar ou buscar tarefas |
| `POST` | Criar | cadastrar uma tarefa nova |
| `PUT` | Substituir (todos os campos) | atualizar uma tarefa inteira |
| `PATCH` | Atualizar um pedaço | mudar só o status |
| `DELETE` | Apagar | excluir uma tarefa |

### Códigos de status (como a API respondeu)
| Código | Família | Significado |
|---|---|---|
| `200 OK` | 2xx sucesso | deu certo, com corpo na resposta |
| `201 Created` | 2xx sucesso | criou um recurso novo |
| `204 No Content` | 2xx sucesso | deu certo, sem corpo (ex.: delete) |
| `400 Bad Request` | 4xx erro do cliente | você enviou dados inválidos |
| `404 Not Found` | 4xx erro do cliente | o recurso não existe |
| `500 Internal Server Error` | 5xx erro do servidor | bug na aplicação |

**Diferença entre 400 e 404:** `400` é "o que você mandou está errado" (ex.: título vazio);
`404` é "o que você procurou não existe" (ex.: tarefa com ID 999).

---

## 3. A arquitetura em camadas

O projeto separa responsabilidades em **camadas**. Cada camada só conversa com a vizinha. Isso
deixa o código organizado, testável e fácil de mudar.

```
Postman / Navegador
        │  (requisição HTTP + JSON)
        ▼
┌───────────────────┐
│    Controller     │  recebe a requisição, define rotas e códigos HTTP
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

Apoios ao longo do caminho:
- **Model** (`Tarefa`, enums): o domínio e as tabelas.
- **DTOs** (`TarefaRequest`, `TarefaResponse`...): o formato de entrada e saída da API.
- **Exception**: tratamento de erros padronizado.

**Por que separar?** Se amanhã você trocar o banco, mexe só no Repository. Se mudar uma regra,
mexe só no Service. Se mudar a URL, mexe só no Controller. Cada mudança fica isolada.

---

## 4. O caminho de uma requisição, passo a passo

Vamos seguir um `POST /tarefas` do início ao fim:

1. **Postman** envia o JSON com título, prioridade etc.
2. O **Controller** (`criar`) recebe. O `@RequestBody` transforma o JSON em um objeto
   `TarefaRequest`. O `@Valid` checa as regras (título não vazio, tamanho etc.).
3. Se os dados forem inválidos, o fluxo para e o **ManipuladorDeErros** devolve `400`.
4. Se forem válidos, o Controller chama o **Service** (`criar`).
5. O **Service** cria um objeto `Tarefa`, copia os dados do DTO e chama o **Repository**.
6. Antes de salvar, o método `@PrePersist` da entidade define `status = PENDENTE` e `criadaEm`.
7. O **Repository** (via JPA/Hibernate) executa um `INSERT` no banco e devolve a tarefa com `id`.
8. O Service devolve a entidade ao Controller.
9. O Controller converte a entidade em `TarefaResponse` e responde `201 Created` com o JSON.

---

## 5. Explicação arquivo por arquivo

### `TarefasApplication.java` — o ponto de partida
```java
@SpringBootApplication
public class TarefasApplication {
    public static void main(String[] args) {
        SpringApplication.run(TarefasApplication.class, args);
    }
}
```
É onde a aplicação **começa**. `@SpringBootApplication` liga o "piloto automático" do Spring: ele
procura suas classes, configura o servidor web e conecta ao banco. O `main` é o primeiro método
que o Java executa.

### `model/StatusTarefa.java` e `model/Prioridade.java` — os enums
Um **enum** é uma lista fixa de valores possíveis. Em vez de guardar o status como texto livre
(sujeito a erro de digitação), só permitimos `PENDENTE`, `EM_ANDAMENTO` e `CONCLUIDA`. Isso evita
dados inválidos e usa a **linguagem do negócio** dentro do código (um princípio de DDD).

### `model/Tarefa.java` — a entidade
É a classe que representa **uma tarefa** e, ao mesmo tempo, **uma linha da tabela** `tarefas`.
Pontos-chave:
- `@Entity` + `@Table(name = "tarefas")`: "esta classe vira uma tabela".
- `@Id` + `@GeneratedValue(strategy = IDENTITY)`: o `id` é a chave primária, gerada pelo banco.
- `@Column(nullable = false, length = 100)`: regras da coluna (não nula, limite de tamanho).
- `@Enumerated(EnumType.STRING)`: salva o enum como texto (`"ALTA"`) e não como número — assim o
  banco fica legível e resistente a mudanças de ordem.
- `@PrePersist` no método `aoCriar()`: roda **antes do primeiro INSERT**, definindo `status`
  inicial e `criadaEm`. Garante a regra "toda tarefa nasce pendente" **sem depender do cliente**.
- **Getters e setters**: métodos padrão do Java para ler (`getTitulo`) e alterar (`setTitulo`) os
  campos, que são `private` (encapsulamento).

### `repository/TarefaRepository.java` — o acesso ao banco
É uma **interface** (um contrato, sem corpo). Ao escrever `extends JpaRepository<Tarefa, Long>`,
ganhamos de graça métodos como `save`, `findById`, `findAll` e `deleteById` — o Spring Data
**implementa tudo sozinho** em tempo de execução. Também declaramos:
```java
List<Tarefa> findByStatus(StatusTarefa status);
```
O Spring **lê o nome do método** e cria a consulta `SELECT * FROM tarefas WHERE status = ?`
automaticamente. Isso se chama **query derivada** (derived query).

### `dto/TarefaRequest.java` — dados que entram
**DTO** = Data Transfer Object (objeto de transferência de dados). É o formato que o cliente
**envia** ao criar/atualizar. Por que não usar a entidade `Tarefa` diretamente? Porque o cliente
**não deve** definir `id`, `status` inicial ou `criadaEm` — esses são controlados pelo servidor.
As anotações **validam** a entrada:
- `@NotBlank`: o título não pode ser vazio nem só espaços.
- `@Size(min = 3, max = 100)`: tamanho permitido.
- `@NotNull`: a prioridade é obrigatória.

Se a validação falhar, o Spring devolve **400** antes de executar a regra de negócio.

### `dto/AtualizarStatusRequest.java` — só o status
Um DTO pequeno, usado no `PATCH`, que carrega apenas o novo `status` (com `@NotNull`).

### `dto/TarefaResponse.java` — dados que saem
É o formato que a API **devolve**. Foi feito com `record` (recurso do Java 17+): uma classe
**imutável** e enxuta, ideal para transportar dados (gera construtor e getters automaticamente).
O método estático `de(...)` **converte** uma entidade `Tarefa` nesse objeto de resposta. Separar
entrada e saída do banco é boa prática: a API decide exatamente o que mostrar.

### `service/TarefaService.java` — as regras de negócio
É o **cérebro** da aplicação. O Controller não decide nada: ele pede ao Service. Aqui estão as
operações `criar`, `listar`, `buscarPorId`, `atualizar`, `alterarStatus` e `excluir`. Quando uma
tarefa não existe, lançamos a exceção de "não encontrada". Repare na **injeção de dependência por
construtor**:
```java
public TarefaService(TarefaRepository repository) {
    this.repository = repository;
}
```
Nós não criamos o repositório com `new`; o Spring **entrega** uma instância pronta. Isso desacopla
as classes e facilita testes (podemos passar um repositório "falso" nos testes). O
`orElseThrow(...)` em `buscarPorId` é o que garante o `404`.

### `controller/TarefaController.java` — as rotas HTTP
É a **porta de entrada** da API. Cada método vira um endpoint:
- `@RestController` + `@RequestMapping("/tarefas")`: todas as rotas começam com `/tarefas`.
- `@PostMapping`, `@GetMapping`, `@PutMapping`, `@PatchMapping`, `@DeleteMapping`: ligam o método
  ao verbo HTTP.
- `@PathVariable`: pega um valor da URL (o `1` em `/tarefas/1`).
- `@RequestParam(required = false)`: pega um parâmetro da query (`?status=PENDENTE`), opcional.
- `@RequestBody`: transforma o JSON recebido em objeto Java.
- `@Valid`: dispara a validação do DTO.
- `ResponseEntity`: controla o **código HTTP** (201, 204 etc.).

O Controller **não tem regra de negócio** — ele só traduz HTTP ↔ Java e delega ao Service.

### `exception/TarefaNaoEncontradaException.java` — erro de "não achei"
Uma exceção personalizada que estende `RuntimeException`, lançada quando buscam um ID inexistente.
Carrega a mensagem "Tarefa nao encontrada".

### `exception/ManipuladorDeErros.java` — tratamento central de erros
Com `@RestControllerAdvice`, esta classe **intercepta os erros** de toda a API e devolve respostas
padronizadas em JSON:
- `TarefaNaoEncontradaException` → **404 Not Found**.
- `MethodArgumentNotValidException` (falha do `@Valid`) → **400** com a lista de campos inválidos.
- `HttpMessageNotReadableException` (JSON malformado ou enum inválido) → **400**.

Centralizar isso evita repetir `try/catch` em todo Controller.

### `application.properties` — configuração
Diz ao Spring qual banco usar. Por padrão, o **H2 em memória**. Há um bloco comentado para o
**PostgreSQL**. Também habilita o console web do H2 e o log do SQL gerado (`show-sql`).

### `pom.xml` — as dependências (Maven)
Lista as "peças" do projeto: Spring Web (API REST), Spring Data JPA (banco), Validation
(validações), H2 e PostgreSQL (drivers) e as ferramentas de teste. O `spring-boot-starter-parent`
define versões compatíveis entre si, evitando conflitos.

### `src/test/.../TarefasApplicationTests.java` — teste de fumaça
Um teste mínimo (`contextLoads`) que verifica se a aplicação **sobe sem erros**. É o primeiro
sinal de que o projeto está saudável.

---

## 6. As anotações do Spring e do JPA explicadas

| Anotação | Camada | O que faz |
|---|---|---|
| `@SpringBootApplication` | principal | liga a autoconfiguração do Spring Boot |
| `@RestController` | controller | marca a classe como controladora de API REST |
| `@RequestMapping` | controller | define o prefixo das rotas |
| `@GetMapping` / `@PostMapping` / etc. | controller | ligam método ↔ verbo HTTP |
| `@PathVariable` | controller | lê um valor da URL |
| `@RequestParam` | controller | lê um parâmetro da query string |
| `@RequestBody` | controller | converte JSON ↔ objeto Java |
| `@Valid` | controller | dispara as validações do DTO |
| `@Service` | service | marca a classe de regras de negócio |
| `@Repository` | repository | marca a interface de acesso a dados |
| `@Entity` / `@Table` | model | mapeiam a classe para uma tabela |
| `@Id` / `@GeneratedValue` | model | chave primária gerada automaticamente |
| `@Column` | model | configura uma coluna |
| `@Enumerated` | model | define como o enum é salvo |
| `@PrePersist` | model | executa código antes do primeiro INSERT |
| `@NotBlank` / `@Size` / `@NotNull` | dto | regras de validação de entrada |
| `@RestControllerAdvice` | exception | tratamento global de erros |
| `@ExceptionHandler` | exception | associa um erro a uma resposta |

---

## 7. DDD e Clean Code neste projeto

### DDD (Domain-Driven Design) — projetar ao redor do domínio
DDD é "modelar o software em torno do **problema real**". Aqui o domínio é "tarefa". Aplicações:
- A classe `Tarefa` representa um **conceito do mundo real**, não uma tabela genérica.
- Os enums `StatusTarefa` e `Prioridade` usam a **linguagem do negócio** (linguagem ubíqua): o
  código "fala" como quem usa o sistema. Em vez de `status = 1`, lemos `status = PENDENTE`.
- As **regras do domínio** (status inicial, data de criação) ficam dentro da entidade (`@PrePersist`).

> Observação honesta: este é um projeto pequeno e didático, então usamos uma **arquitetura em
> camadas** simples, não o DDD "tático" completo (agregados, value objects, repositórios de
> domínio ricos). Mas várias **ideias** de DDD já aparecem aqui.

### Clean Code — código limpo e sustentável
- **Nomes que revelam intenção**: `buscarPorId`, `alterarStatus`, `ManipuladorDeErros`.
- **Responsabilidade única (SRP)**: Controller só HTTP, Service só regras, Repository só dados.
- **Separar entrada/saída do banco** com DTOs: a API não expõe a entidade diretamente.
- **Não se repita (DRY)**: o tratamento de erros está em um único lugar.
- **Baixo acoplamento** via injeção de dependência: classes dependem de abstrações, não de `new`.
- **Falhar com clareza**: erros viram respostas HTTP compreensíveis, nunca travam a API.

---

## 8. Regras de negócio detalhadas

1. O **título** é obrigatório e deve ter de **3 a 100** caracteres (sem contar espaços nas pontas —
   por isso usamos `trim()` no Service).
2. A **descrição** é opcional, com no máximo **500** caracteres.
3. A **prioridade** deve ser `BAIXA`, `MEDIA` ou `ALTA`.
4. Toda tarefa **nasce** com `status = PENDENTE` e `criadaEm` preenchido automaticamente.
5. O **status** pode mudar entre `PENDENTE`, `EM_ANDAMENTO` e `CONCLUIDA`.
6. Buscar, editar, excluir ou mudar o status de um **ID inexistente** retorna `404`.
7. Dados inválidos retornam `400 Bad Request`.
8. Uma lista sem tarefas retorna `[]`, não erro.
9. A exclusão bem-sucedida retorna `204`, sem corpo.

---

## 9. Testes que você deve fazer

Use o Postman (ou o cliente HTTP do IntelliJ) e confira cada cenário:

| Cenário | Entrada | Esperado |
|---|---|---|
| Criar tarefa válida | `POST` com título e prioridade | `201` e ID gerado |
| Criar sem título | `POST` com `titulo` vazio | `400` |
| Criar título curto | `POST` com 2 caracteres | `400` |
| Consultar existente | `GET /tarefas/1` | `200` e JSON |
| Consultar inexistente | `GET /tarefas/999` | `404` |
| Alterar status | `PATCH` com `CONCLUIDA` | `200` e novo status |
| Filtrar pendentes | `GET /tarefas?status=PENDENTE` | apenas pendentes |
| Excluir existente | `DELETE /tarefas/1` | `204` |
| Excluir inexistente | `DELETE /tarefas/999` | `404` |
| Listar vazio | `GET /tarefas` sem registros | `200` com `[]` |

---

## 10. Perguntas de entrevista (com respostas)

**Qual a diferença entre `GET`, `POST`, `PUT`, `PATCH` e `DELETE`?**
`GET` lê, `POST` cria, `PUT` substitui o recurso inteiro, `PATCH` altera parte dele e `DELETE`
remove.

**O que são Controller, Service e Repository?**
Controller recebe/responde HTTP; Service concentra as regras de negócio; Repository acessa o banco.
Cada um tem uma responsabilidade.

**O que é uma entidade JPA?**
Uma classe anotada com `@Entity` que o JPA mapeia para uma tabela; cada objeto vira uma linha.

**Qual a diferença entre `400` e `404`?**
`400` = o cliente enviou dados inválidos. `404` = o recurso solicitado não existe.

**Como testar uma API sem frontend?**
Com ferramentas como Postman ou Insomnia, enviando requisições HTTP e inspecionando as respostas.

**Por que uma classe DTO ajuda a separar entrada de dados e banco?**
Porque o DTO controla exatamente quais campos entram/saem, impedindo o cliente de mexer em campos
internos (id, datas, status inicial) e protegendo a entidade de persistência.

**O que é injeção de dependência?**
É o framework fornecer os objetos de que uma classe precisa (em vez de ela criá-los com `new`),
reduzindo acoplamento e facilitando testes.

---

## 11. Glossário completo

| Termo | Significado simples |
|---|---|
| CRUD | Criar, consultar, atualizar e excluir registros. |
| REST | Estilo de organizar APIs usando HTTP. |
| HTTP | Protocolo de comunicação da web (pedido/resposta). |
| JSON | Formato de texto para trocar dados. |
| Endpoint | Um endereço + ação da API (ex.: `GET /tarefas`). |
| Verbo HTTP | A ação do pedido (GET, POST, PUT, PATCH, DELETE). |
| Status HTTP | Código da resposta (200, 201, 400, 404...). |
| Controller | Recebe a requisição HTTP e devolve a resposta. |
| Service | Contém as regras de negócio. |
| Repository | Faz o acesso aos dados. |
| Entity | Classe que representa uma tabela do banco. |
| DTO | Objeto usado para receber ou devolver dados na API. |
| JPA | Especificação Java para mapear objetos em tabelas. |
| Hibernate | Implementação do JPA que roda nos bastidores. |
| ORM | Mapeamento Objeto-Relacional (objeto ↔ tabela). |
| Enum | Lista fixa de valores possíveis. |
| Anotação (`@`) | Instrução para o Spring/JPA sobre como tratar a classe/método. |
| Injeção de dependência | O Spring entrega objetos prontos, sem usar `new`. |
| Bean | Objeto gerenciado pelo Spring. |
| Validação | Checagem automática dos dados de entrada. |
| Encapsulamento | Esconder os campos (private) e acessá-los por métodos. |
| H2 | Banco simples, em memória, para desenvolvimento. |
| PostgreSQL | Banco de dados robusto usado em produção. |
| Maven | Gerenciador de dependências e build (`pom.xml`). |
| DDD | Projetar o software ao redor do domínio/problema real. |
| Clean Code | Práticas para um código legível e sustentável. |
