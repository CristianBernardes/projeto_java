# Guia de estudo: entrevista Java / Grails / Spring Boot / PostgreSQL

Vaga: Java com Grails, GORM/Hibernate, PostgreSQL, APIs REST, Docker, Spring Boot, Clean Code/SOLID/DDD/Design Patterns. Desejáveis: Go/PHP, Bootstrap, mensageria (RabbitMQ/Kafka), alta disponibilidade.

Como usar: cada seção tem **o que perguntam**, **a resposta de expert** e **armadilhas**. As respostas são curtas de propósito: é para você falar em 30 a 90 segundos.

---

## 0. Estratégia de posicionamento (leia primeiro)

Você é um sênior com 20 anos, forte em PHP/Laravel, e a vaga é Java. O entrevistador vai testar uma coisa: **você aprende rápido e entende o que está por baixo?** Faça o seguinte:

1. **Seja honesto sobre a proporção.** Diga: "Meus últimos anos foram mais em PHP/Node, mas já trabalhei com Java (Android no Exército, migração de legados Java na Axxin, Spring Boot) e os conceitos são os mesmos: ORM, injeção de dependência, filas, transações."
2. **Traduza o que você sabe para o vocabulário Java.** A tabela abaixo é sua ponte.
3. **Nunca blefe em Hibernate/Grails.** Se não souber, diga o que sabe do conceito equivalente e como descobriria. Blefe é pior que lacuna.
4. **Mostre profundidade em banco, mensageria e arquitetura.** Aí você compete de igual.

### Tabela de tradução (Laravel/PHP para Java)

| Você conhece | Equivalente Java/Grails |
|---|---|
| Service Container / Service Providers | Spring IoC Container / `@Configuration` + `@Bean` |
| Eloquent | Hibernate/JPA (Spring Data JPA) / GORM no Grails |
| Migrations | Flyway / Liquibase (no Grails: `database-migration` plugin) |
| Form Requests | Bean Validation (`@Valid`, `@NotNull`) / command objects no Grails |
| Middleware | Servlet Filter / `HandlerInterceptor` / Spring Security filter chain |
| Queues/Jobs | `@Async`, Spring AMQP, Spring Kafka, Quartz |
| Artisan | Gradle tasks, Grails commands (`grails create-controller`) |
| Policies/Gates | Spring Security (`@PreAuthorize`) |
| PHPUnit/Pest | JUnit 5, Mockito, Spock (muito usado em Grails), Testcontainers |
| Composer | Gradle / Maven |
| php-fpm (processo por request) | Servlet container (Tomcat) com thread pool: **memória compartilhada entre requests** |

> Ponto-chave de maturidade: em PHP-FPM cada request nasce e morre. Em Java a aplicação é um processo longevo com estado compartilhado, então **thread-safety, memória e GC passam a ser problema seu**. Diga isso na entrevista.

---

## 1. Java core

### 1.1 JVM, JRE, JDK, memória e GC

**Pergunta:** "Como a JVM organiza a memória?"

- **Heap:** objetos. Dividido em Young (Eden + Survivor S0/S1) e Old (Tenured). Objetos novos nascem no Eden; sobrevivem a minor GCs e são promovidos ao Old.
- **Metaspace** (substituiu PermGen no Java 8): metadados de classes, fora do heap, em memória nativa.
- **Stack (por thread):** frames de métodos, variáveis locais e referências. `StackOverflowError` vem daqui.
- **Code cache:** código compilado pelo JIT.

**Pergunta:** "Quais coletores de lixo existem?"

| GC | Quando |
|---|---|
| Serial | apps pequenas, single core |
| Parallel | throughput máximo, pausas aceitáveis |
| **G1** (padrão desde o Java 9) | equilíbrio, heaps médios/grandes, pausas previsíveis |
| ZGC / Shenandoah | pausas muito baixas (sub-milissegundo a poucos ms), heaps grandes |

**Armadilhas:** dizer que "o GC resolve vazamento de memória". Vazamento em Java existe: referência viva que ninguém usa (cache estático sem limite, listeners não removidos, `ThreadLocal` sem `remove()`, classloader leak).

**Como diagnosticar:** `jcmd`, `jmap -histo`, heap dump + Eclipse MAT, `jstack` para threads, `-Xlog:gc*`, JFR (Java Flight Recorder), Micrometer + Actuator.

**Flags úteis para container:** `-XX:MaxRAMPercentage=75`, `-XX:+UseG1GC`, `-XX:+HeapDumpOnOutOfMemoryError`. Desde o Java 10 a JVM respeita limites de cgroup (container aware).

### 1.2 Tipos, String e imutabilidade

- Primitivos (`int`, `long`...) vs wrappers (`Integer`...). Autoboxing custa e pode gerar `NullPointerException` no unboxing.
- `Integer` cache de -128 a 127: `Integer a=127,b=127; a==b` é `true`; com 128 é `false`. Sempre use `equals`.
- `String` é imutável, fica em String Pool (literais). `new String("a")` cria outro objeto. `intern()` força o pool.
- `StringBuilder` (não thread-safe, rápido) vs `StringBuffer` (synchronized, legado).
- Imutabilidade: classe `final`, campos `private final`, sem setters, cópia defensiva de coleções mutáveis. Vantagem: thread-safe de graça.

### 1.3 `equals` e `hashCode`

Contrato: se `a.equals(b)` então `a.hashCode()==b.hashCode()`. A volta não vale. Violar isso quebra `HashMap`/`HashSet`.

**Em entidade JPA:** não use todos os campos nem o `id` gerado sem cuidado (o id é `null` antes do persist e o hash muda dentro de um `Set`). Opções: chave de negócio estável ou `hashCode` constante com `equals` baseado em id quando não nulo.

### 1.4 Collections (pergunta clássica: HashMap por dentro)

- **HashMap:** array de buckets (capacidade inicial 16, load factor 0.75). Índice = `hash(key) & (n-1)`. Colisão: lista encadeada; a partir de 8 elementos no bucket **e** tabela com pelo menos 64 posições, vira **árvore rubro-negra** (Java 8+). Resize dobra a capacidade e redistribui.
- **Não é thread-safe.** Em concorrência use `ConcurrentHashMap`.
- **ConcurrentHashMap (Java 8+):** CAS + `synchronized` no primeiro nó do bucket (antes era segmentação). Não aceita `null` como chave nem valor. Leituras não bloqueiam. `compute`, `merge`, `computeIfAbsent` são atômicos.
- **LinkedHashMap:** mantém ordem de inserção (ou de acesso: base de cache LRU).
- **TreeMap:** ordenado, O(log n).
- **ArrayList vs LinkedList:** `ArrayList` quase sempre ganha (cache de CPU). `LinkedList` só em casos muito específicos.
- **`List.of()`, `Map.of()`:** imutáveis (Java 9+). `Arrays.asList` é tamanho fixo.
- **Fail-fast iterators:** `ConcurrentModificationException`. Remova via `Iterator.remove()` ou `removeIf`.
- **Escolha de estrutura:** fila de prioridade: `PriorityQueue`; deque: `ArrayDeque`; ordenação estável concorrente: `ConcurrentSkipListMap`.

### 1.5 Generics

- **Type erasure:** informação de tipo some em runtime. Por isso `List<String>` e `List<Integer>` são o mesmo `List` em execução, e não dá `new T()`.
- **PECS:** *Producer Extends, Consumer Super*. `List<? extends Number>` para ler; `List<? super Integer>` para escrever.

### 1.6 Exceções

- **Checked** (`IOException`): obriga tratar ou declarar. **Unchecked** (`RuntimeException`): não obriga.
- Boa prática atual: exceções de domínio unchecked, tratadas em um ponto central (`@ControllerAdvice`).
- Nunca engula exceção (`catch (Exception e) {}`). Sempre logue com a causa ou relance encapsulando (`new X("msg", e)`).
- `try-with-resources` para `AutoCloseable`.
- **Pegadinha:** `finally` com `return` engole exceção.
- **Spring:** `@Transactional` só faz rollback por padrão em unchecked (`RuntimeException`/`Error`). Para checked: `rollbackFor`.

### 1.7 Java moderno (LTS: 8, 11, 17, 21, 25)

Saiba o que cada marco trouxe, porque perguntam "qual versão você usa e o que mudou":

- **Java 8:** lambdas, streams, `Optional`, `java.time`, `CompletableFuture`, default methods.
- **Java 10/11:** `var` (inferência local), `HttpClient`, métodos novos em `String`.
- **Java 14-17:** `record`, `switch` expression, text blocks, `sealed classes`, pattern matching para `instanceof`.
- **Java 21:** **virtual threads** (Project Loom), pattern matching para `switch`, record patterns, sequenced collections.
- **Java 25 (LTS, setembro/2025):** consolida recursos recentes; confirme o que a empresa usa.

```java
// Streams: agrupar e somar (pergunta comum de live coding)
Map<String, BigDecimal> totalPorCliente = pedidos.stream()
    .collect(Collectors.groupingBy(
        Pedido::getCliente,
        Collectors.mapping(Pedido::getValor,
            Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
```

**Cuidados com stream:** não use `parallelStream()` por reflexo (usa o `ForkJoinPool.commonPool` compartilhado, ruim para I/O). Não altere estado externo dentro de `map`/`filter`. `Optional` serve para retorno, não para campo nem parâmetro.

### 1.8 Concorrência (separa júnior de sênior)

- **Thread vs Runnable vs Callable vs Future.**
- **`ExecutorService`:** nunca crie `new Thread` solto em produção; use pools nomeados e dimensionados. Dimensionamento: CPU-bound ≈ núcleos; I/O-bound maior (fórmula: `núcleos × (1 + espera/computação)`).
- **`synchronized` vs `ReentrantLock`:** `ReentrantLock` permite `tryLock`, timeout, fairness e múltiplas `Condition`.
- **`volatile`:** garante **visibilidade** e ordenação (happens-before), **não** atomicidade. `count++` com `volatile` continua com race condition. Use `AtomicInteger`/`LongAdder`.
- **Java Memory Model:** happens-before; publicação segura de objetos.
- **Deadlock:** 4 condições (exclusão mútua, hold and wait, sem preempção, espera circular). Prevenção: ordem global de aquisição de locks, timeouts. Detecção: `jstack`.
- **`CompletableFuture`:** composição assíncrona (`thenApply`, `thenCompose`, `thenCombine`, `exceptionally`, `allOf`). Passe o executor explicitamente.
- **Virtual threads (Java 21):** threads leves gerenciadas pela JVM. Ótimas para I/O bloqueante em alta concorrência (um thread por request sem estourar memória). Cuidado com *pinning* (bloco `synchronized` segurando I/O em versões anteriores à 24) e não faça pool de virtual threads.
- **`ThreadLocal`:** cuidado com vazamento em pools de thread; limpar com `remove()`.
- **Spring é singleton por padrão**, então **bean com estado mutável é bug de concorrência**. Fale isso.

