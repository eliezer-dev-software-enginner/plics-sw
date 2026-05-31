# Decisões Arquiteturais

## 2026-05-31: Remoção de interfaces ContratoTelaCrud depreciadas

**Problema:** ContratoTelaCrud e ContratoTelaCrudV2 estavam marcadas como @Deprecated e sem nenhuma referência no código. Apenas ContratoTelaCrudV3 é utilizada atualmente.

**Decisão:** Remover as duas interfaces obsoletas para simplificar o código.

---

## 2026-05-31: FeedbackViewModel compartilhado

**Problema:** RelatarErroScreen e SugerirMelhoriaScreen tinham código quase idêntico (State, ComputedState, lógica de envio via Telegram), violando o princípio DRY.

**Decisão:** Criar `FeedbackViewModel` com a lógica compartilhada de formulário de feedback (estados `isSending`, `content`, `btnText` e método `send()`). Ambos os Screens agora instanciam o mesmo ViewModel, diferindo apenas no label do textarea.

**Benefícios:**
- Elimina duplicação de código entre duas screens
- Cada screen agora possui sua ViewModel correspondente (conforme regra do projeto)

---

## 2026-05-31: Eventos movidos para core/events com EntityEvent<T> genérico

**Problema:** ClienteEvents, TecnicoEvents e ProdutoEvents repetiam exatamente a mesma estrutura (Criado, Editado, Excluido records), além de ProdutoEvents conter um bug (importava TecnicoModel).

**Decisão:** Criar `EntityEvent<T>` genérico em `core/events` com factory methods estáticos (`criado`, `editado`, `excluido`) e método `is(EventType)` para pattern matching. Isso elimina a necessidade de uma classe de eventos por entidade.

**Benefícios:**
- Elimina duplicação de código (3 classes ~12 linhas cada → 1 classe ~30 linhas)
- Evita bugs de copy-paste (como o TecnicoModel em ProdutoEvents)
- Fácil de estender para novas entidades sem criar novas classes de evento
- Package `events` movido para dentro de `core` (organização consistente)