# Boas Práticas de Conversa com o Copilot — FlowSabouters

> Guia para manter sessões produtivas, evitar quedas de conexão e aproveitar melhor o contexto no desenvolvimento do FlowSabouters (Flutter).

---

## Por que sessões longas causam problemas?

- O modelo tem um **limite de contexto** (janela de tokens): sessões muito longas degradam a qualidade das respostas.
- Respostas em **streaming** são sensíveis a quedas de rede (erros 502, timeout).
- Quanto mais longa a sessão, maior o risco de **perda de contexto** e respostas genéricas.
- No FlowSabouters o risco é maior porque as regras de jogo se entrelaçam (relógio, Gorse Bele, treinamento, economia). Quanto mais áreas numa conversa, mais fácil o modelo misturar regras.

---

## Regras de Ouro

### 1. Uma tarefa por sessão
Abra uma nova sessão para cada tarefa distinta. Não misture "ajustar a Gorse Bele" com "refatorar a Loja" na mesma conversa.

### 2. Mensagens curtas e diretas

Evitar:
> "Preciso que você olhe o motor do jogo, entenda como o tempo funciona, veja os testes, me diga o que acha e implemente o botão de adiantar dias..."

Preferir:
> "Em `session_fast_forward_handler.dart`, implemente `fastForward(int days)`: chama `skipDays(1)` e `tick()` por dia e para se a fase deixar de ser `WorkPhase`."

### 3. Forneça contexto explícito
Sempre informe:
- **Arquivo** e **classe/função** envolvidos
- **Comportamento atual** vs **comportamento esperado**
- **Mensagem de erro** ou teste que falha (se houver)
- **Seção do plano** que a tarefa implementa (ex.: "seção 4 do plano")

### 4. Confirme antes de continuar
Após cada entrega do Copilot, valide o resultado (rode `flutter test` e abra a tela afetada) antes de pedir a próxima alteração. Evita retrabalho em cadeia.

### 5. Use o SCRATCHPAD entre sessões
Antes de encerrar, peça ao Copilot para atualizar o `SCRATCHPAD.md` com o estado atual. Na próxima sessão, comece com:
> "Leia o SCRATCHPAD e me diga o que está pendente."

### 6. Aponte as convenções, não as repita
O projeto tem regras fixas em `.github/copilot-instructions.md`. Em vez de reexplicá-las, diga: "Siga as convenções do projeto". Se o Copilot desviar, cite a regra violada.

---

## Convenções do projeto que o Copilot deve respeitar

Lembre-as quando a tarefa tocar nelas:

- **Telas por pasta** (`screens/<tela>/`), com widgets em `widgets/`.
- **Sem lógica em widget de tela.** Regra de jogo fica em `domain/`.
- **I/O atrás de interface** (stores, relógio, aleatoriedade), para permitir fakes nos testes.
- **Cores só em `AppColors`**; nada de cor solta no widget.
- **Strings em `app_pt.arb`, `app_en.arb` e `app_es.arb`**, sempre as três.
- **Domínio puro e testável:** `GameEngine`, `DailySimulator`, `GorseBeleTrigger` e afins não dependem de Flutter.
- **Aleatoriedade injetada** (`Random` semeado) e **relógio injetado** (`FakeClock`), para testes determinísticos.

---

## Estrutura recomendada de sessão

```
1. [CONTEXTO]  "Estou trabalhando em <funcionalidade>. Arquivo: <caminho>."
2. [PROBLEMA]  "O comportamento atual é X. O esperado é Y."
3. [AÇÃO]      "Implemente / Corrija / Explique."
4. [VALIDAÇÃO] Rode os testes e confira a tela antes de continuar.
5. [ENCERRAR]  "Atualize o SCRATCHPAD com o que foi feito."
```

---

## Como dividir uma feature grande em sessões

Use o plano como mapa. Exemplo com a ordem de entrega do plano:

| Sessão | Tarefa única | Entrega |
|--------|--------------|---------|
| 1 | Gorse Bele: constantes e `GorseBeleParams` | Parâmetros e testes de `GorseBeleTrigger` |
| 2 | Gorse Bele: avaliação por dia e aviso prévio (`omenDay`) | Gatilho por dia de jogo e teste de sorteios |
| 3 | Gorse Bele: opção Calma/Normal/Caos nas Configurações | Tela, l10n `pt/en/es` |
| 4 | Adiantar dias: `skipDays` no relógio | Método e teste de fração de dia |
| 5 | Adiantar dias: `session_fast_forward_handler` | Handler, condições de parada e teste de equivalência |
| 6 | Adiantar dias: `FastForwardButton` | Widget e teste de widget |