---

## 2. OOP, SOLID, Design Patterns, Clean Code, DDD

### 2.1 SOLID com exemplos curtos

- **S**: uma razão para mudar. Ex.: `PedidoService` que calcula imposto, envia e-mail e gera PDF viola.
- **O**: aberto para extensão, fechado para modificação. Ex.: nova regra de frete = nova classe `FreteStrategy`, sem `if/else` crescendo.
- **L**: subtipo substitui o tipo base sem quebrar contrato. Ex.: `Quadrado extends Retangulo` quebra.
- **I**: interfaces pequenas e coesas.
- **D**: dependa de abstrações; injete dependências. Ex.: serviço recebe `PagamentoGateway` (interface), não `StripeClient`.

### 2.2 Design Patterns que mais caem (e onde você já usou)

| Pattern | Uso real que você pode citar |
|---|---|
| Strategy | Worker em Go do SorteLab por modalidade; provedores de pagamento (Pix/NuPay) |
| Repository | Eloquent/Spring Data |
| Factory | criação de gateways por tipo |
| Observer | Events/Listeners no Laravel; `ApplicationEventPublisher` no Spring |
| Adapter | integração com serviços de terceiros/Banco Central |
| Decorator | middlewares/filters; Spring AOP |
| Template Method | classes base de jobs |
| Singleton | beans Spring (cuidado com estado) |
| Builder | objetos com muitos parâmetros (Lombok `@Builder`) |
| Circuit Breaker | Resilience4j (integrações) |

Pergunta de armadilha: "Singleton é anti-pattern?" Resposta: o *GoF Singleton* com estado global estático é problemático (testabilidade, concorrência); singleton gerenciado por container de DI é aceitável.

### 2.3 Clean Code

Nomes que revelam intenção, funções pequenas, poucos parâmetros, sem efeitos colaterais escondidos, tratamento de erro sem retornar `null`, comentários só para o "porquê", testes legíveis (Arrange-Act-Assert), regra do escoteiro. Composição acima de herança.

### 2.4 DDD (a vaga cita explicitamente: estude)

- **Linguagem ubíqua:** o código usa o vocabulário do negócio.
- **Bounded Context:** limite onde um modelo e seus termos têm um significado único (ex.: "Cliente" em Vendas vs em Cobrança).
- **Entity:** identidade. **Value Object:** imutável, igualdade por valor (`Dinheiro`, `CPF`, `Endereco`).
- **Aggregate e Aggregate Root:** cluster de objetos com invariantes consistentes; acesso externo só pela raiz; **uma transação por agregado**; referência entre agregados por id.
- **Domain Service:** regra que não pertence a uma entidade. **Domain Event:** algo relevante que aconteceu (`PedidoPago`).
- **Repository:** abstração de persistência por agregado.
- **Application Service:** orquestra casos de uso e transações; sem regra de negócio.
- **Anti-corruption layer:** protege seu modelo de sistemas legados/terceiros.
- **Modelo anêmico (anti-pattern):** entidades só com getters/setters e toda lógica em services.

> Pergunta provável: "Como DDD se encaixa em Grails?" Grails é convention-over-configuration e orientado a domain classes (GORM). Dá para aplicar DDD, mas a tentação é deixar a domain class virar tabela anêmica. Mantenha regra de negócio no domínio e na camada de service, e evite lógica em controller.

---

## 3. Spring e Spring Boot

### 3.1 Núcleo

- **IoC/DI:** o container cria e injeta as dependências. **Prefira injeção por construtor** (imutável, testável, falha cedo, sem reflexão). `@Autowired` em campo é desencorajado.
- **Escopos de bean:** `singleton` (padrão), `prototype`, `request`, `session`.
- **Ciclo de vida:** instanciar, injetar, `@PostConstruct`, uso, `@PreDestroy`.
- **Proxy e AOP:** Spring cria proxies (JDK dynamic proxy para interface, CGLIB para classe) para `@Transactional`, `@Async`, `@Cacheable`, `@Secured`.
- **Pegadinha clássica:** **self-invocation**. Chamar `this.metodoTransacional()` de dentro da mesma classe **não passa pelo proxy**, então `@Transactional`/`@Async`/`@Cacheable` não funcionam. Solução: mover para outro bean.
- **Auto-configuração (Boot):** `@SpringBootApplication` = `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`. Starters trazem dependências; `@ConditionalOnClass`/`@ConditionalOnProperty` decidem o que ativar.
- **Profiles e configuração:** `application.yml`, `application-prod.yml`, variáveis de ambiente, `@ConfigurationProperties` (tipado, preferível a `@Value`).
- **Actuator:** `/actuator/health`, `/metrics`, `/prometheus`; liveness/readiness probes.

### 3.2 `@Transactional` (pergunta quase certa)

- Propagação: `REQUIRED` (padrão), `REQUIRES_NEW` (suspende a atual e abre outra), `NESTED`, `SUPPORTS`, `MANDATORY`, `NEVER`.
- Isolamento: delega ao banco (padrão do PostgreSQL: Read Committed).
- `readOnly = true`: otimização e intenção; Hibernate pode desligar dirty checking.
- **Rollback:** apenas unchecked por padrão.
- **Armadilhas:** self-invocation; método não público; transação longa segurando conexão (chamar API externa dentro da transação); `LazyInitializationException` fora da transação; capturar a exceção dentro do método e "engolir" impede o rollback.

### 3.3 Camada web / REST

```java
@RestController
@RequestMapping("/api/v1/pedidos")
class PedidoController {
    private final PedidoService service;
    PedidoController(PedidoService service) { this.service = service; }

    @PostMapping
    ResponseEntity<PedidoDto> criar(@Valid @RequestBody CriarPedidoRequest req) {
        var criado = service.criar(req);
        return ResponseEntity.created(URI.create("/api/v1/pedidos/" + criado.id())).body(criado);
    }
}

@RestControllerAdvice
class ErroHandler {
    @ExceptionHandler(RegraDeNegocioException.class)
    ProblemDetail negocio(RegraDeNegocioException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }
}
```

- **DTO vs entidade:** nunca exponha entidade JPA na API (acoplamento, lazy loading, vazamento de campos, loops de serialização).
- **Bean Validation:** `@Valid`, `@NotBlank`, `@Size`, validadores customizados.
- **Spring MVC (Tomcat, thread por request) vs WebFlux (reativo, event loop):** use WebFlux só com stack inteira não bloqueante; senão MVC + virtual threads costuma ser mais simples.
- **Fluxo de uma request:** Servlet container, `DispatcherServlet`, `HandlerMapping`, interceptors, controller, `HttpMessageConverter` (Jackson), resposta.

### 3.4 Spring Security (o básico que cobram)

- Cadeia de filtros (`SecurityFilterChain`). Autenticação (quem é) vs autorização (o que pode).
- **JWT:** stateless, assinado; cuidado com expiração, revogação e armazenamento. **OAuth2/OIDC:** authorization code + PKCE para front; client credentials para serviço a serviço. Você já tem OAuth2 com o Banco Central no PFE: cite.
- `@PreAuthorize("hasRole('ADMIN')")`, CSRF (relevante para sessão/cookie, desnecessário para API stateless com token no header), CORS.
- Senha: BCrypt/Argon2, nunca MD5/SHA simples.

### 3.5 Testes

- JUnit 5, Mockito, AssertJ. `@SpringBootTest` (integração completa), `@WebMvcTest` (só web), `@DataJpaTest` (só persistência).
- **Testcontainers** para PostgreSQL/Kafka/RabbitMQ/Redis reais. Evite H2 simulando PostgreSQL: o comportamento diverge.
- Pirâmide: muitos unitários, alguns de integração, poucos E2E.
- **Spock** (Groovy) é padrão de fato no ecossistema Grails.

---

## 4. Hibernate / JPA / GORM (o ponto mais sensível para você)

### 4.1 Conceitos que precisam estar na ponta da língua

- **JPA** é a especificação; **Hibernate** é a implementação; **Spring Data JPA** é a camada de repositórios; **GORM** é a camada de persistência do Grails (usa Hibernate por baixo, mas também há GORM para MongoDB etc.).
- **Estados da entidade:** *transient* (novo), *managed/persistent* (no contexto), *detached* (fora do contexto), *removed*.
- **Persistence Context / Session / EntityManager:** cache de 1º nível por sessão/transação; garante identidade (mesma linha = mesmo objeto na sessão).
- **Dirty checking:** Hibernate compara o estado no flush e gera `UPDATE` sozinho para entidades managed. Não precisa chamar `save`.
- **Flush:** sincroniza o contexto com o banco (antes de queries, no commit, ou manual).
- **`merge` vs `persist`:** `persist` torna managed uma entidade nova; `merge` copia o estado de uma detached para uma managed (devolve outra instância).

### 4.2 Lazy vs Eager e o problema N+1

- Padrões JPA: `@ManyToOne`/`@OneToOne` = **EAGER**; `@OneToMany`/`@ManyToMany` = **LAZY**. Boa prática: tornar tudo LAZY e buscar o que precisa por query.
- **N+1:** 1 query traz N pais; acessar o filho lazy de cada um gera N queries extras.
- **Soluções:** `JOIN FETCH` (JPQL), `@EntityGraph`, `@BatchSize`/`hibernate.default_batch_fetch_size`, projeções (DTO direto na query).
- **Cuidado:** `JOIN FETCH` de coleção com paginação gera paginação em memória (warning `HHH000104`). Pagine IDs primeiro ou use batch fetch.
- **`LazyInitializationException`:** acessar associação lazy fora da sessão. Solução correta: buscar o necessário na query; **não** "resolver" com Open Session in View sem entender o custo (mantém conexão aberta durante a renderização).
- Como detectar: ligar log SQL (`hibernate.show_sql` só em dev, ou `datasource-proxy`), estatísticas do Hibernate, testes que contam queries.

### 4.3 Cache

- **1º nível:** por sessão, sempre ligado.
- **2º nível:** por `SessionFactory`, opcional (Ehcache, Infinispan, Redis via provedor). Bom para dados de leitura frequente e quase imutáveis. Exige estratégia de invalidação.
- **Query cache:** raramente vale; invalida com qualquer escrita na tabela.

### 4.4 Concorrência no banco

- **Optimistic locking:** `@Version`. Update falha (`OptimisticLockException`) se outra transação alterou. Ideal para conflito raro.
- **Pessimistic locking:** `@Lock(PESSIMISTIC_WRITE)` gera `SELECT ... FOR UPDATE`. Ideal para conflito frequente (ex.: saldo, estoque), com timeout.
- `SKIP LOCKED` para filas implementadas em tabela.

### 4.5 Performance e boas práticas

