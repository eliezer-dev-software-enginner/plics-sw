# Decisões Arquiteturais

## 2026-06-08: PDV — cliente padrão para vendas à vista e dataCriacao em itens

**Problema 1:** Ao clicar "Finalizar Venda" sem selecionar cliente (venda à vista), `clienteId` era `null` causando NPE em `Long.valueOf(clienteId)`.

**Problema 2:** Ao corrigir o problema 1, o fluxo avançava e revelava que `dataCriacao` não era setado nos `PedidoItemModel`, causando `NOT NULL constraint failed: pedido_itens.dataCriacao`.

**Decisão:**
1. Quando `clienteId` é `null` e a venda não é fiada, usar `1` (ID do "CLIENTE PADRÃO" inserido pela migration V16).
2. Adicionar `itemModel.setDataCriacao(LocalDateTime.now())` no loop de itens em `PDVService.finalizarVenda()`.
3. Alinhar `PedidoModel.clienteId` de `Long` para `Integer` (consistente com `VendaModel`, `OrdemServicoModel`, `ContaAreceberModel`). SQLite retorna `INTEGER` como `Integer` no Java, e Persism não faz conversão automática `Integer → Long`.
4. Adicionar `model.setDataCriacao(LocalDateTime.now())` em `ContaAreceberService.gerarContasDeVenda()` — mesma causa do problema 2, `dataCriacao` não era setado nas contas a receber.
5. Adicionar construtor `PDVService(Session)` para permitir testes com session em memória.
6. Criar `PDVServiceTest` com 4 testes: cliente padrão, cliente nulo, fiado gera contas, não fiado não gera contas.

**Arquivos alterados:**
- `src/main/java/my_app/db/models/PedidoModel.java:20`
- `src/main/java/my_app/db/dto/PedidoDto.java:6`
- `src/main/java/my_app/services/PDVService.java:26,70`
- `src/main/java/my_app/screens/pdvScreen/PDVScreenViewModel.java:211`
- `src/main/java/my_app/db/services/ContaAreceberService.java:140`
- `src/test/java/my_app/services/PDVServiceTest.java` (novo)

---

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

---

## 2026-05-31: Updater incluso no pacote de distribuição

**Problema:** O plics-sw-updater era um projeto separado sem integração com os scripts de empacotamento, impossibilitando que o instalador já incluísse o utilitário de atualização.

**Decisão:** Adicionar `build_updater()` em `scripts/config.py` que compila o `plics-sw-updater` e copia o JAR resultante para `temp_dir/updater.jar`. Ambos os scripts (`create-msi.py` e `create-deb.py`) chamam essa função antes do jlink/jpackage. O smoke test (execução da aplicação gerada) foi adicionado ao `create-msi.py` seguindo o mesmo padrão já existente no `create-deb.py`.

---

## 2026-05-31: Correções no empacotamento MSI

**Problema:** O script `create-msi.py` falhava com múltiplos erros:
1. `JAVA_HOME` com `\bin` no final quebrava o Gradle
2. `--java-options` com aspas literais inválidas para o `jpackage`
3. Ícone `.png` rejeitado pelo MSI (exige `.ico`)
4. Runtime mínimo (5 módulos fixos) causava "Failed to Launch JVM" por falta de módulos
5. `copy_natives` procurava DLLs em `temp/lib/` mas estavam em `temp/bin/`
6. `jdeps`, `jlink` e `jpackage` não estavam no PATH

**Decisão:**
1. Criada função `_java_home()` que remove `\bin` do `JAVA_HOME` automaticamente
2. `--java-options` sem aspas literais
3. `ICON_PATH` usa `.ico` no Windows
4. `run_jlink` usa `jdeps --print-module-deps` para detectar módulos necessários dinamicamente, mais fallback com módulos essenciais (`java.logging`, `java.xml`, etc.)
5. `copy_natives` busca DLLs em `temp/bin/` primeiro, com fallback para `temp/lib/`
6. Todos os comandos JDK usam caminho absoluto via `_java_home()`

**Benefícios:**
- Geração do MSI funciona sem dependências de PATH ou configuração manual
- Runtime inclui todos os módulos que a aplicação realmente precisa
- Aplicação instalada inicia sem "Failed to Launch JVM"

---

## 2026-06-04: Correção de column name mismatch em ComprasRepository

**Problema:** A query `SELECT * FROM compras WHERE data_criacao BETWEEN ? AND ?` usava snake_case `data_criacao`, mas a coluna foi criada no migration V7 como `dataCriacao` (camelCase). Isso causava `SQLException: no such column: data_criacao` ao carregar a HomeScreen.

**Decisão:** Alterar `data_criacao` para `dataCriacao` no SQL raw de `ComprasRepository.somarComprasPorPeriodo()`.

**Arquivo alterado:** `src/main/java/my_app/db/repositories/ComprasRepository.java:26`

---

## 2026-06-04: Alinhamento de tipos entre model e migration (Compras)

**Problema:** A migration V7 usava `dataCriacao INTEGER`, `data_compra TEXT`, `preco_compra TEXT`, `desconto_em_reais TEXT`. Persism mapeia `INTEGER` do SQLite para `Integer` em Java (não `long`), e `TEXT` para `String` (não `long`/`BigDecimal`). Isso causava `NumberFormatException` e `IllegalArgumentException` em testes e potencialmente em produção.