Cada linha cabe em uma sessão. Se uma tarefa exigir mais de 5 arquivos, divida de novo.

---

## Sinais de que a sessão está longa demais

- Copilot começa a repetir informações já dadas.
- Respostas ficam genéricas ou perdem precisão técnica.
- Erros 502 / quedas de conexão frequentes.
- O modelo "esquece" decisões tomadas no início da conversa (ex.: volta a sugerir comprar treinamento na Loja, já decidido como só no `TeamSheet`).
- Ele reintroduz lógica no widget ou cores fora do `AppColors`.

**Ação:** abra uma nova sessão e comece com um resumo do estado atual.

---

## Quando a conexão cair (502 / timeout)

1. Aguarde 2-3 minutos antes de tentar novamente.
2. Verifique sua conexão de internet.
3. Reabra uma nova sessão. Não tente continuar a sessão quebrada.
4. Reenvie apenas a última mensagem, não toda a conversa.
5. Confira no `git diff` o que já foi aplicado antes da queda, para não duplicar alterações.

---

## Template de abertura de sessão

```
Contexto: [nome da feature ou bug, e seção do plano]
Arquivo principal: lib/domain/<area>/<arquivo>.dart
Estado atual: [o que já foi feito / o que está quebrado]
Objetivo desta sessão: [uma coisa específica]
Fora do escopo: [o que não deve ser tocado]
```

Exemplo preenchido:

```
Contexto: Gorse Bele menos agressiva (seção 4 do plano)
Arquivo principal: lib/domain/gorse_bele/gorse_bele_trigger.dart
Estado atual: evaluate roda a cada tick (1/s) com 35-75% de chance
Objetivo desta sessão: avaliar uma vez por dia de jogo, guardando lastEvaluatedDay
Fora do escopo: overlay, Configurações, aviso prévio
```

---

## Exemplos de bons pedidos

**Regra de domínio:**
> "Em `training_catalog.dart`, crie a tabela `const` de distâncias entre `CardDomain` (pares não ordenados). Adicione um teste que falha se faltar algum par."

**Correção de bug:**
> "Em `daily_simulator.dart`, `_simulateDay` roda duas vezes quando `skipDays` é chamado com o relógio a 60 s do dia. Esperado: uma vez por dia (`lastSimulatedDay`). Escreva o teste que reproduz e corrija."

**Tela:**
> "Crie `screens/shop/exchange_tab.dart` com o câmbio (`CashToGemsExchanger`), sem lógica no widget. Use `AppColors` e adicione as strings nos três `.arb`."

**Teste:**
> "Escreva o teste de equivalência: mesma semente, N dias via `FakeClock` vs `fastForward(N)`, comparando dia, caixa, cards e eventos."

---

## Pedidos para evitar

- "Melhore o jogo" ou "arrume a Gorse Bele": sem arquivo, comportamento esperado ou critério.
- Colar o plano inteiro: cite a seção e o arquivo.
- Pedir implementação e redesenho de regra na mesma mensagem. Decida a regra primeiro, depois peça o código.
- Pedir mudanças de números de balanceamento sem simulação. Peça o script de simulação antes de fixar valores.

---

## Limites práticos sugeridos

| Item | Recomendado |
|------|-------------|
| Duração de uma sessão | Até ~30-40 trocas de mensagens |
| Tamanho de uma mensagem | Até ~500 palavras |
| Arquivos por sessão | Até ~5 arquivos distintos |
| Tarefas por sessão | **1 tarefa principal** |

---

## Checklist de fim de sessão

- [ ] Os testes da área tocada passam (`flutter test`).
- [ ] Strings novas estão nos três `.arb` (`pt`, `en`, `es`).
- [ ] Nenhuma cor fora do `AppColors`; nenhuma regra de jogo em widget.
- [ ] `SCRATCHPAD.md` atualizado com feito, pendente e decisões tomadas.
- [ ] Commit pequeno, com mensagem que cita a seção do plano.

---

*Criado em: outubro/2026 — FlowSabouters*