- Batch insert/update (`hibernate.jdbc.batch_size`, `order_inserts`). **`GenerationType.IDENTITY` desabilita batch de insert** no Hibernate; prefira `SEQUENCE` no PostgreSQL com `allocationSize`.
- Projeções e DTOs para leitura. Não carregue entidade completa para listar.
- `equals`/`hashCode` corretos (ver 1.3). Evite `@Data` do Lombok em entidade.
- Relacionamento bidirecional: mantenha os dois lados sincronizados com métodos utilitários.
- Migrações com **Flyway/Liquibase**; nunca `hbm2ddl.auto=update` em produção.

### 4.6 GORM (Grails)

```groovy
class Pedido {
    String numero
    BigDecimal total
    Date dateCreated      // preenchido automaticamente
    Date lastUpdated      // idem
    static belongsTo = [cliente: Cliente]
    static hasMany   = [itens: ItemPedido]

    static constraints = {
        numero blank: false, unique: true
        total min: 0.0G
    }
    static mapping = {
        table 'pedidos'
        itens lazy: true, batchSize: 50
        version true
    }
}

// Dynamic finders
Pedido.findByNumero('123')
Pedido.findAllByTotalGreaterThan(100.0G, [max: 20, sort: 'dateCreated', order: 'desc'])

// Where query (compilada, type-checked)
Pedido.where { total > 100 && cliente.nome == 'Ana' }.list()

// Criteria
Pedido.createCriteria().list(max: 20, offset: 0) {
    eq('status', 'PAGO')
    cliente { eq('ativo', true) }
    order('dateCreated', 'desc')
}
```

**O que perguntam e a resposta:**
- **`save()` não grava imediatamente?** Depende da sessão/flush. Use `save(flush: true)` quando precisar, ou `failOnError: true` para lançar exceção em validação falha (por padrão `save()` retorna `null` se a validação falhar).
- **Validação:** `constraints` rodam em `validate()`/`save()`. `errors` guarda as falhas.
- **Transações:** `@Transactional` do Grails (`grails.gorm.transactions.Transactional`) em Services. Services são singletons e transacionais por convenção/declaração.
- **Data Services (GORM 6+):** interfaces com `@Service(Pedido)` e finders declarativos gerados em compilação.
- **N+1 no GORM:** mesmo problema. Use `fetch: 'join'` no mapping, `join` no criteria, `batchSize`, ou `withCriteria` com `join 'itens'`.
- **Dynamic finders** são práticos mas viram ilegíveis e difíceis de otimizar; para consultas complexas use `where`/criteria/HQL.
- **Sessão no Grails:** Open Session In View ligado por padrão em app web (`OpenSessionInView`); entenda a implicação.

---

## 5. Grails (o diferencial da vaga)

Mesmo sem vivência, saiba falar com autoridade sobre a arquitetura.

- **O que é:** framework web full-stack em **Groovy** (roda na JVM, interopera 100% com Java) construído sobre **Spring Boot**, com *convention over configuration* (inspirado em Rails). Grails 5/6 usam Spring Boot 2.x; versões mais novas (7+) migram para Spring Boot 3. **Confirme a versão da vaga**, pois isso determina Java mínimo (`javax` vs `jakarta`).
- **Estrutura:** `grails-app/domain` (entidades GORM), `controllers`, `services`, `views` (GSP), `conf` (application.yml, `UrlMappings`), `src/main/groovy` e `src/main/java` (código comum), `taglib`, `i18n`, `init/BootStrap`.
- **Controller:** ações = métodos; retorna `render`, `respond`, `redirect`. Para API: `RestfulController`, `respond obj`, JSON views.
- **Service:** onde mora a regra de negócio e a transação. Injeção por nome/tipo (`PedidoService pedidoService`).
- **Command Objects:** validação de entrada, equivalente ao Form Request.
- **UrlMappings:** roteamento declarativo.
- **Plugins:** Spring Security Core/REST, Hibernate, Quartz, Asset Pipeline.
- **Groovy que você precisa reconhecer:** tipagem dinâmica opcional (`def`), closures, GStrings (`"Olá ${nome}"`), `?.` (safe navigation), `?:` (Elvis), `==` chama `equals`, listas/mapas literais (`[1,2]`, `[a:1]`), `@CompileStatic` para desempenho e segurança de tipo.
- **Gradle** é o build; `grails run-app`, `grails test-app`, `grails war`.
- **Frontend:** GSP (Groovy Server Pages) + Bootstrap, ou API REST + SPA.

**Pergunta provável:** "Por que Grails e não só Spring Boot?" Resposta equilibrada: produtividade (convenções, scaffolding, GORM), ótimo para CRUD e equipes enxutas; em contrapartida, "mágica" que dificulta depuração, comunidade menor, dinâmica do Groovy pode esconder erros em tempo de execução (mitigue com `@CompileStatic` e testes). **Como a vaga também exige Spring Boot, a empresa provavelmente usa ambos.**

**Plano realista:** instale o Grails (via SDKMAN), crie um app com domain, service, controller REST e teste com Spock **antes da entrevista**. Quem chega dizendo "montei um CRUD Grails ontem à noite" ganha ponto.

---

## 6. PostgreSQL

### 6.1 Índices

- **B-tree:** padrão, igualdade e faixa, `ORDER BY`.
- **Hash:** só igualdade (raro).
- **GIN:** `jsonb`, arrays, full-text. **GiST:** geoespacial, ranges. **BRIN:** tabelas enormes com correlação física (logs por data).
- **Índice composto:** ordem das colunas importa (regra do prefixo mais à esquerda).
- **Índice parcial** (`WHERE ativo`) e **de expressão** (`lower(email)`).
- **Covering index:** `INCLUDE` para *index-only scan*.
- **Custo:** índices aceleram leitura e desaceleram escrita e ocupam espaço.
- **Por que o índice não é usado?** função na coluna, tipos incompatíveis, `LIKE '%x'`, baixa seletividade, estatísticas desatualizadas (`ANALYZE`), o planner achou seq scan mais barato.

### 6.2 `EXPLAIN (ANALYZE, BUFFERS)`

Saiba ler: Seq Scan, Index Scan, Index Only Scan, Bitmap Heap Scan, Nested Loop, Hash Join, Merge Join, estimativa vs real de linhas (grande divergência = estatística ruim), tempo por nó.

### 6.3 MVCC, transações e isolamento

- **MVCC:** cada transação vê um snapshot; leitores não bloqueiam escritores. Linhas "mortas" ficam até o **VACUUM** limpar (autovacuum). Bloat e transações longas atrapalham o vacuum.
- **Níveis:** Read Committed (padrão), Repeatable Read (snapshot da transação, pode dar erro de serialização), Serializable (SSI; exige retry). `Read Uncommitted` se comporta como `Read Committed`.
- **Anomalias:** dirty read, non-repeatable read, phantom read, lost update, write skew.
- **Locks:** `SELECT ... FOR UPDATE`, `FOR UPDATE SKIP LOCKED` (filas), `NOWAIT`, advisory locks.
- **Deadlock:** o Postgres detecta e aborta uma transação. Evite com ordem consistente de acesso.

### 6.4 Outros temas

- **Connection pooling:** HikariCP na aplicação (dimensione pequeno: `(núcleos × 2) + discos` é ponto de partida; mais conexões não significa mais vazão). PgBouncer entre muitas instâncias e o banco.
- **`jsonb`:** flexível e indexável; não substitui modelagem relacional.
- **Particionamento** (range/list/hash) para tabelas gigantes; **réplicas de leitura** (streaming replication); **PITR** com WAL.
- **Paginação:** `OFFSET` degrada em páginas profundas; use *keyset pagination* (`WHERE id > :ultimo ORDER BY id LIMIT n`).
- **CTEs, window functions** (`row_number() over (partition by ...)`), `UPSERT` (`INSERT ... ON CONFLICT DO UPDATE`), `RETURNING`.
- **Tipos:** `numeric` para dinheiro (nunca `float`); `timestamptz` para datas.
- **Migrações online:** `CREATE INDEX CONCURRENTLY`, evitar `ALTER TABLE` com lock longo.

**Pergunta de live-coding SQL:** "segundo maior salário por departamento", "duplicados", "top N por grupo". Treine com `window functions`.

---

## 7. APIs REST

