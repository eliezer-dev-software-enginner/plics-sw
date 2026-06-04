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