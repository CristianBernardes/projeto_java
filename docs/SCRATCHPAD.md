# SCRATCHPAD — Projeto Java para estudo

> Estado vivo entre sessões. Comece cada sessão com: *"Leia o SCRATCHPAD e me diga o que está pendente."* Termine com: *"Atualize o SCRATCHPAD com o que foi feito."*
> Regras de conversa: [boas_praticas_de_conversa_com_o_copilot.md](./boas_praticas_de_conversa_com_o_copilot.md)

## Objetivo atual

Criar um projeto Java de estudo baseado na vaga que estarei participando [vaga.md](./vaga.md) em Docker com docker-compose.yml e um Makefile. O projeto deve ter como banco de dados o postgresql e sua persistência deve ser feita numa pasta .docker que nunca será commitada.

## ✅ Feito

### Sessão 2 — Correção DIP + README + Swagger
- `ProdutoServicePort.java` — interface criada; controller e testes dependem dela, nunca da classe concreta
- `ProdutoService` — implementa `ProdutoServicePort`, `@Override` em todos os métodos públicos
- `ProdutoController` — injeção via `ProdutoServicePort` (não mais `ProdutoService`)
- `ProdutoControllerTest` — `@MockBean ProdutoServicePort` (não mais a classe concreta)
- `OpenApiConfig.java` — metadados globais Swagger
- `application.yml` — config springdoc
- DTOs anotados com `@Schema`; controller com `@Tag`, `@Operation`, `@ApiResponse`, `@Parameter`
- `README.md` — documentação completa
- 6/6 testes passando

- `pom.xml` — Spring Boot 3.3.5, Java 21, JPA/Hibernate, Flyway, Validation, Actuator, Lombok, Spotless (Google Java Format), **SpringDoc OpenAPI 2.6.0**
- `Dockerfile` — multi-stage build (Maven builder + JRE alpine), usuário não-root, flags JVM para container
- `Dockerfile.dev` — imagem Maven para testes/format/lint
- `docker-compose.yml` — serviços `postgres` (porta 5433) e `app` (porta 8088), healthcheck, rede `projeto-java`, serviço `maven` (profile dev)
- `Makefile` — targets: `up`, `up-db`, `down`, `build`, `restart`, `logs`, `logs-app`, `logs-db`, `ps`, `shell-app`, `shell-db`, `test`, `format`, `lint`, `clean`
- `application.yml` + `application-docker.yml` — perfil `docker` para o container, config springdoc
- `V1__create_produtos.sql` — migration Flyway: tabela `produtos`
- Domínio `Produto` — entidade rica com regras de negócio encapsuladas
- `ProdutoRepository` — Spring Data JPA
- DTOs: `CriarProdutoRequest`, `AtualizarProdutoRequest`, `ProdutoResponse` (records com `@Schema`)
- `ProdutoService` — casos de uso com `@Transactional`
- `ProdutoController` — REST CRUD com `@Tag`, `@Operation`, `@ApiResponse`, `@Parameter`
- `OpenApiConfig` — metadados globais da API (título, descrição, servidores)
- `RecursoNaoEncontradoException` + `GlobalExceptionHandler` — respostas de erro padronizadas
- Testes: `ProdutoServiceTest` (Mockito) e `ProdutoControllerTest` (`@WebMvcTest`) — 6/6 passando
- `README.md` — documentação completa: stack, pré-requisitos, comandos, endpoints, estrutura

## Pendente

- Executar `make up` e validar o build completo + Swagger UI no browser
- Adicionar testes de integração com Testcontainers
- Explorar mensageria (RabbitMQ/Kafka) — desejável da vaga



## Exemplo:

```yaml
  postgres:
    container_name: sorte-maker-postgres
    image: postgres:16-alpine
    restart: unless-stopped
    environment:
      POSTGRES_USER: sortemaker
      POSTGRES_PASSWORD: secret123
      POSTGRES_DB: sortemaker
    volumes:
      - ./.docker/postgres/dbdata:/var/lib/postgresql/data
      - ./confs:/docker-entrypoint-initdb.d
    ports:
      - "5432:5432"
    networks:
      - projeto-java
```

## ⚠️ OBSERVAÇÃO OBRIGATÓRIA: PADRÕES DE QUALIDADE E BOAS PRÁTICAS

**Toda implementação deve seguir rigorosamente os princípios de engenharia de software, priorizando qualidade, legibilidade, manutenibilidade, segurança, escalabilidade e consistência arquitetural.**