- **Verbos e semântica:** `GET` seguro e idempotente; `PUT` idempotente (substitui); `PATCH` parcial; `DELETE` idempotente; `POST` não idempotente.
- **Status codes:** 200, 201 (+ `Location`), 202 (assíncrono), 204, 400, 401 (não autenticado), 403 (sem permissão), 404, 409 (conflito), 422 (regra de negócio), 429 (rate limit), 500, 502/503/504.
- **Idempotência:** header `Idempotency-Key` para `POST` crítico (pagamentos). Você tem essa história de pagamentos: use.
- **Paginação, filtro, ordenação:** `?page`/`size` ou cursor; devolva metadados.
- **Versionamento:** `/v1` na URL (simples) ou header; mantenha compatibilidade (adicionar campo não quebra).
- **Erros padronizados:** RFC 9457 (`application/problem+json`).
- **Caching HTTP:** `ETag`/`If-None-Match`, `Cache-Control`.
- **Segurança:** HTTPS, autenticação (JWT/OAuth2/API key), validação de entrada, rate limiting (Kong), CORS correto, não vazar stacktrace, OWASP API Top 10 (BOLA/IDOR é o #1: sempre checar se o recurso pertence ao usuário).
- **Documentação:** OpenAPI/Swagger (springdoc).
- **Richardson Maturity Model**, HATEOAS (saiba que existe; poucos aplicam).
- **REST vs gRPC vs GraphQL:** REST universal; gRPC binário e rápido entre serviços; GraphQL para clientes com necessidades variadas.

---

## 8. Mensageria

### 8.1 Por que mensageria? (resposta de arquitetura)

Desacoplamento temporal e de disponibilidade, absorção de picos (buffer), processamento assíncrono, escalabilidade horizontal de consumidores, comunicação orientada a eventos. **Custo:** consistência eventual, complexidade operacional, duplicidade de mensagens, ordem, debugging distribuído.

### 8.2 Garantias de entrega (fundamental)

- **At-most-once:** pode perder, nunca duplica.
- **At-least-once:** nunca perde, **pode duplicar** (o padrão realista).
- **Exactly-once:** só dentro de limites específicos (transações do Kafka entre tópicos). Na prática: **at-least-once + consumidor idempotente**.

**Idempotência no consumidor:** tabela de mensagens processadas (`message_id` único), chave natural com `UPSERT`, ou operação naturalmente idempotente. Você já trabalhou isso na MadeiraMadeira (idempotência e reconciliação): conte essa história.

### 8.3 Apache Kafka

**Modelo mental:** log distribuído, particionado e replicado, **append-only**. Não é fila tradicional: mensagens **não somem ao consumir**; ficam pela retenção, e cada consumer group controla seu *offset*.

**Conceitos:**
- **Topic** dividido em **partitions**. **Ordem só é garantida dentro de uma partição.**
- **Key:** define a partição (hash da chave). Mesma chave = mesma partição = ordem preservada (ex.: `pedidoId`).
- **Producer:** `acks=0|1|all`. Para durabilidade: `acks=all` + `min.insync.replicas=2` + `replication.factor=3`. **Producer idempotente** (`enable.idempotence=true`, padrão nas versões atuais) evita duplicatas por retry.
- **Consumer group:** cada partição é consumida por **um** consumidor do grupo. Paralelismo máximo = número de partições. Grupos diferentes recebem cópias independentes.
- **Offset:** posição de leitura; commit automático ou manual. Commit **depois** de processar = at-least-once. Commit antes = at-most-once.
- **Rebalance:** quando consumidores entram/saem; pausa o consumo. Mitigações: *cooperative sticky assignor*, `max.poll.interval.ms` adequado, processamento rápido por poll, static membership.
- **Retenção e compactação:** por tempo/tamanho, ou *log compaction* (mantém o último valor por chave: bom para estado/changelog).
- **Replicação:** líder + seguidores; **ISR** (in-sync replicas). Falha do líder elege novo da ISR. Cuidado com `unclean.leader.election`.
- **Controle:** Kafka 4.x opera apenas com **KRaft** (ZooKeeper removido). Versões antigas usavam ZooKeeper.
- **Transações e exactly-once semantics (EOS):** `transactional.id`, `read_committed`; vale para o padrão consume-transform-produce dentro do Kafka.
- **Kafka Streams / ksqlDB / Kafka Connect / Schema Registry (Avro/Protobuf):** saiba o papel de cada um.
- **Dead Letter Topic (DLT) e retry topics:** mensagem "veneno" não pode travar a partição: tente N vezes com backoff e envie para o DLT.

**Spring Kafka:**
```java
@KafkaListener(topics = "pedidos", groupId = "faturamento", concurrency = "3")
void consumir(ConsumerRecord<String, PedidoEvent> rec, Acknowledgment ack) {
    processar(rec.value());   // idempotente!
    ack.acknowledge();        // commit manual após sucesso
}
```
Configure `DefaultErrorHandler` com `DeadLetterPublishingRecoverer` e `ExponentialBackOff`.

**Perguntas típicas:**
- *"Como garantir a ordem?"* Mesma chave na mesma partição, um consumidor por partição, sem processamento paralelo desordenado dentro da partição, e cuidado com retries que reordenam (`max.in.flight.requests.per.connection` ≤ 5 com idempotência).
- *"Quantas partições?"* Pelo paralelismo desejado e vazão alvo; aumentar depois muda o mapeamento chave→partição (quebra ordem histórica); reduzir não é possível.
- *"Consumer lag?"* Diferença entre o último offset e o do grupo. Monitorar (Burrow, Prometheus/Grafana). Lag crescente: escalar consumidores (até o nº de partições), otimizar processamento, ou mais partições.
- *"Perdi mensagem, como?"* `acks=1` com falha do líder, commit antes de processar, retenção expirada, consumer reiniciado com `auto.offset.reset=latest`.
- *"Kafka vs RabbitMQ?"* Veja 8.5.

### 8.4 RabbitMQ

**Modelo mental:** *message broker* com roteamento inteligente (protocolo AMQP 0-9-1). Produtor publica em uma **exchange**; a exchange roteia por **bindings** para **queues**; consumidores leem das queues. Mensagem é **removida após ack** (modelo de fila).

**Tipos de exchange:**
- **direct:** routing key exata.
- **topic:** padrões (`pedido.*.criado`, `#`).
- **fanout:** broadcast para todas as filas ligadas.
- **headers:** roteia por cabeçalhos (raro).

**Confiabilidade:**
- **Publisher confirms** (broker confirma o recebimento) + **mandatory/return** para mensagens não roteadas.
- **Consumer ack manual** (`basicAck`/`basicNack`/`basicReject`). Sem ack, a mensagem volta à fila se a conexão cair.
- **Durabilidade:** exchange e queue `durable` + mensagem `persistent`. Sozinha, nenhuma garante.
- **Quorum queues** (Raft) são o tipo recomendado para durabilidade e replicação; **filas clássicas espelhadas foram removidas no RabbitMQ 4.0**. **Streams** (log, estilo Kafka) para replay e alto volume.
- **Prefetch (QoS):** quantas mensagens não confirmadas por consumidor; controla vazão e justiça. Prefetch alto demais concentra trabalho em um consumidor lento.
- **Dead Letter Exchange (DLX):** destino de mensagens rejeitadas, expiradas (TTL) ou que excederam o tamanho da fila. Base de padrões de **retry com delay** (fila de espera com TTL que devolve à fila principal) ou o plugin de delayed message.
- **Poison message:** limite de tentativas (header `x-death`/delivery limit em quorum queues) e DLQ.
- **Work queue / competing consumers:** vários consumidores na mesma fila dividem as mensagens (round-robin). **Pub/Sub:** cada consumidor tem sua fila ligada a um fanout/topic.
- **Ordem:** garantida por fila com um único consumidor; vários consumidores ou reentregas quebram a ordem.
- **Backpressure:** `memory/disk alarms` bloqueiam publishers; filas gigantes degradam. Mantenha filas curtas.
- **Federation/Shovel** para mover mensagens entre brokers.

**Spring AMQP:**
```java
@RabbitListener(queues = "faturamento.pedidos")
void consumir(PedidoEvent evento) { processar(evento); }  // exceção => nack/requeue conforme config
```
Configure `spring.rabbitmq.listener.simple.retry`, `default-requeue-rejected=false` e DLX para evitar loop infinito de requeue.

### 8.5 Kafka vs RabbitMQ (resposta pronta)

| Critério | Kafka | RabbitMQ |
|---|---|---|
| Modelo | log distribuído persistente | broker com filas e roteamento |
| Consumo | pull; mensagens permanecem; replay possível | push; mensagem some após ack |
| Roteamento | simples (topic/partição/chave) | rico (exchanges, bindings) |
| Ordem | por partição | por fila (1 consumidor) |
| Escala/vazão | altíssima (centenas de milhares a milhões msg/s) | alta, menor que Kafka |
| Retenção/replay | sim, nativo | não (exceto Streams) |
| Casos | event streaming, CDC, analytics, event sourcing, integração de alto volume | filas de tarefas, RPC, roteamento complexo, workflows, baixa latência por mensagem |
| Operação | mais pesada | mais leve para começar |

**Regra de bolso:** "preciso reprocessar o histórico, múltiplos consumidores independentes ou volume massivo" = Kafka. "Preciso distribuir tarefas com roteamento flexível e acks por mensagem" = RabbitMQ. Em muitos sistemas coexistem.

### 8.6 Padrões que um sênior cita

- **Transactional Outbox:** grava o evento numa tabela **na mesma transação** da mudança de estado; um processo (poller ou CDC com Debezium) publica no broker. Resolve o *dual write* (salvar no banco e publicar na fila não é atômico).
- **Inbox/idempotent consumer:** guarda IDs processados.
- **Saga:** transação distribuída como sequência de transações locais + compensações (coreografia por eventos ou orquestração).
- **CQRS e Event Sourcing:** separar escrita e leitura; estado como sequência de eventos (use com critério).
- **Retry com backoff exponencial + jitter, DLQ, circuit breaker.**
- **Versionamento de eventos/schema:** compatibilidade retroativa (Schema Registry).

---

## 9. Redis

**Modelo mental:** banco de dados em memória, chave-valor com estruturas ricas. **Execução de comandos em thread única** (I/O pode ser multi-thread nas versões recentes), então cada comando é atômico, e comandos O(N) lentos (`KEYS *`, `SMEMBERS` gigante) travam tudo.

### 9.1 Estruturas e usos

| Estrutura | Uso típico |
|---|---|
| String | cache, contadores (`INCR`), flags, locks |
| Hash | objetos/sessões |
| List | filas simples (`LPUSH`/`BRPOP`), como no seu SorteLab |
| Set | unicidade, tags, interseção |
| Sorted Set | ranking/leaderboard, rate limit por janela, agendamento |
| Bitmap / HyperLogLog | presença diária / contagem aproximada de únicos |
| Stream | log de eventos com consumer groups (`XADD`, `XREADGROUP`, `XACK`) |
| Pub/Sub | broadcast **sem persistência** (mensagem perdida se ninguém ouve) |
| Geo | busca por proximidade |

### 9.2 Padrões de cache

- **Cache-aside (lazy loading):** lê do cache; miss = lê do banco e popula. O mais comum.
- **Read-through / Write-through / Write-behind:** o cache fica na frente do banco com graus diferentes de consistência.
- **TTL sempre** (com *jitter* para evitar expiração simultânea).
- **Invalidação:** a parte difícil. Estratégias: TTL curto, invalidar no write, eventos.
- **Cache stampede (thundering herd):** chave quente expira e milhares de requests batem no banco. Mitigações: lock/single-flight, TTL com jitter, refresh antecipado, serving stale.
- **Cache penetration:** consultas por chaves inexistentes. Mitigação: cachear "não existe" por pouco tempo, Bloom filter.
- **Cache avalanche:** muitas chaves expirando juntas. Mitigação: jitter, alta disponibilidade.
- **Hot key / big key:** uma chave concentra tráfego ou é enorme. Mitigação: replicação local, sharding da chave, quebrar valor.

### 9.3 Persistência, memória e alta disponibilidade

- **RDB** (snapshot periódico, rápido para restaurar, pode perder minutos) e **AOF** (log de comandos, `appendfsync everysec`, perde ~1s). Podem combinar.
- **Eviction (`maxmemory-policy`):** `allkeys-lru`, `allkeys-lfu`, `volatile-lru`, `volatile-ttl`, `noeviction`. Para cache puro: `allkeys-lru`/`lfu`.
- **Replicação** assíncrona (líder/réplicas), então pode haver perda de escritas num failover.
- **Sentinel:** monitora e faz failover automático (HA sem sharding).
- **Cluster:** sharding por **16384 hash slots**; multi-key só se as chaves estiverem no mesmo slot (use *hash tags* `{pedido:1}`).
- **Redis não é banco primário** para dados críticos sem aceitar o risco de durabilidade.

### 9.4 Locks distribuídos e rate limit

```
SET lock:pedido:123 <token-unico> NX PX 30000     # adquire
-- liberar com script Lua: só apaga se o token for o seu
```
- Sempre use **token único** e libere via **Lua** (check-and-delete atômico), com TTL para evitar lock órfão.
- **Redlock** é debatido (Martin Kleppmann vs antirez): para correção forte use *fencing tokens* ou lock no próprio banco. Para "evitar trabalho duplicado" costuma bastar.
- **Rate limit:** fixed window (`INCR` + `EXPIRE`), sliding window (sorted set), token bucket (Lua).

### 9.5 Spring + Redis

`spring-data-redis` (Lettuce, não bloqueante, padrão), `RedisTemplate`, `@Cacheable` com `RedisCacheManager` (defina TTL e serialização JSON; o padrão JDK é frágil). **Redisson** para locks, semáforos, filas.

### 9.6 Redis como fila: quando sim e quando não

Lista com `BRPOPLPUSH`/`LMOVE` ou **Streams** funcionam para cargas simples (seu SorteLab). Para entrega garantida, retry, DLQ e roteamento, use RabbitMQ/Kafka. Lista simples perde a mensagem se o worker morrer após o `BRPOP` sem padrão de confirmação.

---

## 10. Docker e containers

- **Imagem vs container;** camadas e cache de build (ordene `COPY` do que muda menos para o que muda mais).
- **Multi-stage build:** compila em uma imagem com JDK, executa em imagem enxuta com JRE.
- **Java no container:** JVM com `-XX:MaxRAMPercentage`; `jlink`/imagens distroless/Temurin; Spring Boot **layered jars** e buildpacks (`bootBuildImage`); **GraalVM native image** (startup rápido, build lento, menos dinâmico: reflexão exige configuração, algo delicado em Groovy/Grails).
- **Segurança:** usuário não root, imagem mínima, sem segredos na imagem (`ENV`/camadas ficam no histórico), scan de vulnerabilidades, versões fixadas.
- **Docker Compose:** orquestra app + Postgres + Redis + RabbitMQ/Kafka localmente. `depends_on` com `healthcheck`.
- **Rede e volumes:** bridge, rede interna (você usa segmentação no SorteLab), volumes nomeados para dados.
- **`CMD` vs `ENTRYPOINT`;** `docker exec`; `docker logs`; limites de CPU/memória.
- **Kubernetes** (você tem a CKA): pods, deployments, services, probes liveness/readiness/startup, HPA, ConfigMap/Secret, resource requests/limits. Cite se perguntarem sobre produção.

Exemplo de Dockerfile multi-stage:
```dockerfile
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN ./gradlew --no-daemon dependencies
COPY src src
RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:21-jre
RUN useradd -r app
USER app
COPY --from=build /app/build/libs/*.jar /app/app.jar
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-jar","/app/app.jar"]
```

---

## 11. Alta disponibilidade, escalabilidade e resiliência

- **Escalar:** vertical vs horizontal. Para escalar horizontalmente a aplicação precisa ser **stateless** (sessão em Redis/JWT, arquivos em S3, nada local).
- **Gargalo costuma ser o banco:** cache, réplicas de leitura, índices, particionamento, CQRS, filas para suavizar picos.
- **Load balancer** (L4/L7), health checks, *graceful shutdown* (`server.shutdown=graceful`), deploy sem downtime (rolling, blue/green, canary).
- **Teorema CAP:** em partição de rede escolhe-se consistência ou disponibilidade. **PACELC** completa a ideia (sem partição: latência vs consistência).
- **Consistência eventual** e como lidar na UX e no domínio.
- **Resiliência:** timeouts em **toda** chamada externa, retry com backoff + jitter (só em operações idempotentes), **circuit breaker**, **bulkhead** (isolar pools), fallback, rate limiting, *load shedding*. Biblioteca: **Resilience4j**.
- **Observabilidade:** logs estruturados com correlation id, métricas (Micrometer/Prometheus; RED e USE), tracing distribuído (OpenTelemetry), alertas por SLO.
- **SLI/SLO/SLA; RTO/RPO;** backup e teste de restore.
- **Thundering herd, retry storm:** retries sem jitter derrubam o serviço que já está lento.
- **Idempotência + outbox + DLQ** são o trio que mais aparece em "como garantir que não perde nem duplica pedido".

**Pergunta de design clássica:** "Desenhe um sistema de processamento de pedidos/pagamentos." Estruture: requisitos funcionais e não funcionais, estimativa de carga, API, modelo de dados, fluxo síncrono vs assíncrono, onde entra fila, idempotência, consistência (outbox/saga), cache, escalabilidade, falhas, observabilidade. **Fale em voz alta o raciocínio e os trade-offs.**

---

## 12. Go e PHP (desejáveis: aqui você brilha)

- **Go:** goroutines e channels, `context.Context` (cancelamento e timeouts), `defer`, interfaces implícitas, tratamento explícito de erro, `sync.WaitGroup`/`Mutex`, worker pool, graceful shutdown. Sua história: worker em Go consumindo fila Redis no SorteLab, Go Microservice (`rb-ai-proxy`).
- **PHP:** PHP 8.x (tipos, enums, readonly, attributes, match), Laravel, filas, Livewire. A vaga pede apoiar sub-aplicações em Go/PHP: mostre que você é o elo.
- **Comparação madura:** Java (ecossistema, JVM, tipagem forte, maturidade corporativa), Go (binário único, simplicidade, concorrência leve, ótimo para serviços pequenos), PHP (produtividade web, deploy simples).

---

## 13. Perguntas comportamentais e de senioridade

Prepare histórias no formato **STAR** (Situação, Tarefa, Ação, Resultado) com números quando houver:

1. **Uma decisão técnica difícil e o trade-off** (ex.: orquestração de pagamentos com idempotência e reconciliação na MadeiraMadeira).
2. **Um incidente em produção e o que você aprendeu** (causa raiz, correção, prevenção).
3. **Refatoração de legado** (EPROC, migração de Java legado para Node/PHP na Axxin).
4. **Mentoria/liderança técnica** (Spassu, ESA).
5. **Discordância com colega/gestor** e como resolveu.
6. **Aprender tecnologia nova rápido** (Go, Flutter, IA aplicada, e agora Grails).
7. **"Por que sair do PHP para Java?"** Resposta: construo sistemas de alta criticidade e quero aprofundar no ecossistema JVM, em que já tenho base; Java/Spring é o padrão em ambientes corporativos de grande porte e a vaga junta Java com Go/PHP, o que aproveita minha polivalência.
8. **"Onde você se vê?"** Referência técnica da stack, participando de arquitetura.

### Modelos de resposta (adapte com seus números reais)

Use a estrutura **Situação, Tarefa, Ação, Resultado**. Onde houver `[ ]`, preencha com um dado real seu; não invente métrica.

**1. Decisão técnica difícil e trade-off.**
"Na MadeiraMadeira eu atuava na orquestração de pagamentos com vários provedores (Pix, NuPay, gateways) e antifraude. **Situação:** retries e callbacks duplicados podiam gerar cobrança ou status duplicado. **Tarefa:** garantir que a mesma operação nunca produzisse efeito duas vezes. **Ação:** adotei idempotência por chave em todas as operações, workers assíncronos com Kafka/SQS em at-least-once, e uma rotina de reconciliação com o provedor. O trade-off foi aceitar consistência eventual e mais complexidade em troca de não perder nem duplicar transação. **Resultado:** [redução de duplicidades/incidentes, se você souber]."

**2. Incidente em produção.**
"**Situação:** [sintoma e impacto]. **Ação:** primeiro estabilizei (rollback/feature flag), depois investiguei causa raiz com logs, métricas e tracing, em vez de tratar sintoma. **Resultado:** corrigi e adicionei [teste/alerta/timeout] para não repetir. Aprendi a medir antes de mexer e a comunicar o impacto com clareza a produto e liderança."

**3. Refatoração de legado.**
"No EPROC [ou na Axxin, com a migração de sistemas legados Java], modernizei funcionalidades críticas com foco em desacoplamento e performance. Fiz **em passos pequenos**, cobrindo o comportamento atual com testes antes de mexer, extraindo responsabilidades e medindo ganho. Evito reescrever tudo de uma vez: o risco é maior que o benefício."

**4. Mentoria e liderança técnica.**
"Na Spassu e no Exército eu conduzia code reviews, planejamento de sprints e mentoria de juniores e seniores em SOLID, testes e versionamento. Meu foco é ensinar o **porquê** da decisão, para o time ganhar autonomia, não só aplicar regras."

**5. Discordância.**
"Quando discordo, levo **dados e alternativas** (custo, risco, prazo) em vez de opinião, ouço o contra-argumento e, se a decisão for outra, **commito com ela** e acompanho o resultado. [exemplo real em uma frase]."

**6. Aprender tecnologia nova rápido.**
"Aprendi Go construindo o worker do SorteLab e o `rb-ai-proxy`, e Flutter lançando o app na Google Play. Meu método: projeto real pequeno, ler a documentação oficial, comparar com o que já conheço e pedir revisão. Com Grails estou fazendo igual: já montei [um CRUD com domain, service e testes Spock]."

**8. "Onde você se vê?"**
"Como referência técnica da stack, participando de decisões de arquitetura, com ownership de qualidade e performance, e ajudando o time a crescer."

**Pontos fracos aceitáveis (honestos):** "Minha vivência recente em Hibernate/Grails é menor; fechei essa lacuna estudando e construindo um projeto."

**Perguntas para você fazer (mostram nível):**
- Qual a versão do Grails/Java/Spring Boot e plano de atualização?
- Como é a arquitetura hoje: monólito Grails ou serviços? Onde entram Go e PHP?
- Como lidam com mensageria (Kafka/RabbitMQ) e observabilidade?
- Como é o processo de code review e de deploy?
- Qual o maior desafio técnico do time agora?

---

## 14. Exercícios de live coding com solução

Treine digitando sem IDE e **falando em voz alta** o raciocínio e a complexidade (Big-O).

### 14.1 Two Sum (HashMap, O(n))
```java
int[] twoSum(int[] nums, int alvo) {
    Map<Integer,Integer> vistos = new HashMap<>();   // valor -> índice
    for (int i = 0; i < nums.length; i++) {
        Integer j = vistos.get(alvo - nums[i]);
        if (j != null) return new int[]{j, i};
        vistos.put(nums[i], i);
    }
    throw new IllegalArgumentException("sem solução");
}
```
Fale: "força bruta é O(n²); com mapa guardo o complemento e fica O(n) em tempo e O(n) em memória."

### 14.2 Anagrama e palíndromo
```java
boolean anagrama(String a, String b) {
    if (a.length() != b.length()) return false;
    int[] freq = new int[26];                       // assume a-z; senão use Map<Character,Integer>
    for (int i = 0; i < a.length(); i++) { freq[a.charAt(i)-'a']++; freq[b.charAt(i)-'a']--; }
    return Arrays.stream(freq).allMatch(f -> f == 0);
}

boolean palindromo(String s) {
    int i = 0, j = s.length() - 1;
    while (i < j) {
        while (i < j && !Character.isLetterOrDigit(s.charAt(i))) i++;
        while (i < j && !Character.isLetterOrDigit(s.charAt(j))) j--;
        if (Character.toLowerCase(s.charAt(i++)) != Character.toLowerCase(s.charAt(j--))) return false;
    }
    return true;
}
```

### 14.3 FizzBuzz
```java
for (int i = 1; i <= 100; i++) {
    String s = (i % 15 == 0) ? "FizzBuzz" : (i % 3 == 0) ? "Fizz" : (i % 5 == 0) ? "Buzz" : String.valueOf(i);
    System.out.println(s);
}
```

### 14.4 Inverter lista ligada e detectar ciclo
```java
class No { int val; No prox; }

No inverter(No cabeca) {
    No anterior = null, atual = cabeca;
    while (atual != null) {
        No prox = atual.prox;
        atual.prox = anterior;
        anterior = atual;
        atual = prox;
    }
    return anterior;                                // O(n) tempo, O(1) espaço
}

boolean temCiclo(No cabeca) {                       // Floyd: tartaruga e lebre
    No lenta = cabeca, rapida = cabeca;
    while (rapida != null && rapida.prox != null) {
        lenta = lenta.prox;
        rapida = rapida.prox.prox;
        if (lenta == rapida) return true;
    }
    return false;
}
```

### 14.5 LRU Cache
Versão curta (`LinkedHashMap`):
```java
class LruCache<K,V> extends LinkedHashMap<K,V> {
    private final int max;
    LruCache(int max) { super(16, 0.75f, true); this.max = max; }   // true = ordem de acesso
    @Override protected boolean removeEldestEntry(Map.Entry<K,V> e) { return size() > max; }
}
```
Se pedirem "sem `LinkedHashMap`": `HashMap<K, No>` + **lista duplamente ligada** (cabeça = mais recente, cauda = menos recente). `get` move o nó para a cabeça; `put` insere na cabeça e remove a cauda quando passar do limite. Tudo O(1). Para uso concorrente: `synchronized` nos métodos ou `Collections.synchronizedMap`, e diga que em produção usaria Caffeine.

### 14.6 Streams: agrupar, contar, ordenar
```java
Map<String, Long> contagem = palavras.stream()
    .collect(Collectors.groupingBy(String::toLowerCase, Collectors.counting()));

List<Map.Entry<String, Long>> top3 = contagem.entrySet().stream()
    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
    .limit(3)
    .toList();
```

### 14.7 Produtor/consumidor com `BlockingQueue`
```java
BlockingQueue<Integer> fila = new ArrayBlockingQueue<>(10);       // limitada = backpressure

Thread produtor = new Thread(() -> {
    try { for (int i = 0; i < 100; i++) fila.put(i); fila.put(-1); }  // -1 = poison pill
    catch (InterruptedException e) { Thread.currentThread().interrupt(); }
});

Thread consumidor = new Thread(() -> {
    try {
        for (int v; (v = fila.take()) != -1; ) System.out.println("consumiu " + v);
    } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
});
produtor.start(); consumidor.start();
```
Fale: "`put` bloqueia se cheia e `take` bloqueia se vazia; não preciso de `wait/notify`. Sempre restauro a flag de interrupção."

### 14.8 Singleton thread-safe
```java
// 1) Enum (mais simples e à prova de reflexão/serialização)
enum Config { INSTANCE; /* campos e métodos */ }

// 2) Holder idiom (lazy e thread-safe sem lock)
class Servico {
    private Servico() {}
    private static class Holder { static final Servico I = new Servico(); }
    static Servico get() { return Holder.I; }
}
```
Fale: "no Spring eu não escrevo Singleton: o container já gerencia bean singleton."

### 14.9 Rate limiter (token bucket)
```java
class TokenBucket {
    private final long capacidade;
    private final double tokensPorNano;
    private double tokens;
    private long ultimo = System.nanoTime();

    TokenBucket(long capacidade, double tokensPorSegundo) {
        this.capacidade = capacidade;
        this.tokensPorNano = tokensPorSegundo / 1_000_000_000.0;
        this.tokens = capacidade;
    }

    synchronized boolean permitir() {
        long agora = System.nanoTime();
        tokens = Math.min(capacidade, tokens + (agora - ultimo) * tokensPorNano);
        ultimo = agora;
        if (tokens >= 1) { tokens -= 1; return true; }
        return false;
    }
}
```
Fale: "para vários nós uso Redis com script Lua (atômico) ou o rate limit do Kong."

### 14.10 CRUD REST com validação (Spring Boot)
```java
@Entity
class Produto {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) Long id;
    @Column(nullable = false) String nome;
    @Column(nullable = false, precision = 12, scale = 2) BigDecimal preco;
    // getters/setters
}

record ProdutoRequest(@NotBlank String nome, @NotNull @Positive BigDecimal preco) {}
record ProdutoResponse(Long id, String nome, BigDecimal preco) {}

interface ProdutoRepository extends JpaRepository<Produto, Long> {}

@Service
class ProdutoService {
    private final ProdutoRepository repo;
    ProdutoService(ProdutoRepository repo) { this.repo = repo; }

    @Transactional
    ProdutoResponse criar(ProdutoRequest r) {
        var p = new Produto(); p.setNome(r.nome()); p.setPreco(r.preco());
        p = repo.save(p);
        return new ProdutoResponse(p.getId(), p.getNome(), p.getPreco());
    }

    @Transactional(readOnly = true)
    ProdutoResponse buscar(Long id) {
        var p = repo.findById(id).orElseThrow(() -> new NaoEncontradoException("Produto " + id));
        return new ProdutoResponse(p.getId(), p.getNome(), p.getPreco());
    }
}

@RestController
@RequestMapping("/api/v1/produtos")
class ProdutoController {
    private final ProdutoService service;
    ProdutoController(ProdutoService service) { this.service = service; }

    @PostMapping
    ResponseEntity<ProdutoResponse> criar(@Valid @RequestBody ProdutoRequest req) {
        var r = service.criar(req);
        return ResponseEntity.created(URI.create("/api/v1/produtos/" + r.id())).body(r);
    }

    @GetMapping("/{id}")
    ProdutoResponse buscar(@PathVariable Long id) { return service.buscar(id); }
}
```
Fale: "DTO separado da entidade, `BigDecimal` para dinheiro, `@Transactional(readOnly)` na leitura, 201 com `Location`, erro tratado num `@RestControllerAdvice` (seção 3.3)."

### 14.11 SQL
```sql
-- Segundo maior salário por departamento
SELECT departamento_id, salario FROM (
  SELECT departamento_id, salario,
         DENSE_RANK() OVER (PARTITION BY departamento_id ORDER BY salario DESC) AS pos
  FROM funcionarios
) t WHERE pos = 2;

-- Top 3 pedidos por cliente
SELECT * FROM (
  SELECT p.*, ROW_NUMBER() OVER (PARTITION BY cliente_id ORDER BY total DESC) AS rn
  FROM pedidos p
) t WHERE rn <= 3;

-- Duplicados por e-mail
SELECT email, COUNT(*) FROM usuarios GROUP BY email HAVING COUNT(*) > 1;

-- Total acumulado por dia
SELECT dia, valor, SUM(valor) OVER (ORDER BY dia) AS acumulado FROM vendas;
```
Fale: "`DENSE_RANK` não pula posição em empate; `ROW_NUMBER` desempata arbitrariamente; `RANK` pula."

### 14.12 Consumidor idempotente (pseudo-código Java)
```java
@Transactional
void tratar(PedidoPago evento) {
    // tabela mensagens_processadas(id_mensagem PK/UNIQUE)
    try {
        processadas.save(new MensagemProcessada(evento.id()));   // UNIQUE garante 1 vez
    } catch (DataIntegrityViolationException jaProcessada) {
        return;                                                   // duplicata: ignora e confirma
    }
    faturamento.emitirNota(evento.pedidoId());                    // mesma transação do registro
}
// Só confirma (ack/commit offset) depois que a transação comitou.
```
Fale: "o registro da mensagem e o efeito de negócio estão **na mesma transação**; assim retry e duplicata não geram efeito duplo."

---

## 15. Banco de perguntas rápidas **com respostas prontas**

Formato: fale a resposta em 30 a 60 segundos. A primeira frase em negrito é a resposta direta; o resto é o complemento que mostra senioridade.

### Java

**1. Diferença entre `==` e `equals`?**
**`==` compara referências** (nos primitivos, compara valores); **`equals` compara o conteúdo lógico**, desde que a classe o sobrescreva. O `equals` padrão de `Object` é só `==`. Em `String`, `Integer` e objetos em geral use `equals`. Pegadinha: `"a" == "a"` dá `true` por causa do String Pool, e `Integer` de -128 a 127 também (cache), mas `new String("a") == "a"` é `false`. Por isso nunca confie em `==` para objetos.

**2. Por que `String` é imutável?**
**Por segurança, thread-safety e desempenho.** (1) Segurança: strings carregam caminhos, URLs, usuários e nomes de classe; se fossem mutáveis, alguém poderia alterá-las depois da validação. (2) Thread-safety: pode ser compartilhada entre threads sem sincronização. (3) O `hashCode` é calculado uma vez e guardado em cache, o que torna `String` uma ótima chave de `HashMap`. (4) Permite o **String Pool** reaproveitar instâncias. O custo é a concatenação em laço, que cria muitos objetos: use `StringBuilder`.

**3. O que acontece se sobrescrever `equals` sem `hashCode`?**
**Quebra o contrato: dois objetos "iguais" terão hashes diferentes** e cairão em buckets diferentes em `HashMap`/`HashSet`. Resultado: `set.contains(x)` retorna `false` para um objeto logicamente igual ao que está lá, e duplicatas entram no `Set`. A regra é: se `a.equals(b)`, então `a.hashCode() == b.hashCode()`. O inverso não precisa valer (colisão é permitida). Use os mesmos campos nos dois, ou `Objects.equals`/`Objects.hash`, ou `record`.

**4. `HashMap` vs `ConcurrentHashMap` vs `Hashtable`?**
**`HashMap` é rápido e não é thread-safe; `Hashtable` é legado e sincroniza tudo com um lock único; `ConcurrentHashMap` é thread-safe com granularidade fina.** Detalhes: `HashMap` aceita uma chave `null` e valores `null`. `Hashtable` e `ConcurrentHashMap` não aceitam `null`. O `ConcurrentHashMap` (Java 8+) usa CAS e `synchronized` só no primeiro nó do bucket, leituras não bloqueiam, tem operações atômicas (`computeIfAbsent`, `merge`) e iteradores *weakly consistent* (não lançam `ConcurrentModificationException`). Em código novo: `HashMap` quando não há concorrência, `ConcurrentHashMap` quando há. `Hashtable` nunca.

**5. `final`, `finally`, `finalize`?**
**São três coisas sem relação.** `final`: variável que não pode ser reatribuída, método que não pode ser sobrescrito, classe que não pode ser estendida. `finally`: bloco que executa depois de `try/catch` em quase todos os casos (exceto `System.exit`, crash da JVM ou laço infinito); serve para liberar recursos, mas prefira `try-with-resources`. `finalize`: método de `Object` chamado pelo GC antes de coletar, **depreciado (Java 9) e marcado para remoção**, imprevisível; use `AutoCloseable` ou `Cleaner`.

**6. Checked vs unchecked?**
**Checked o compilador obriga a tratar ou declarar (`throws`); unchecked não.** Checked estende `Exception` (exceto `RuntimeException`), ex.: `IOException`, `SQLException`, e representa falha recuperável esperada. Unchecked estende `RuntimeException` ou `Error`, ex.: `NullPointerException`, `IllegalArgumentException`, e normalmente indica bug. Hoje a prática dominante é usar **unchecked para exceções de domínio** e tratá-las em um ponto central (`@RestControllerAdvice`), porque checked polui assinaturas e atrapalha lambdas. No Spring, **`@Transactional` só faz rollback por padrão em unchecked**; para checked use `rollbackFor`.

**7. O que é *happens-before*?**
**É a garantia do Java Memory Model de que o efeito de uma ação é visível e ordenado para outra.** Sem um happens-before entre duas threads, uma pode nunca ver a escrita da outra, por causa de cache de CPU e reordenações do compilador/JIT. Regras principais: ordem do programa dentro da mesma thread; `unlock` de um monitor acontece antes do `lock` seguinte do mesmo monitor; escrita em `volatile` acontece antes de uma leitura posterior; `Thread.start()` acontece antes das ações da thread iniciada; ações da thread acontecem antes de `join()` retornar; e a publicação segura via `final`. Na prática: use `synchronized`, `volatile`, `Atomic*` ou coleções concorrentes para criar essas arestas.

**8. `volatile` torna `i++` seguro?**
**Não.** `volatile` garante **visibilidade** e ordem, mas não **atomicidade**. `i++` é ler, somar e escrever (três passos); duas threads podem ler o mesmo valor e uma escrita se perde. Soluções: `AtomicInteger.incrementAndGet()` (CAS), `LongAdder` para contadores com muita contenção, ou `synchronized`/`Lock`. `volatile` serve para flags (`volatile boolean parar`) e publicação de referência imutável.

**9. Como funciona o GC geracional?**
**Baseia-se na hipótese de que a maioria dos objetos morre jovem.** O heap tem **Young** (Eden + dois Survivors) e **Old**. Objetos nascem no Eden; no *minor GC* os vivos são copiados para um Survivor e vão envelhecendo; ao atingir um limite de idade são promovidos para o Old. O Old é coletado com menos frequência (*major/mixed GC*), que é mais caro. O **G1** (padrão) divide o heap em regiões e coleta primeiro as com mais lixo, mirando pausas previstas. **ZGC/Shenandoah** fazem quase tudo concorrente, com pausas muito curtas. Problemas típicos: promoção prematura, objetos grandes, vazamento que enche o Old e causa *Full GC*. Diagnóstico: logs de GC, JFR, heap dump.

**10. O que são virtual threads e quando usar?**
**Threads leves gerenciadas pela JVM (Java 21), montadas em poucas threads de plataforma (*carrier*) e desmontadas quando bloqueiam em I/O.** Permitem o modelo simples "uma thread por request" com centenas de milhares de tarefas concorrentes, sem a memória das threads de SO. **Use** em carga I/O-bound (HTTP, banco, filas). **Não use** para CPU-bound (não ajuda), **não faça pool** delas (crie uma por tarefa, `Executors.newVirtualThreadPerTaskExecutor()`), cuidado com *pinning* (`synchronized` segurando I/O em versões antigas) e com `ThreadLocal` pesado. No Spring Boot 3.2+: `spring.threads.virtual.enabled=true`.

**11. `interface` com default method vs `abstract class`?**
**Interface define contrato e permite herdar vários; classe abstrata compartilha estado e implementação.** Interface: sem estado de instância, uma classe implementa várias, `default` methods servem para evoluir uma API sem quebrar implementadores. Classe abstrata: tem campos, construtor, membros `protected`, herança única, ideal para *Template Method* e código base comum. Regra: **comece pela interface**; use abstrata quando precisar de estado ou de esqueleto de algoritmo.

**12. O que é `record`?**
**Uma classe de dados imutável e concisa (Java 16).** `record Ponto(int x, int y) {}` gera construtor canônico, acessores (`x()`, `y()`), `equals`, `hashCode` e `toString`. É `final`, os campos são `final`, pode implementar interfaces, ter métodos e **construtor compacto para validação**. Ideal para DTOs, eventos, chaves compostas e retornos. **Não serve como entidade JPA** (exige construtor sem argumentos e campos mutáveis/proxy) e não substitui classes com comportamento rico.

### Spring

**13. Por que injeção por construtor?**
**Porque deixa a dependência obrigatória, imutável e testável.** Campos `final`, o objeto nunca existe em estado incompleto, o `new` funciona em teste unitário sem Spring nem reflexão, o erro de dependência ausente aparece no startup e dependências circulares ficam evidentes. Construtor com muitos parâmetros é cheiro de classe com responsabilidades demais. Em classe com um único construtor o `@Autowired` é dispensável.

**14. Como o Spring resolve duas implementações do mesmo tipo?**
**Por `@Primary` (padrão), `@Qualifier("nome")` (explícito no ponto de injeção) ou injetando todas.** Se nada for informado, o Spring tenta casar o nome do parâmetro com o nome do bean; se continuar ambíguo, lança `NoUniqueBeanDefinitionException`. Injetar `List<PagamentoGateway>` ou `Map<String, PagamentoGateway>` é ótimo para *Strategy*.

**15. O que é um bean singleton e o risco com estado?**
**Uma única instância por container, compartilhada por todas as threads.** Se tiver campo mutável (lista, contador, `SimpleDateFormat`), haverá *race condition*. Mantenha beans **stateless**: estado em variáveis locais, em objetos de request ou em estruturas thread-safe; ou use escopo `prototype`/`request` quando fizer sentido. Esta é a diferença mental em relação ao PHP-FPM, em que cada request recomeça do zero.

**16. Por que `@Transactional` não funciona em chamada interna?**
**Porque a transação é aplicada por um proxy, e `this.metodo()` ignora o proxy.** O Spring envolve o bean em um proxy (JDK ou CGLIB) que abre e fecha a transação; a chamada vinda da própria classe vai direto ao objeto real. Vale para `@Async`, `@Cacheable` e `@Retryable`. Soluções: **extrair o método para outro bean** (melhor), injetar o próprio bean (gambiarra) ou usar AspectJ. Outros motivos de não funcionar: método não público, classe/método `final`, exceção checked sem `rollbackFor`, exceção engolida em `catch`.

**17. Como o Boot faz auto-configuração?**
**`@EnableAutoConfiguration` carrega classes de configuração listadas em arquivos de metadados dos starters e as ativa por condições.** Cada uma usa `@ConditionalOnClass`, `@ConditionalOnMissingBean`, `@ConditionalOnProperty` etc. Se existe `DataSource` no classpath e nenhum bean seu, ele configura um; se você declara o seu, o seu vence (`OnMissingBean`). Para ver o que foi ativado: executar com `--debug` (*conditions report*) ou o endpoint `/actuator/conditions`.

**18. `@Component` vs `@Service` vs `@Repository`?**
**Todas são estereótipos de `@Component`; mudam a semântica e, no caso de `@Repository`, o comportamento.** `@Service` marca a camada de negócio (só semântica). `@Repository` marca persistência e ativa a **tradução de exceções** (exceções do provedor viram `DataAccessException`). `@Controller`/`@RestController` marcam a camada web. `@Component` é genérico.

**19. Como tratar exceções globalmente?**
**Com `@RestControllerAdvice` + `@ExceptionHandler`.** Mapeie exceções de domínio para status HTTP (404, 409, 422), devolva um corpo padronizado (`ProblemDetail`, RFC 9457), logue uma vez com a causa, e **nunca vaze stacktrace** para o cliente. Trate também `MethodArgumentNotValidException` (validação) e um *fallback* genérico para 500.

### Persistência

**20. O que é N+1 e como resolver?**
**É fazer 1 query para listar N entidades e mais N queries para carregar uma associação lazy de cada uma.** Exemplo: listar 100 pedidos e acessar `pedido.getItens()` em cada um gera 101 queries. Soluções: `JOIN FETCH`, `@EntityGraph`, `@BatchSize`/`default_batch_fetch_size` (carrega em lotes com `IN`), ou **projeção/DTO** direto na query. Cuidado: `JOIN FETCH` de coleção com paginação paginia em memória. Detectar: log de SQL, estatísticas do Hibernate, testes que contam queries.

**21. `LAZY` vs `EAGER` e `LazyInitializationException`?**
**`LAZY` carrega a associação só no primeiro acesso (via proxy); `EAGER` carrega junto sempre.** Padrões JPA: `@ManyToOne`/`@OneToOne` são EAGER; `@OneToMany`/`@ManyToMany` são LAZY. Boa prática: deixar tudo LAZY e buscar o necessário por query. A `LazyInitializationException` ocorre ao acessar uma associação lazy **depois que a sessão fechou**. A solução correta é buscar o que precisa (fetch join, entity graph ou DTO) dentro da transação; ligar *Open Session in View* só para "fazer funcionar" mantém conexão presa durante a renderização.

**22. `merge` vs `persist`?**
**`persist` torna managed uma entidade nova (a mesma instância); `merge` copia o estado de uma entidade detached para uma instância managed e retorna essa outra instância.** Depois do `merge`, o objeto que você passou continua detached. No Spring Data, `save()` chama `persist` se a entidade é nova (id nulo ou sem `@Version`) e `merge` caso contrário.

**23. Optimistic vs pessimistic lock?**
**Otimista não bloqueia e detecta conflito no commit; pessimista bloqueia a linha.** Otimista: coluna `@Version`; o `UPDATE ... WHERE id=? AND version=?` falha se alguém alterou, lança `OptimisticLockException` e você refaz (retry). Ótimo quando conflito é raro. Pessimista: `SELECT ... FOR UPDATE` (`@Lock(PESSIMISTIC_WRITE)`); ótimo quando conflito é frequente (saldo, estoque), mas reduz concorrência e pode dar deadlock, então use timeout e ordem consistente de acesso.

**24. Cache de 1º vs 2º nível?**
**1º nível é por sessão/transação e sempre ligado; 2º nível é compartilhado entre sessões e opcional.** O 1º garante identidade (mesmo id = mesmo objeto na sessão) e evita query repetida por `find`. O 2º (Ehcache, Infinispan, Redis via provedor) vive no `SessionFactory`, exige configurar estratégia de concorrência e invalidação, e vale para dados de leitura frequente que quase não mudam (tabelas de domínio). *Query cache* raramente compensa.

**25. Por que `IDENTITY` atrapalha batch?**
**Porque o id só existe depois do `INSERT`, então o Hibernate precisa executar cada insert na hora e não consegue agrupá-los.** Com `SEQUENCE`, o Hibernate reserva ids em blocos (`allocationSize`) sem ir ao banco por linha e pode usar `jdbc.batch_size` e `order_inserts`. No PostgreSQL prefira `SEQUENCE`.

**26. Isolamento padrão do PostgreSQL e o que ele evita?**
**Read Committed.** Cada comando enxerga os dados comitados até o início *daquele comando*. Evita *dirty read*. **Não evita** *non-repeatable read*, *phantom read*, *lost update* nem *write skew*. Para isso: `Repeatable Read` (snapshot da transação, pode dar erro de serialização e exige retry), `Serializable` (SSI), `SELECT ... FOR UPDATE` ou `@Version`. No PostgreSQL, `Read Uncommitted` se comporta como `Read Committed`.

**27. Como achar uma query lenta?**
**Medir primeiro, depois explicar.** No banco: `pg_stat_statements` (queries mais caras), `log_min_duration_statement`, `EXPLAIN (ANALYZE, BUFFERS)` (seq scan indevido, estimativa de linhas muito diferente da real, join caro), conferir índices e rodar `ANALYZE`. Na aplicação: logs/estatísticas do Hibernate, APM e tracing para ver N+1. Correções: índice adequado (composto na ordem certa), reescrever a query, projeção, paginação por keyset, cache.

### Mensageria e Redis

**28. At-least-once vs exactly-once?**
**At-least-once nunca perde, mas pode duplicar; exactly-once exige condições especiais.** Na prática, entre sistemas distintos, o padrão é **at-least-once + consumidor idempotente**, o que dá efeito "exatamente uma vez" no negócio. O Kafka oferece EOS (producer idempotente + transações + `read_committed`) para o fluxo consumir-transformar-produzir **dentro do Kafka**; efeitos colaterais externos (banco, e-mail, pagamento) ainda precisam de idempotência.

**29. Como garantir idempotência no consumidor?**
**Registrando o que já foi processado ou tornando a operação naturalmente idempotente.** Técnicas: tabela de mensagens processadas com `UNIQUE (message_id)` gravada **na mesma transação** do efeito; `UPSERT` por chave natural; `UPDATE` condicional por versão/estado; operações do tipo "definir valor" em vez de "somar". Em APIs, o equivalente é o header `Idempotency-Key`. Só confirme (ack/commit de offset) após processar.

**30. Por que a ordem do Kafka é por partição?**
**Porque cada partição é um log sequencial com um único líder; entre partições não há ordem global**, e é justamente isso que permite escalar. Para garantir ordem de uma entidade, use a **mesma chave** (ex.: `pedidoId`), que sempre cai na mesma partição, e um único consumidor por partição. Cuidado com retries que reordenam e com aumentar partições depois (muda o mapeamento chave→partição).

**31. O que é *consumer lag*?**
**É a diferença entre o último offset produzido na partição e o offset já confirmado pelo grupo de consumidores.** Lag crescendo significa que o consumo está mais lento que a produção. Ações: monitorar (Prometheus/Burrow), otimizar o processamento, escalar consumidores (até o número de partições), ajustar `max.poll.records`, aumentar partições. Lag constante e baixo é saudável.

**32. DLQ/DLT: quando e como?**
**Quando uma mensagem falha repetidamente ou por erro não recuperável, para não travar a fila (mensagem "veneno").** Fluxo: tentar N vezes com *backoff* exponencial; esgotadas as tentativas, mover para a Dead Letter Queue/Topic **com metadados** (erro, tentativas, origem), alertar, e ter um meio de **reprocessar** depois de corrigir. Erros de validação/formato vão direto para a DLQ; erros transitórios (timeout) fazem retry. No RabbitMQ: DLX + `x-death`/delivery limit; no Kafka: `DeadLetterPublishingRecoverer` no Spring Kafka. Nunca faça `requeue` infinito.

**33. Para que serve o *Outbox*?**
**Resolve o *dual write*: gravar no banco e publicar no broker não é atômico.** Se salvar o pedido e a publicação falhar (ou o contrário), o sistema fica inconsistente. Solução: na **mesma transação** do banco, gravar o evento numa tabela `outbox`; um *relay* (poller ou CDC com Debezium) lê a tabela e publica no broker, marcando como enviado. A entrega é at-least-once, então os consumidores precisam ser idempotentes.

**34. Redis é single-thread: implicações?**
**Cada comando é atômico, sem necessidade de locks, mas um comando lento bloqueia todos os outros.** Evite `KEYS *` (use `SCAN`), coleções gigantes lidas de uma vez, scripts Lua longos e *big keys*. Versões recentes usam threads auxiliares para I/O, mas a execução de comandos continua serial. Para escalar além de um núcleo: **Redis Cluster** (sharding) ou várias instâncias. Pipelining reduz round-trips.

**35. Como evitar *cache stampede*?**
**Impedindo que muitas requisições recalculem a mesma chave expirada ao mesmo tempo.** Técnicas: TTL com *jitter* aleatório, *lock*/single-flight (só uma requisição recalcula, as demais esperam ou recebem valor antigo), renovação antecipada antes de expirar, *serve stale while revalidate*, e pré-aquecimento de chaves quentes. Relacionados: *penetration* (chaves inexistentes: cachear o "não existe" ou Bloom filter) e *avalanche* (muitas chaves expirando juntas: jitter).

**36. Riscos do lock distribuído com Redis?**
**O lock pode expirar enquanto o trabalho ainda roda, ou ser perdido num failover, e aí dois processos acham que têm o lock.** Causas: TTL curto, pausa longa (GC), replicação assíncrona do Redis. Mitigações: `SET key token NX PX ttl` com **token único**, liberar por **script Lua** (só apaga se o token for o seu), renovação automática (watchdog do Redisson), **fencing token** no recurso protegido e operações idempotentes. Se a correção for crítica, use o próprio banco (`SELECT FOR UPDATE`, constraint única) em vez de depender do Redis.

### Arquitetura

**37. Monólito modular vs microsserviços?**
**Comece por um monólito modular e extraia serviços quando houver motivo claro.** Monólito modular: um deploy, módulos com fronteiras bem definidas (Bounded Contexts), transações simples, operação barata. Microsserviços: escala e deploy independentes por time, isolamento de falhas, tecnologias diferentes, ao custo de rede, consistência eventual, observabilidade, versionamento de contratos e operação. Microsserviços sem fronteiras de domínio claras viram um "monólito distribuído".

**38. Saga vs 2PC?**
**2PC é transação distribuída atômica e bloqueante; Saga é uma sequência de transações locais com compensações e consistência eventual.** 2PC depende de um coordenador, segura locks e reduz disponibilidade; não funciona entre bancos e brokers heterogêneos. Saga: cada passo comita localmente e publica um evento; se um passo falha, executam-se **ações compensatórias** (estornar pagamento, liberar estoque). Pode ser por **coreografia** (eventos) ou **orquestração** (um coordenador). Cuidados: compensações idempotentes e a falta de isolamento entre passos.

**39. Circuit breaker: estados e uso?**
**Protege o sistema de ficar chamando um serviço que está falhando.** **Fechado**: chamadas normais, contando falhas. Ao passar do limite (taxa de falha numa janela), vai para **aberto**: falha rápido, sem chamar o serviço, por um tempo. Depois vai para **meio-aberto**: deixa passar poucas chamadas de teste; se der certo, fecha; se não, reabre. Combine com *timeout*, *retry* com backoff e jitter, *bulkhead* e *fallback*. Biblioteca: Resilience4j (`failureRateThreshold`, `slidingWindowSize`, `waitDurationInOpenState`).

**40. Como tornar uma API idempotente?**
**Pedindo uma chave de idempotência e devolvendo sempre o mesmo resultado para a mesma chave.** O cliente envia `Idempotency-Key: <uuid>`; o servidor grava (chave, hash do corpo, resposta, status) numa tabela com `UNIQUE (chave)` **na mesma transação** da operação. Se a chave repete, devolve a resposta guardada; se repete com corpo diferente, responde 422/409; se chega em paralelo, a constraint única ou um lock serializa. Defina TTL das chaves. Além disso: `PUT`/`DELETE` já são idempotentes por semântica, e regras como "um pagamento por pedido" devem ter constraint única no banco.

---

## 16. Plano de estudo sugerido (7 dias)

| Dia | Foco | Entrega |
|---|---|---|
| 1 | Java core (seções 1.1 a 1.6) | responder as perguntas 1-12 da seção 15 em voz alta |
| 2 | Concorrência + Java moderno (1.7, 1.8) | LRU, produtor/consumidor, streams |
| 3 | Spring Boot + `@Transactional` + REST + Security (seção 3, 7) | CRUD Spring Boot com Postgres e Testcontainers |
| 4 | Hibernate/JPA + PostgreSQL (seções 4, 6) | reproduzir N+1 e corrigir; ler `EXPLAIN ANALYZE` |
| 5 | **Grails + GORM** (seções 4.6, 5) | app Grails com domain, service, controller REST e teste Spock |
| 6 | Kafka, RabbitMQ, Redis (seções 8, 9) | produtor/consumidor idempotente com DLQ nos dois brokers (docker compose) |
| 7 | Design de sistemas, comportamentais, simulado (seções 11, 13, 14) | 3 histórias STAR + 1 design completo em voz alta |

**Ordem de prioridade se faltar tempo:** (1) Spring/`@Transactional`/JPA N+1, (2) HashMap/concorrência/Java moderno, (3) PostgreSQL (índices, isolamento, EXPLAIN), (4) Kafka/RabbitMQ (garantias, idempotência, DLQ, outbox), (5) um Grails mínimo funcionando, (6) Redis (cache e lock), (7) Docker.

---

## 17. Fontes para aprofundar

- *Effective Java* (Joshua Bloch): itens sobre `equals`/`hashCode`, imutabilidade, generics, exceções.
- *Java Concurrency in Practice* (Brian Goetz): concorrência e JMM.
- *Designing Data-Intensive Applications* (Martin Kleppmann): réplicas, transações, streams, consistência.
- *Domain-Driven Design Distilled* (Vaughn Vernon): DDD de forma objetiva.
- *High Performance Java Persistence* (Vlad Mihalcea): Hibernate, N+1, locking, batch.
- Documentação oficial: Spring Boot, Hibernate ORM, Grails/GORM, PostgreSQL, Apache Kafka, RabbitMQ, Redis.

**Mensagem final:** você não precisa saber tudo de Grails; precisa demonstrar **fundamento sólido, curiosidade e honestidade**. Domine Spring/JPA/Postgres/mensageria com profundidade, mostre que já rodou um Grails, e conduza a conversa para as histórias reais de pagamentos, filas e arquitetura em que você é referência.