**Decisão:**
1. Migration V7: `dataCriacao INTEGER` → `REAL`, `data_compra TEXT` → `REAL`, `preco_compra TEXT` → `REAL`, `desconto_em_reais TEXT` → `REAL`
2. `CompraModel.fornecedorId` alterado de `Long` para `Integer` (consistente com `FornecedorModel`, `ProdutoModel`, `ContasPagarModel`)
3. `CompraDto.fornecedorId` alterado de `Long` para `Integer`
4. `ContasPagarService`: simplificado `setFornecedorId` (sem `.intValue()`)

**Arquivos alterados:**
- `src/main/resources/flyway_migrations/V7__criar_compras.sql`
- `src/main/java/my_app/db/models/CompraModel.java`
- `src/main/java/my_app/db/dto/CompraDto.java`
- `src/main/java/my_app/services/ContasPagarService.java`
- `src/main/java/my_app/screens/comprasScreen/ComprasScreenViewModel.java`

---

## 2026-06-04: WelcomeScreen movido para pacote próprio com ViewModel

**Problema:** WelcomeScreen estava solto em `my_app.screens` sem ViewModel, violando a convenção do projeto.

**Decisão:** Criar pacote `my_app.screens.welcomeScreen`, mover `WelcomeScreen` para dentro dele com uma `WelcomeScreenViewModel` (minimalista, sem lógica de negócio).

**Arquivos alterados:**
- `src/main/java/my_app/screens/WelcomeScreen.java` → movido para `welcomeScreen/`
- `src/main/java/my_app/screens/welcomeScreen/WelcomeScreenViewModel.java` (novo)
- `src/main/java/my_app/core/AppRoutes.java` (import atualizado)

---

## 2026-06-04: PedidosScreenViewModel refatorado para usar PedidoItemService

**Problema:** PedidosScreenViewModel usava `PedidoItemRepository` diretamente e geria `Session` manualmente, violando o padrão Service + Repository.

**Decisão:** Substituir `PedidoItemRepository` + `Session` por `PedidoItemService`. Adicionado método `listarPorPedido()` em `PedidoItemService`.

**Arquivos alterados:**
- `src/main/java/my_app/db/services/PedidoItemService.java` (adicionado `listarPorPedido`)
- `src/main/java/my_app/screens/pedidosScreen/PedidosScreenViewModel.java` (refatorado)

---

## 2026-06-08: Licença de teste com validade até dia 11

**Problema:** Usuários de teste usavam a licença `QHd3fuX3mtoCo1gd9dmeKGTEBrxUJ31MxJ` sem prazo de validade, e não havia mecanismo para expirá-la.

**Decisão:**
1. `AuthScreenViewModel`: aceitar tanto a licença de produção (`984e2bb76c7b627641b6b7dc080f8e23`) quanto a de teste (`QHd3fuX3mtoCo1gd9dmeKGTEBrxUJ31MxJ`). A de teste só é válida até o dia 11 do mês (inclusive).
2. Salvar a licença utilizada no campo `licensa` da tabela `preferencias` (Migration V17).
3. `AuthScreenViewModel.load()`: se a licença salva for a de teste e estiver expirada, exibir o campo de licença novamente para o usuário digitar uma válida.
4. `HomeScreenViewModel.isLicensaTesteExpirada()`: método público que consulta a licença salva e a data atual.
5. `HomeScreen.onMount()`: redirecionar para `AuthScreen` se a licença de teste expirou.

**Arquivos alterados:**
- `src/main/resources/flyway_migrations/V17__add_licensa_to_preferencias.sql` (novo)
- `src/main/java/my_app/db/models/PreferenciasModel.java`
- `src/main/java/my_app/screens/authScreen/AuthScreenViewModel.java`
- `src/main/java/my_app/screens/homeScreen/HomeScreen.java`
- `src/main/java/my_app/screens/homeScreen/HomeScreenViewModel.java`
- `src/test/java/my_app/screens/authScreen/AuthScreenViewModelTest.java`
- `src/test/java/my_app/screens/homeScreen/HomeScreenViewModelTest.java`

---

## 2026-06-04: Testes de repository para ComprasRepository

**Decisão:** Criar `ComprasRepositoryTest` com 6 testes (salvar, listar, atualizar, excluirById, buscarById, somarComprasPorPeriodo) seguindo o padrão dos demais testes do projeto.

**Arquivo criado:** `src/test/java/my_app/db/repositories/ComprasRepositoryTest.java`

---

## 2026-06-04: Logging estruturado com SLF4J + appender de erros separado

**Problema:** Apenas 1 arquivo de produção usava SLF4J. Os demais usavam `e.printStackTrace()` ou `System.out/err`, sem persistência de logs em arquivo.

**Decisão:**
1. `logback.xml`: adicionado appender `ERROR-FILE` que grava apenas eventos ERROR em `error.log` com retenção de 30 dias
2. Substituídos todos os `printStackTrace` (11 arquivos) por `log.error("mensagem", e)`
3. Adicionado Logger em `DB.java` para registrar conexões e erros de driver

**Arquivos alterados:**
- `src/main/resources/logback.xml`
- `src/main/java/my_app/db/DB.java`
- `src/main/java/my_app/domain/ContratoTelaCrudV3.java`
- `src/main/java/my_app/hotreload/HotReload.java`
- `src/main/java/my_app/hotreload/Reloader.java`
- `src/main/java/my_app/screens/authScreen/AuthScreenViewModel.java`
- `src/main/java/my_app/screens/comprasScreen/ComprasScreenViewModel.java`
- `src/main/java/my_app/screens/pdvScreen/PDVScreenViewModel.java`
- `src/main/java/my_app/screens/pedidosScreen/PedidosScreenViewModel.java`
- `src/main/java/my_app/screens/vendaScreen/VendaMercadoriaScreenViewModel.java`
- `src/main/java/my_app/utils/TrayManager.java`