- **SOLID:** aplique rigorosamente os cinco princípios de SOLID, respeitando responsabilidades bem definidas, baixo acoplamento e alta coesão.
- **Design Patterns:** utilize padrões de projeto quando forem adequados ao problema, evitando complexidade desnecessária, abstrações prematuras e overengineering.
- **Clean Code:** escreva código limpo, legível, expressivo e autodocumentado, com nomes significativos, funções pequenas, responsabilidades únicas e fluxo de execução claro.
- **JavaDoc:** documente obrigatoriamente classes, interfaces e métodos públicos utilizando JavaDoc (`/** ... */`), descrevendo claramente o propósito, parâmetros, retornos e exceções.
- **Formatação obrigatória:** Criar um arquivo Makefile na raiz para padronizar a formatação Java.
- **DRY (Don't Repeat Yourself):** evite duplicação de lógica, regras de negócio e código. Extraia componentes reutilizáveis quando houver uma justificativa real.
- **KISS (Keep It Simple, Stupid):** prefira soluções simples, explícitas e fáceis de manter, sem sacrificar a qualidade arquitetural.
- **YAGNI (You Aren't Gonna Need It):** implemente apenas o que é necessário para os requisitos atuais, sem criar funcionalidades ou abstrações especulativas.
- **Separação de responsabilidades:** mantenha as camadas de apresentação, domínio, aplicação e infraestrutura devidamente isoladas, respeitando a arquitetura existente.
- **Princípios de domínio:** preserve as regras de negócio em seus respectivos contextos, evitando espalhá-las entre interfaces, controllers, componentes e serviços genéricos.
- **Consistência arquitetural:** antes de implementar qualquer funcionalidade, analise a estrutura do projeto, identifique os padrões existentes e siga as convenções estabelecidas. Não introduza novas abordagens sem justificativa técnica.
- **Tratamento de erros:** implemente tratamento explícito e apropriado de exceções, estados inválidos, falhas de integração e condições inesperadas. Nunca silencie erros ou utilize valores artificiais para mascarar falhas.
- **Segurança:** valide entradas, respeite as permissões e os limites de confiança, proteja dados sensíveis e aplique o princípio do menor privilégio. Nunca exponha credenciais, tokens ou informações confidenciais.
- **Testes automatizados:** crie ou atualize testes unitários, de integração e de ponta a ponta conforme o escopo e o risco da alteração. Teste também cenários negativos, limites e casos excepcionais.
- **Regressão:** preserve o comportamento existente fora do escopo da mudança. Não remova funcionalidades, regras de negócio ou validações sem autorização explícita.
- **Performance:** evite consultas redundantes, processamento desnecessário, problemas de N+1, renderizações excessivas e alocações evitáveis. Otimize com base em evidências, sem comprometer a legibilidade.
- **Tipagem e contratos:** utilize tipagem forte sempre que disponível, respeite interfaces e contratos existentes e evite tipos genéricos, coerções implícitas e o uso indiscriminado de `any`.
- **Legibilidade e manutenção:** evite funções gigantes, classes com múltiplas responsabilidades, condicionais excessivamente complexas, números mágicos, comentários redundantes e dependências desnecessárias.
- **Dependências:** reutilize as bibliotecas já adotadas pelo projeto sempre que possível. Não adicione dependências externas sem necessidade comprovada e avaliação de seus impactos.
- **Refatoração responsável:** refatore somente o necessário para viabilizar a solução ou melhorar objetivamente a qualidade do código relacionado. Evite alterações abrangentes e não relacionadas ao requisito.
- **Compatibilidade:** respeite as versões das linguagens, frameworks, bibliotecas, APIs e contratos existentes, considerando os impactos sobre os demais módulos e consumidores.
- **Logs e observabilidade:** registre informações úteis para diagnóstico, sem expor dados sensíveis ou gerar ruído excessivo.
- **Documentação:** atualize a documentação técnica quando houver mudanças relevantes em contratos, arquitetura, configurações, fluxos de negócio ou procedimentos de execução.
- **Decisões do dono do produto são incontestáveis:** o que o Cristian decidir deve ser executado exatamente como pedido, sem contestar, reinterpretar, limitar ou "melhorar" por conta própria. Dúvidas de entendimento podem ser perguntadas antes; depois da decisão, só se executa.

### Fluxo obrigatório de trabalho

1. **Analisar:** compreenda os requisitos, investigue o código existente e identifique os componentes e as regras de negócio envolvidos antes de alterar qualquer arquivo.
2. **Planejar:** defina a solução mais simples e consistente com a arquitetura atual, considerando impactos, dependências, riscos e cenários de erro.
3. **Implementar:** desenvolva uma solução coesa, reutilizável quando apropriado, sem duplicações desnecessárias ou desvios dos padrões do projeto.
4. **Validar:** execute `make format`(que será criado) e os testes, linters, verificações de tipos e demais validações disponíveis e pertinentes.
5. **Revisar:** verifique possíveis regressões, problemas de segurança, inconsistências arquiteturais, código morto, alterações não relacionadas e falhas nos cenários excepcionais.
6. **Concluir:** informe objetivamente o que foi implementado, quais validações foram executadas e eventuais limitações ou pendências.

### Regra inegociável

**Nunca considere uma implementação concluída apenas porque funciona no cenário principal.** A solução deve atender aos requisitos, respeitar a arquitetura existente, preservar os comportamentos esperados, possuir testes adequados e passar pelas verificações de qualidade aplicáveis.

Não ignore falhas de testes, não desabilite verificações para fazer o pipeline passar e não declare que uma validação foi executada com sucesso sem ter confirmado seu resultado.

**Commits sem coautoria do Copilot:** nenhum commit pode ter o Copilot (ou qualquer assistente de IA) como coautor. Não inclua trailers `Co-authored-by:` referentes ao Copilot na mensagem de commit. Se a ferramenta adicionar esse trailer automaticamente, remova-o antes de commitar (ou use `git commit --amend` / `git rebase` para retirá-lo de commits já criados e ainda não enviados).

**Qualidade de código não é opcional. Toda alteração deve ser implementada com responsabilidade técnica, simplicidade, consistência e foco na manutenção de longo prazo.**

## COMMITS
**Todos os commits que fizer, este arquivo deve ser atualizado e enviado junto**
---
