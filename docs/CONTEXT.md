# Contexto do Projeto

## Estrutura
- JavaFX + Megalodonte (UI framework)
- Persism (ORM) + SQLite
- Flyway (migrations)
- Padrão: Screen + ViewModel + Service + Repository
- Events movidos para core/events com EntityEvent<T> genérico
- **Updater embutido no mesmo JAR** via `--add-launcher`: `my_app.updater.Main` (não é projeto separado)

## Fluxo de atualização
```
 Plics SW instalado (MSI ou DEB)
       │
       └─ Menu "Suporte" > "Buscar atualização"
              │
              ├─ [args.length >= 2] ── Usa updaterPath + pkgPath fornecidos via CLI
              │
              └─ [args.length < 2] ── Produção:
                     ├─ Descobre "Plics SW Updater" (.exe no Windows, sem ext. no Linux)
                     └─ GitHub API → baixa .msi (Windows) ou .deb (Linux)
              │
              ▼
       Lança updater <PID> <pkgPath>  →  System.exit(0)
              │
              ▼
       Aguarda PID morrer (onExit().join())
              │
              ▼
       ┌── Windows ── run-update.bat + cmd /c:
       │    taskkill (mata java.exe, javaw.exe, Plics SW.exe)
       │    timeout 10s
       │    msiexec /i <msi> /quiet (retry 3x)
       │    msg.exe notifica
       │
       └── Linux ── run-update.sh + bash:
            pkill -f "Plics SW"
            sleep 10
            pkexec dpkg -i <deb> (retry 3x)
            notify-send notifica
              │
              ▼
       System.exit(0) — saída graciosa
```

## Scripts de empacotamento
- `scripts/config.py`: funções compartilhadas (`run_gradle()`, `run_jlink()`, `run_jpackage()`, etc.)
- `scripts/create-msi.py`: gera instalador Windows (.msi) sem updater (original)
- `scripts/create-deb.py`: gera instalador Linux (.deb) sem updater (original)
- `scripts/updater_config.py`: constantes do updater (nome, main class, UUID)
- `scripts/create-msi-with-updater.py`: gera MSI com updater via `--add-launcher`
- `scripts/create-deb-with-updater.py`: gera DEB com updater via `--add-launcher`

## Últimas alterações

### 2026-08-04: Bug — campo Observação ficava travado em 31px em vez de crescer até o maxHeight
- **Reportado pelo usuário** ao testar em ClienteScreen: campo "muito pequena".
- **Causa**: a nova sobrecarga `Components.TextAreaColumn(label, state, placeholder, minHeight, maxHeight)` (feita mais cedo hoje) construía o `InputProps` via `getInputProps(placeholder)` — helper que por baixo chama `getInputProps(placeholder, 31)`, ou seja, sempre seta `height=31` (pensado pro `Input` de uma linha, nunca usado antes num `TextAreaColumn`). `InputProps.applyTextAreaTheme` prioriza `height` sobre `maxHeight` quando os dois estão setados (`if (height > 0) {...} else if (maxHeight > 0) {...}`) — então a lógica de crescimento nunca rodava, o campo ficava preso no branch de tamanho fixo com 31px.
- **Fix**: a sobrecarga `minHeight`/`maxHeight` agora monta o `InputProps` diretamente (`new InputProps().placeHolder(...).fontSize(...).minHeight(...).maxHeight(...).width(400)`), sem passar por `getInputProps`, garantindo que `height` fique em 0 e o branch de crescimento seja o que roda.
- `./gradlew test`: 281/281. **Ainda não verificado visualmente** por mim (sem automação de clique/teclado) — só a análise de código explica exatamente o sintoma reportado. Testar de novo em ClienteScreen antes de considerar resolvido.

### 2026-08-04: Campo Observação (Cliente e Produto) agora cresce com o conteúdo, sem vazar o scroll pra tela toda
- Substitui o fix anterior de hoje (altura fixa 120px) por uma solução de verdade, feita em `megalodonte-components` (publicada em Maven Local, `megalodonte-libs`/`megalodonte-ecossystem` com pointer do submódulo bumped):
  - `InputProps.minHeight(int)`/`maxHeight(int)` (novo): `TextAreaInput` cresce com o texto digitado dentro desse intervalo (60–160px em Cliente/Produto), em vez de ficar travado num tamanho fixo — só rola por dentro depois de passar do máximo.
  - `TextAreaInput`: rolar o mouse dentro do campo, ao chegar no limite do próprio conteúdo, vazava pro `ScrollPane` da tela inteira (reportado pelo usuário) — fix com `Scroll.confineScrollEvents(textArea)` no construtor (utilitário que já existia, usado em `FlowRow`).
  - Novo `FocusableFieldInterface<T>` (`megalodonte.components`) — `TextAreaInput` e `v2.Input` (que não compartilham implementação nenhuma por baixo) agora implementam o mesmo contrato de foco (`requestFocus()`/`onChangeFocus(...)`), pedido explícito do usuário.
- `Components.java`: nova sobrecarga `TextAreaColumn(label, state, placeholder, minHeight, maxHeight)`, sem mexer na sobrecarga antiga de `height` fixo (ainda usada por Fornecedor, Compras, Contas a Pagar/Receber, Venda, feedback).
- `ClienteScreen`/`ProdutoScreen`: migrados pra nova sobrecarga (60–160px). `FornecedorScreen` não foi tocado (fora do pedido).
- `./gradlew test` (plics-sw): 281/281. `megalodonte-components` não tem testes automatizados (`tasks.test { enabled = false }` no `build.gradle.kts` do módulo) — só compilação verificada lá.
- **Não verificado visualmente** — precisa digitar texto suficiente pra crescer o campo e rolar o mouse dentro dele, e não há automação de teclado/mouse neste ambiente. Testar antes de confiar: (1) o campo cresce ao digitar várias linhas e para de crescer em ~160px; (2) rolar o mouse dentro do campo cheio não move a tela por trás.

### 2026-08-04: Altura do campo Observação (Cliente e Produto) aumentada de 80 pra 120px
- `Components.TextAreaColumn` (usa `TextArea` nativo via `InputProps.height`) seta `prefHeight`/`minHeight`/`maxHeight` todos pro mesmo valor — não existe um conceito separado de "cresce com o conteúdo até um teto" no componente atual, é sempre uma caixa de tamanho fixo com scroll interno pro texto que não cabe. Pedido do usuário ("pode chegar a uma altura máxima") tratado como "escolher um valor fixo mais confortável" — 120px (comportava ~5-6 linhas de texto) em vez dos 80px (default do `TextAreaColumn`) usados em `ClienteScreen`/`ProdutoScreen`.
- `FornecedorScreen` não foi tocado (usuário só pediu Cliente e Produtos).

### 2026-08-04: updates.json completado com tudo desde a última entrada (2026-07-30) até hoje
- Revisado `git log` desde o commit "chore: bump para versão 1.1.1" (2026-07-26) — a entrada `v1.1.2` existente já cobria os commits até 2026-07-30 (foi quando ela foi criada, commit `f471ab6`). Adicionadas as notas do que faltava: tela de Relatórios Financeiros, botões de excluir/reimprimir venda no Histórico do Caixa, campo Observação em Clientes, placeholders em todo o app, rename/remoção de campos em Produtos, e os bugs de borda dupla/overflow de texto/foco do calendário/race condition de Atualizar/clearForm incompletos corrigidos nas últimas sessões.
- **Deliberadamente fora do changelog**: a checagem de acesso provisória (`VerificacaoAcessoService`) — o próprio usuário chamou de "provisório", e é um mecanismo de proteção; anunciar publicamente como funciona reduziria sua utilidade. Também de fora: refatorações internas sem efeito visível (migração pro `v2.Input`, contrato CRUD, categoria do menu Linux/nome do app extraídos pro `gradle.properties`) e commits de teste/experimentação do próprio usuário (ajuste de coordenadas de janela, debounce do `dev.py`, toggle de papel de parede no PDV).
- Versão do app **não foi alterada** (`gradle.properties` continua `appVersion=1.1.2`) — só as notas da entrada já existente foram completadas, como pedido.

### 2026-08-04: Auditoria de clearForm()/populateFieldsFromModel() nas 12 ViewModels de CRUD
- **Motivo**: bug do "Novo em Clientes não limpa tudo" (entrada abaixo) era um caso específico de um padrão mais amplo — campo novo adicionado ao formulário, mas o ponto que "lista os campos à mão" (`clearForm()` ou `populateFieldsFromModel()`) não foi atualizado junto. Auditadas as 12 ViewModels do `ContratoTelaCrudV3`, comparando cada campo setado em `populateFieldsFromModel()` contra o que `clearForm()` reseta (e vice-versa).
- **`FornecedorScreenViewModel.clearForm()`**: faltava `tipoPessoaSelected.set(Data.tiposPessoaList.getFirst())` — corrigido.
- **`ProdutoScreenViewModel.clearForm()`**: faltava `perecivelSelected.set("Não")` — corrigido.
- **`ComprasScreenViewModel.populateFieldsFromModel()`**: faltavam `descontoEmDinheiro` e `fornecedorSelected` — **bug mais sério, direção oposta**: editar uma compra com desconto ou de um fornecedor diferente do selecionado no dropdown e clicar "Atualizar" sobrescrevia silenciosamente o desconto pra 0 e o fornecedor pro que estivesse selecionado (não o real da compra). Corrigido reaproveitando `data.getFornecedor()` (já vem resolvido do `fetchListData()`) e `Utils.deRealParaCentavos(data.getDescontoEmReais())`.
- **`VendaMercadoriaScreenViewModel.populateFieldsFromModel()`**: mesmo bug do desconto — faltava `descontoEmDinheiro`. Corrigido.
- **Sem alterações** (já estavam completas): `CategoriaScreenViewModel`, `TecnicoScreenViewModel`, `OrdemServicoScreenViewModel`, `ComprasAPagarScreenViewModel`, `ContasAReceberScreenViewModel`. `PedidosScreenViewModel` (tela somente-leitura) e `PreferenciasViewModel` (registro único, sem "Novo"/lista) não se aplicam.
- `./gradlew test`: 281/281 (sem teste automatizado dedicado pra nenhum desses — não há `*ViewModelTest.java` no projeto).

### 2026-08-04: Bug — "Novo" em ClienteScreen não limpava o formulário inteiro
- **Causa**: `ClienteViewModel.clearForm()` só resetava `nome/cnpjCpf/celular/email` — os mesmos 4 campos que existiam quando o método foi escrito originalmente. `observacao`, `tipoPessoaSelected`, `isGestante`, `dataNascimento`, `dataNascimentoBebe` e os campos de endereço (`enderecoState`) foram adicionados depois e nunca entraram no reset — mesma classe de bug do `finalModel` incompleto corrigido mais cedo hoje (campo novo adicionado, ponto que precisa "lembrar" de incluí-lo não é atualizado).
- **Fix**: `clearForm()` agora reseta todos os campos do formulário, incluindo `enderecoState.get().clear()` (método que já existia, mesmo usado em `FornecedorScreenViewModel.clearForm()`).
- **Heads up, não corrigido agora**: `FornecedorScreenViewModel.clearForm()` tem uma lacuna parecida — não reseta `tipoPessoaSelected` nem `fornecedorSelected`. Não mexi porque não foi pedido; vale uma auditoria dedicada em todas as 12 ViewModels se o usuário quiser.
- `./gradlew test`: 281/281.

### 2026-08-04: Bug sistêmico — "Atualizar" virava "Cadastrar" em 6 telas (race condition no modoEdicao)
- **Causa raiz** (mesma classe de bug já corrigida uma vez em `CategoriaScreenViewModel` em 2026-06-22, ver `docs/DECISIONS.md`, mas nunca propagada pras outras telas): `ContratoTelaCrudV3.handleAddOrUpdate()` chama `viewModel().handleAddOrUpdate()` (que dispara um `Async.Run(...)` — fire-and-forget, roda em thread virtual) e, **logo em seguida, de forma síncrona**, `viewModel().modoEdicaoState().set(false)`. Como o `Async.Run` só é agendado (não executa na hora), esse reset quase sempre roda ANTES da task assíncrona chegar a ler `modoEdicao.get()` — então clicar em "Atualizar" executava o branch de criar (`salvar()`) em vez de atualizar.
- **Telas afetadas e corrigidas**: `ClienteViewModel`, `ComprasScreenViewModel` (liam `modoEdicao.get()` direto dentro do `Async.Run`), `OrdemServicoScreenViewModel`, `ComprasAPagarScreenViewModel`, `ContasAReceberScreenViewModel`, `ProdutoScreenViewModel` (o branch externo era lido antes, mas `populateModelFromFields()` — chamado de DENTRO do `asyncAtualizar()`/`asyncSalvar()` — lia `modoEdicao.get()` de novo internamente pra decidir se reaproveita o model selecionado ou cria um novo; mesmo race, um nível abaixo).
- **`VendaMercadoriaScreenViewModel` tinha correção PARCIAL**: já capturava `boolean editando` antes do `Async.Run` (fix de 2026-06-22... na verdade aplicado depois, documentado no próprio código), mas `populateModelFromFields()` continuava sendo chamado de DENTRO do `Async.Run` e relia em `modoEdicao.get()` internamente — o mesmo race sobrevivia um nível abaixo. Completado: `populateModelFromFields()` agora só é chamado dentro do async quando não depende mais do modoEdicao ao vivo (model montado síncrono quando `editando`).
- **Telas já seguras, confirmadas**: `FornecedorScreenViewModel`, `TecnicoScreenViewModel`, `CategoriaScreenViewModel` — já montavam o model chamando `populateModelFromFields()` síncrono, antes do `Async.Run`, e passavam o objeto pronto pros métodos `asyncSalvar(model)`/`asyncAtualizar(model)`. Esse é o padrão correto, replicado nas 6 telas corrigidas.
- **Padrão do fix**: `modoEdicao.get()` e `populateModelFromFields()` chamados síncronos (thread da UI), ANTES de qualquer `Async.Run`; `asyncSalvar()`/`asyncAtualizar()` passam a receber o `model` já pronto como parâmetro em vez de montá-lo internamente.
- **Consequência real do bug** (por tela): Cliente/Compras — criava um registro duplicado em vez de atualizar (o `model` "novo" tinha todos os campos preenchidos, então `salvar()` funcionava normalmente). OrdemServico/ComprasAPagar/ContasAReceber/Produto — o `model` "novo" ficava sem `id`, então `service.atualizar(model)` provavelmente falhava (exceção/erro na tela) em vez de duplicar.
- Nenhum teste automatizado dedicado — não há `*ViewModelTest.java` no projeto hoje (havia um pra Categoria em 2026-06-22, segundo o `docs/DECISIONS.md`, mas não existe mais).
- `./gradlew test`: 281/281 (sem mudança de comportamento nos testes de Service/Repository — o bug só existia no fluxo real via `ContratoTelaCrudV3`, que testes de Service não exercitam).

### 2026-08-04: Bug — editar cliente não refletia Observação (nem Data Nasc./Gestante/Endereço) na tabela
- **Causa**: `ClienteViewModel.handleAddOrUpdate()`, no ramo de edição, salva no banco com `model` (completo, vindo de `populateModelFromFields()`), mas troca o item da lista em tela por um `finalModel` construído manualmente campo a campo — um `ClienteModel` novo que só copiava `nome/cpfCnpj/celular/email/pessoaFisica/dataCriacao`. `observacao`, `dataNascimento`, `gestante`, `dataNascimentoBebe` e os campos de endereço nunca foram adicionados a essa cópia conforme o model crescia, então o UPDATE no banco funcionava mas a linha na tela voltava a mostrar esses campos em branco até recarregar a tela. Mesmo padrão existe em `FornecedorScreenViewModel.asyncAtualizar` (lá, `observacao` já estava incluído — só o `ClienteViewModel` estava desatualizado).
- **Fix**: `finalModel` agora copia todos os campos do model. Nenhuma mudança de arquitetura — o padrão de "cópia manual pra forçar o `ListState` a detectar mudança" foi mantido, só completado.
- `./gradlew test`: 281/281 (sem teste automatizado dedicado — não há `*ViewModelTest.java` no projeto).

### 2026-08-04: Campo Observação no formulário de ClienteScreen
- `ClienteModel.observacao`, `ClienteViewModel.observacao` (State) e a exibição em `ItemDetails` já existiam — só faltava o `TextAreaColumn` no `form()`, então o campo nunca era preenchível na prática. Adicionado seguindo o mesmo padrão já usado em `FornecedorScreen`.

### 2026-08-04: RelatoriosScreen — formas de pagamento mais usadas nas vendas
- **Novo**: pizza "Formas de pagamento mais usadas", somando `VendaModel.tipoPagamento` (vendas de mercadoria) + `PedidoModel.formaPagamento` (PDV) no período. Novo record `FormaPagamentoValor(forma, valor)`, novo campo `formasPagamento` em `RelatorioDados`, nova seção no PDF.
- **Vendas "A PRAZO" ficam de fora** — mesmo critério já usado em `receitasVendas` (`VendaRepository.somarVendasPorPeriodo` já excluía): o valor só vira receita reconhecida quando a respectiva conta a receber é efetivamente paga, então contá-la de novo aqui, pela forma de pagamento, duplicaria o valor.
- `PedidoRepository`/`PedidoService` ganharam `listarPorPeriodo(...)` (só existia o `somarPedidosPorPeriodo`, que agora reaproveita o novo método).
- Card de gráficos virou `FlowRow` (era `Row`) pra comportar o 4º gráfico sem estourar a largura da tela.
- `./gradlew test`: 281/281 (2 testes novos: 1 em `RelatorioServiceTest` cobrindo a exclusão de A PRAZO, 1 em `RelatorioPdfExporterTest`).

### 2026-08-04: RelatoriosScreen — novos clientes/fornecedores no período + correção de bug (só 3 de 10 produtos mais vendidos apareciam)
- **Novos clientes/fornecedores no período**: `ClienteRepository`/`FornecedorRepository` ganharam `listarNovosPorPeriodo(dataInicio, dataFim)` (mesmo padrão `dataCriacao BETWEEN ? AND ?` já usado em `VendaRepository`), expostos via `ClienteService`/`FornecedorService`. `RelatorioDados` ganhou os campos `novosClientes`/`novosFornecedores` (List). Exibidos em 2 cards novos na tela (`cardNovosClientes`, `cardNovosFornecedores`) e em 2 seções novas no PDF.
- **Bug corrigido**: `RelatorioService` já buscava até 10 produtos mais vendidos (`TOP_PRODUTOS = 10`), mas `RelatoriosScreenViewModel` só expunha 3 (`produtoMaisVendido1/2/3`) — os outros 7 eram descartados sem nunca aparecer na tela. Trocado por um único `State<String> produtosMaisVendidos` com a lista inteira, uma linha por produto (mesmo padrão já usado em `produtosSemVenda`).
- **Mapeamento feito antes de implementar**: levantamento de tudo que pode ser mostrado no relatório (ranking de clientes/fornecedores por valor, ordens de serviço por status/técnico, ticket médio, forma de pagamento, comparação com período anterior, etc.) — registrado na conversa, não em arquivo; ainda não implementado, só os 2 itens acima (o que o usuário pediu primeiro).
- `./gradlew test`: 279/279 (1 teste novo em `RelatorioServiceTest`, 2 testes de PDF ajustados pro novo formato de `RelatorioDados`).

### 2026-08-04: Placeholders adicionados em todos os inputs sem placeholder do app
- Varredura em todas as `*Screen.java` procurando `InputColumn`/`InputColumnAuth`/`TextAreaColumn` com placeholder vazio (`""`). Corrigidos: `enderecoComponent` (Cidade, Bairro, Rua, Número — usado por Cliente, Fornecedor, Empresa), `FornecedorScreen` (Inscrição estadual, Observação), `ClienteScreen` (Email), `PreferenciasScreen`/`AuthScreen` (Login, Senha, Licença), `ComprasAPagarScreen`/`ContasAReceberScreen`/`VendaMercadoriaScreen`/`ComprasScreen` (Observação), `RelatarErroScreen`/`SugerirMelhoriaScreen` (corpo do texto).
- Campos com máscara (CPF, CNPJ, CEP, telefone, moeda) já tinham placeholder embutido no próprio helper (`getInputPropsV2`) e não precisaram de mudança.
- `./gradlew test`: 278/278.

### 2026-08-04: Checagem provisória de acesso na inicialização (HtmlParser + VerificacaoAcessoService)
- **O que é**: mecanismo temporário ("provisório", palavra do usuário) que compara um "dígito verificador" publicado no site oficial com um valor esperado embutido no app, na inicialização (`Main.initialize()`, dentro do mesmo `Async.Run` que já roda as migrations do Flyway, antes de resolver a rota normal). Se não bater — ou se o site estiver fora do ar/sem internet — mostra um alerta ("O acesso não pode ser realizado.") e navega pra rota `ACESSO_BLOQUEADO` em vez da rota normal.
- **`my_app.services.HtmlParser`** (já existia, criado pelo usuário): usa Jsoup pra buscar `span.verificador` numa URL. Alterado só pra receber a URL como parâmetro em vez de hardcoded, e a extração do `Document` foi separada num método `extrairVerificador(Document)` testável sem rede (`HtmlParserTest`, com fixtures HTML locais via `Jsoup.parse(String)`).
- **`my_app.services.VerificacaoAcessoService`** (novo): dono da URL e do valor esperado, **ambos criptografados em AES/ECB** (reaproveitando `CryptoManager`, o MESMO esquema já usado em `TelegramNotifierFactory` pros tokens do Telegram) — pedido explícito do usuário pra não deixar nem a URL nem o valor esperado em texto puro no GitHub. Qualquer exceção (rede, parsing) retorna `false` (bloqueia).
- **`my_app.screens.acessoBloqueadoScreen.AcessoBloqueadoScreen`** (novo): tela simples — ícone de cadeado, "Acesso bloqueado", texto explicativo, botão "Suporte via WhatsApp" (reaproveita `Data.linkWhatsappSupport` + `Redirect.to(...)`, mesmo padrão já usado em `AuthScreen`). Nova rota `AppRoutes.Screens.ACESSO_BLOQUEADO`.
- **Nota de transparência sobre a URL**: `Data.linkWebsiteOfficial` (usada no botão "Ir para o Site Oficial" do `AuthScreen`) já expõe essa mesma URL em texto puro, comitada há tempo — criptografar só o uso interno do `HtmlParser` não torna a URL do site desconhecida por completo, já que ela aparece de outro jeito na própria UI do app. O que a criptografia protege de fato é o **valor esperado** (o que permitiria falsificar o "dígito verificador" no site pra sempre liberar o acesso).
- **Verificado ao vivo, dos dois lados**: com o valor esperado correto, o app segue normal pra rota real (confirmado — o site já tinha o `span.verificador` publicado com o hash exato que o usuário passou, `curl` direto no site confirmou depois que o `WebFetch` não achava por converter HTML pra markdown e descartar o `<span>` oculto). Com um valor esperado errado (teste temporário, revertido depois), o alerta "Erro" e a tela "Acesso bloqueado" apareceram — confirmando o caminho de bloqueio também funciona.
- `./gradlew test`: 278/278 (dois novos testes em `HtmlParserTest`).

### 2026-08-03: DatePicker — popup do calendário (mês/dias) tematizado
- **Reportado pelo usuário**: o popup do calendário (que abre ao clicar no ícone) usava o azul padrão do Modena pro dia selecionado, sem seguir o tema.
- **Causa**: mesmo problema já resolvido pro `Select` (dropdown do ComboBox) — o popup do calendário roda numa `Scene` separada da do `DatePicker`, então nada aplicado inline no próprio controle chega até lá.
- **Fix**: `DatePickerProps.applyCalendarPopupTheme` — escuta `datePicker.showingProperty()`, e quando o popup abre, localiza a janela dele via `Window.getWindows()` (procura por `.date-picker-popup`) e estiliza diretamente: `.month-year-pane` (cabeçalho mês/ano) vira `theme.colors().primary()` com texto branco, `.day-name-cell` (Dom/Seg/...) usa `textSecondary()`, e cada `.day-cell` reage a hover/seleção/hoje via listener (`.selected`→`primary()`+texto branco, hover→`hover()`, dias de mês adjacente→`placeholder()`, hoje→borda `primary()`). Seletores conferidos direto no `modena.css` real (extraído do jar do JavaFX nesta máquina), não inventados — dia selecionado sinaliza via **style class** `.selected` (não pseudo-classe `:selected`), por isso o listener reage a mudança na `styleClass` list, não só hover.
- Publicado em Maven Local. `./gradlew test`: 276/276.
- **Não verificado visualmente** — abrir o popup do calendário exige um clique real, e não há automação de mouse neste ambiente (só consigo lançar o app e tirar screenshot do estado renderizado, não interagir). Implementação baseada nos seletores reais do modena.css e na técnica já validada pro popup do Select, mas o resultado visual em si não foi conferido — testar clicando no ícone do calendário antes de dar como certo.

### 2026-08-03: Ícones no RelatoriosScreen + card "Lucro líquido" ao lado de "Despesas"
- **Layout**: `cardResumo()` (Lucro líquido) movido pra dentro da mesma `Row` de `cardReceitas()`/`cardDespesas()` (antes ficava sozinho abaixo, largura cheia) — os 3 cards ficam lado a lado agora.
- **Ícones** (usuário achou a tela "muito básica"): novo helper `cardTitulo(titulo, Ikon, cor)` — ícone + texto numa `Row`, reaproveitado nos 5 cards. `AntDesignIconsOutlined.RISE`/verde (Receitas), `.FALL`/vermelho (Despesas), `.FUND`/azul (Lucro líquido) — mesmos ícones já usados nos cards da Home, pra manter consistência visual entre as duas telas. `.TROPHY`/âmbar (Produtos mais vendidos), `.INBOX`/cinza (Produtos sem venda) — novos, sem precedente na Home. Botões "Gerar relatório" (`.BAR_CHART`) e "Baixar PDF" (`.FILE_PDF`) também ganharam ícone via `Button.icon(Components.ikon(...))`.
- Cores hex reaproveitadas das constantes já existentes em `ButtonProps` (verde/vermelho/azul/âmbar dos variants success/danger/primary/warning), não inventadas.
- Verificado ao vivo: layout em 3 colunas confirmado, todos os 5 ícones renderizando nas cores certas, sem exceptions. `./gradlew test`: 276/276.

### 2026-08-03: DatePicker — glow azulado de foco removido (só a borda deveria mudar)
- **Reportado pelo usuário**: campo de data mostrava um "brilho"/halo azulado feio ao ganhar foco, além da borda mudar de cor.
- **Causa**: `DatePickerProps.applyFocusFeedback` já trocava a cor da borda no foco via listener (certo), mas também setava `-fx-faint-focus-color` (a variável que o Modena usa pra desenhar o glow/halo em volta do controle quando focado) pra a MESMA cor do tema — só trocou a cor do glow, não removeu ele.
- **Fix**: `-fx-faint-focus-color` agora vai pra `transparent` (mata o glow), mantendo só a troca de borda via listener como feedback visual de foco — que é o único efeito que o usuário queria.
- `megalodonte-components`: recompilado, republicado em Maven Local. Verificado ao vivo (RelatoriosScreen, campo "Data fim" com foco por padrão na abertura da tela): borda escura sem nenhum glow ao redor. `./gradlew test`: 276/276.

### 2026-08-03: Teste — `megalodonte.components.v2.Input`, reescrito do zero (sem TextField/TextArea/nenhum Control nativo)
- **Primeira versão** (superada, ver abaixo): `v2.Input` ainda usava `javafx.scene.control.TextField` por baixo, só removendo a classe CSS ".text-field" pra escapar do Modena. Funcionava visualmente, mas o usuário pediu algo que não dependesse de **nenhum** input nativo (nem TextField, nem TextArea).
- **Reescrito do zero**: `megalodonte-components/src/main/java/megalodonte/components/v2/Input.java` agora não usa nenhum `Control`/`TextInputControl` — é um `Pane` puro com um `javafx.scene.text.Text` pro conteúdo, um `Rectangle` pro caret (com blink via `Timeline`) e outro pra seleção, e todo teclado/mouse tratado manualmente (`KeyEvent.KEY_TYPED`/`KEY_PRESSED` pra digitar/apagar/navegar/copiar/colar/selecionar, `MouseEvent.MOUSE_PRESSED` pra clicar e posicionar o caret via medição de largura de substring com um `Text` medidor auxiliar). Como não existe nenhum Control nativo, não existe skin nenhuma do Modena pra aplicar — zero disputa de CSS possível, por construção.
- **`megalodonte.props.v2.InputProps`** (novo, não reaproveita o `InputProps` antigo — a API dele pressupõe um `TextInputControl`, que não existe mais aqui): estiliza via lookup por id dos nós internos (`#megalodonte-input-text`, `#megalodonte-input-placeholder`, `#megalodonte-input-caret`, `#megalodonte-input-selection`).
- **Bug encontrado e corrigido durante o teste ao vivo**: a primeira versão do `v2.InputProps.applyTheme` aplicava as restrições de largura/altura (`width`/`height` de `InputProps`) no `Pane` interno (`surface`) em vez do `StackPane` externo (`outer`, o node que de fato vai pro layout do pai) — sem limite no `outer`, ele herdava `Double.MAX_VALUE` de largura máxima do `Region` e "escapava" do slot da `Column`, renderizando longe da posição correta. Fix: aplicar as restrições no `outer`, deixando o `StackPane` redimensionar o `surface` pra caber nele (mesmo padrão do `InputProps` antigo, que sempre constrangeu o `stackPane` externo, não o `TextField` interno).
- Verificado visualmente após o fix (captura ao vivo via `./gradlew run` + `xwd`): campo na posição/tamanho corretos, borda única, ícone "$" e "R$ 0,00" idênticos aos demais campos, sem exceptions no log. `./gradlew test`: 275/275.
- **Limitações conhecidas** (documentadas no javadoc da classe): sem drag-select do mouse, sem IME complexo além do que `KeyEvent.getCharacter()` já entrega, sem menu de contexto (clique-direito). Copiar/colar/recortar (Ctrl+C/X/V) e seleção via teclado (Shift+setas, Ctrl+A) funcionam.

### 2026-08-03: Top 10 produtos mais vendidos (era 3) no RelatoriosScreen
- `RelatorioService.TOP_PRODUTOS`: `3` → `10`. Sem mudança de label na UI/PDF (nenhum dos dois tinha o número "3" no texto, só a lógica de corte).
- `RelatorioServiceTest`: teste que validava o corte em 3 (`deveRetornarTop3ProdutosMaisVendidosSomandoPdvEVendas`) só criava 4 produtos — com o limite em 10 ele nunca mais corta nada, então virou só um teste de ordenação/soma (renomeado `deveRetornarProdutosMaisVendidosOrdenadosSomandoPdvEVendas`, ajustado pra esperar os 4 produtos, não 3). Novo teste `deveLimitarATop10ProdutosMaisVendidosQuandoMaisDeDezForamVendidos` (12 produtos, quantidades distintas) cobre o corte de fato, já que o anterior não cobria mais. `deveRetornarMenosDeTresProdutosQuandoPoucosForamVendidos` renomeado pra `deveRetornarMenosDeDezProdutosQuandoPoucosForamVendidos` (comportamento não mudou, só o nome pra refletir o novo limite).
- `testes-gerais.md` caso #178: "limitado a 3" → "limitado a 10".
- `./gradlew test`: 276/276 (275 + o novo teste do corte em 10).

### 2026-08-03: `Select` — tamanho de fonte padrão dos itens vira "small" (era "body")
- **Causa**: `SelectProps.applyTheme` usava `theme.typography().body()` como fallback quando nenhum `fontSize` explícito era passado — nenhum `Select` da aplicação passa um `fontSize` customizado, então esse fallback era o tamanho real em uso em todo select do app.
- **Fix**: fallback trocado pra `theme.typography().small()`, consistente com o padrão já usado nos labels dos campos (`new Text(label, new TextProps().fontSize(theme.typography().small()))`, usado em praticamente todo `Components.java`).
- **Detalhe adicional**: o popup do dropdown (`ListCell`s) roda numa Scene separada da do `ComboBox` (já documentado no `README.md`/comentário de `SelectProps`) — `-fx-font-size` setado no `ComboBox` não chega até lá por herança de CSS normal. `Select.styleListCell()`/`buttonCell()` (megalodonte-components) agora aplicam `updateFontSize(cell, theme.typography().small())` diretamente em cada célula, então o tamanho pequeno vale tanto pro texto exibido com o combo fechado quanto pros itens da lista aberta.
- `./gradlew test`: 276/276.

### 2026-08-03: `megalodonte.components.inputs.Input` (antigo) marcado `@Deprecated`; app inteiro migrado pro `v2.Input`
- Validado o teste do campo DESCONTO, o usuário pediu pra deprecar o `Input` antigo e migrar o resto da aplicação pro `v2.Input`.
- **`megalodonte.components.inputs.Input`** (megalodonte-components): marcado `@Deprecated(forRemoval = true)`, javadoc aponta pro `v2.Input`. `InputBase`/`PasswordInput`/`TextAreaInput` **não** foram tocados — `PasswordInput` ainda estende `InputBase` (mascaramento de senha não foi reimplementado do zero, fora do escopo pedido) e `TextAreaInput` continua com o fix de `text-area.css` (funciona, não é um `Input` de uma linha).
- **`my_app/domain/components/Components.java`** (plics-sw): todo método que construía `new Input(...)` (antigo) migrado pro novo — `InputColumnCep`, `InputColumnCpf`, `InputColumnDecimal`, `InputColumnCnpjAlfanumerico`, `InputColumnPhone`, `InputColumnNumeric`, `InputColumnCurrency` (as duas sobrecargas, incorporando o que era `InputColumnCurrencyV2` — esse método foi removido, `InputColumnCurrency` normal já usa `v2` agora), `InputColumnComEnterHandler`, `SelectDropDownSearch`, `InputColumnComDynamicSearch`, `InputColumn` (implementação central, usada por `InputColumnAuth`/`InputWithButtonRow` por baixo), `searchInput`. `PDVScreen`'s campo DESCONTO voltou a usar `InputColumnCurrency` puro (sem sufixo `V2`, que não existe mais).
- **Não migrado** (fora do escopo — usam `TextAreaInput`/`PasswordInput`, não `Input`): `TextAreaColumn`/`TextAreaColumnWidthNoRestricted` (Observações, textos longos) e qualquer campo de senha/login. `getInputProps(...)` (antigo, retorna `megalodonte.props.InputProps`) mantido só pra esses dois; novo `getInputPropsV2(...)` (retorna `megalodonte.props.v2.InputProps`) usado por tudo que migrou.
- **`Components.InputRef.requestFocus()`**: antes fazia lookup manual por um `TextInputControl` filho (workaround necessário pro `Input` antigo, que envolve o `TextField` real numa `StackPane`). Como `v2.Input.requestFocus()` já foca o node certo internamente, simplificado pra só chamar `inputRef.requestFocus()`.
- **`PDVScreenViewModel.qtdRef`** (`Ref<Input>`, usado por `InputColumnComEnterHandler`) e o campo "Quantidade" do PDV: import trocado de `megalodonte.components.inputs.Input` pra `megalodonte.components.v2.Input`.
- **Verificado**: `./gradlew test` 275/275; captura ao vivo da PDVScreen inteira (busca de produto, quantidade, e os 5 campos de moeda) sem exceptions no log, todas as bordas únicas e corretas.
- **Não verificado — risco a testar manualmente**: digitação/clique/seleção interativos em geral, e **especificamente acentuação em português** (ç, ã, õ, é, etc., muito comum em nome/descrição/endereço) — o tratamento de teclado do `v2.Input` é manual (`KeyEvent.getCharacter()`), sem a integração de IME que um `TextInputControl` nativo tem "de graça". Sem ferramenta de automação de teclado neste ambiente (xdotool/similar não instalados), não consegui testar isso sozinho — validar em produção antes de confiar cegamente, principalmente nos campos de nome/endereço/descrição usados em Cliente, Fornecedor, Produto, etc.

### 2026-08-03: `v2.Input` — texto vazava pra fora da borda em largura fixa; clip + auto-scroll horizontal
- **Bug reportado pelo usuário**: ao fixar `width(...)` num `v2.Input`, digitar mais caracteres do que cabiam visualmente fazia o texto vazar pra fora da borda arredondada, em vez de ficar contido/rolar como um `TextField` de verdade.
- **Causa**: `javafx.scene.layout.Pane` (usado como superfície do campo) não recorta filhos que ultrapassam seus próprios limites por padrão — diferente de um `Control` com skin, que clipa automaticamente.
- **Fix**: `surface.setClip(...)` com um `Rectangle` cujo tamanho é bindado ao do próprio `Pane` (corta qualquer overflow visual), mais rolagem horizontal de verdade — `scrollOffset` recalculado em `layoutContent()` a cada mudança, deslocando texto/caret/seleção pra manter o caret sempre visível dentro da área útil (largura menos padding), igual um `TextField` nativo faz. `indexAt(...)` (clique pra posicionar o caret) ajustado pra considerar o `scrollOffset` atual.
- **`InputProps.maxWidth(int)`** (novo, pedido junto): mesmo padrão de `SelectProps`/`ButtonProps` — aplicado no `outer` (StackPane), não no `surface`, mesmo raciocínio do bug de layout do teste anterior (o node que vai pro layout do pai é o `outer`).
- Verificado: `./gradlew test` 275/275; captura ao vivo do ProdutoScreen (SKU, Nome, Marca, Tamanho, Modelo) sem exceptions, campos com bordas corretas em repouso. **Não verificado interativamente** (mesma limitação de automação de teclado/mouse já registrada acima) — o comportamento de overflow/scroll em si (digitar além da largura visível) depende de teste manual do usuário.
- **Follow-up — bug reportado pelo usuário**: digitar uma string gigante (provavelmente via colar, `pasteFromClipboard`, que insere tudo de uma vez em vez de char a char) e depois apagar tudo de uma vez (seleção + backspace/delete) deixava o texto renderizando por trás/embaixo do ícone à esquerda. Causa provável: o `scrollOffset` acumulado pro texto longo ficava temporariamente maior do que o necessário pro texto novo (curto), e o rastreamento baseado só na posição do caret nem sempre corrige isso a tempo em todo caminho de edição — resultando em `layoutX = leftPad - scrollOffset` negativo, empurrando o texto pra antes do ícone. **Fix defensivo**: adicionada uma trava extra em `layoutContent()` que limita `scrollOffset` ao máximo necessário pro comprimento ATUAL do texto (`widthOf(value) - visibleWidth`), independente de como ele chegou lá — garante que nunca fica "preso" num valor grande demais depois que o texto encolhe. `./gradlew test`: 275/275. **Não reproduzido nem verificado por mim** (sem automação de teclado/mouse no ambiente) — é um fix raciocinado a partir do código, não confirmado ao vivo; testar o cenário exato (colar string grande, selecionar tudo, apagar) antes de confiar.

### 2026-08-03: `v2.Input` — caret sempre começa no final do valor, não no início
- **Pedido do usuário**: um campo com valor "0" mostrava o caret antes do caractere ("|0") ao ser populado; o esperado é sempre depois ("0|"), como um `TextField` nativo se comporta ao carregar um valor.
- **Causa**: `bind()`/`applyInitialFormat()` só mandavam o caret pro final quando `lockCursorToEnd()` tinha sido chamado explicitamente — a maioria dos campos (`InputColumn`, ou seja, Nome/Marca/Tamanho/etc.) não chama esse método, então caíam no caminho que mantinha `caretIndex=0` (valor inicial do campo).
- **Fix**: como o guard de `internalChange` já garante que esse listener de `state.subscribe(...)` só roda pra mudanças **externas** de state (valor inicial, reset de formulário, carregar registro pra edição) — nunca pra auto-notificação da digitação do próprio usuário, que já é filtrada — trocado `caretIndex = lockCursorToEnd ? value.length() : Math.min(...)` por simplesmente `caretIndex = value.length()` sempre, tanto em `bind()` quanto em `applyInitialFormat()`. `lockCursorToEnd()` continua existindo e válido pro OUTRO uso que já tinha (manter o caret no final durante a reformatação em `notifyChange()`, usado pelos campos de moeda/CEP/CPF/etc.).
- `./gradlew test`: 275/275.

### 2026-08-03: Correção de borda dupla em Input/Select em toda a aplicação (megalodonte-components)
- Reportado pelo usuário no PDVScreen (campos SUBTOTAL/DESCONTO/TOTAL A PAGAR/TOTAL RECEBIDO/TROCO e os selects "Cliente"/"Forma de pagamento"), mas o bug era em `InputProps`/`SelectProps` do `megalodonte-components` — afeta **todo input de texto/moeda/numérico e todo select da aplicação**, não só o PDV.
- **Causa raiz confirmada** (via pesquisa, ver `DECISIONS.md` do megalodonte-ecossystem para os detalhes/fontes): Modena define `.text-field`/`.text-area` com **3 camadas** de `-fx-background-color` empilhadas (`-fx-shadow-highlight-color`, `-fx-text-box-border`, `-fx-control-inner-background`), cada uma com seu próprio inset — isso é o mecanismo nativo do "bevel" do Modena. Sobrescrever borda/insets via `node.setStyle()` (inline) não substitui essas 3 camadas de forma confiável — limitação documentada do motor de CSS do JavaFX pra propriedades multi-camada — por isso a camada `-fx-text-box-border` do Modena continuava visível por baixo da borda temática da aplicação, aparentando uma borda dupla/anel escuro por dentro.
- **Fix real**: stylesheet próprio (`getStylesheets().add(...)`, origem "author", que tem prioridade sobre o Modena independente de pseudo-classe) que **achata a declaração pra 1 camada só** em vez de só zerar insets — `text-field.css` (novo, anexado em `Input`) e `text-area.css` (já existia pro fix de cantos do TextArea; estendido). A borda real (cor/largura/raio) continua vindo de `InputProps` via inline, aplicada por cima sem mais nada do Modena pra disputar.
- Tentativas anteriores (zerar `-fx-background-insets`/`-fx-border-insets`/`-fx-effect` só via inline em `InputProps`) não resolveram de forma confiável e foram removidas do código após a causa raiz ser confirmada, pra não deixar tentativas mortas no `applyInputBorderStyling`/`applyTextAreaTheme`. `SelectProps`/`ComboBox` nunca teve esse problema (não usa `.text-field`/`.text-input`) — o insets-reset de lá é defensivo/desnecessário mas inofensivo, mantido por consistência.
- Publicado em Maven Local; `./gradlew test` seguiu 100% (275/275) em cada rodada.

### 2026-08-03: Checkboxes de cor do ProdutoScreen reativos + Checkbox tematizado (megalodonte-components)
- **`megalodonte-components`** (repo externo, `megalodonte-ecossystem/megalodonte-libs/megalodonte-components`): `Checkbox` antes era um wrapper puro de `javafx.scene.control.CheckBox`, sem nenhum estilo do tema — destoava do resto da UI. Criado `CheckboxProps` (estende `TextComponentProps`), aplicando cor do box/mark via lookup dos sub-nodes do skin (`.box`/`.mark`, mesmo padrão já usado em `DatePicker.applyIcon`/`SimpleTableProps.applyHeaderStyling`, já que esses sub-nodes só existem depois do skin instalado): `primary()` quando marcado, `surface()` quando não, borda de `theme.colors().border()`. Reage também a troca de tema em runtime. Publicado em Maven Local (`./gradlew build publishToMavenLocal`, mesma versão `1.0.0-beta`).
- **`ProdutoScreen.coresCheckboxes()`**: renderização das cores era imperativa (`vm.cores.onChange(...)` reconstruindo manualmente `Row`s de 4 `CheckBox`es a cada mudança da lista). Substituído por `ForEachState.of(vm.cores, this::corCheckbox)` + `FlowRow.items(ForEachState)` — lista flui e quebra linha sozinha (sem mais o chunking manual de 4 em 4), e cada `Checkbox` agora é o componente tematizado do megalodonte-components. Bind bidirecional com `vm.coresSelecionadas` (`ListState<String>`) mantido via um `State<Boolean>` por cor, sincronizado nos dois sentidos.
- **Sem mudança de comportamento** para o usuário (mesma seleção múltipla de cores, caso de teste #153 continua válido) — só visual (checkbox agora usa as cores do tema) e interno (reativo em vez de imperativo).

### 2026-08-03: Placeholders no ProdutoScreen, rename "Descrição curta"→"Nome" e remoção do campo Comissão
- **Placeholders**: todos os inputs de texto/numérico/moeda do formulário de ProdutoScreen ganharam placeholder (`Components.InputColumn`/`InputColumnNumeric`/`InputWithButtonRow`), seguindo o padrão "Ex: ..." já usado nas demais telas. `Components.InputWithButtonRow` ganhou overload com parâmetro `placeholder` (usado no campo de SKU).
- **Rename**: label "Descrição curta" → "Nome" (campo continua mapeado em `ProdutoModel.descricao`, sem mudança de model/coluna).
- **Remoção do campo Comissão**: campo nunca era exibido em nenhuma tabela/relatório/cálculo — só existia no formulário e na tabela `produtos`, sem uso downstream. Removido de `ProdutoModel` (campo `comissao`), `ProdutoScreenViewModel` (state, `populateModelFromFields`, `populateFieldsFromModel`, `clearForm`, cópia em `asyncAtualizar`) e `ProdutoScreen` (input do formulário). Coluna do banco removida via `V32__remove_comissao_produtos.sql` (`ALTER TABLE produtos DROP COLUMN comissao` — suportado desde SQLite 3.35, projeto usa 3.45.1.0). `ProdutoRepositoryTest` ajustado para não setar mais o campo.
- **Testes manuais**: coluna "Comissão" removida das tabelas de ProdutoScreen nos 5 arquivos de perfil (`testes-loja-de-roupas.md`, `testes-petshop.md`, `testes-lanchonete.md`, `testes-acougue.md`, `testes-mercado.md`).

### 2026-08-02: Botão "Imprimir venda" no Histórico do Caixa (PedidosScreen)
- **`PedidosScreenViewModel`**: novo método `imprimirVendaSelecionada()`, espelhando `PDVScreenViewModel.imprimirNota()` — opera sobre `pedidoSelecionado.get()` (linha selecionada na tabela) em vez da última venda finalizada. Reaproveita `carregarPortaImpressora()` e o record `DadosNotaPedido`/`carregarDadosNotaPedido()` já existentes na classe (duplicado por VM, mesmo padrão já usado para `EscPosPrinter` em outras telas). Novos campos `EmpresaService empresaService` e `EscPosPrinter escPosPrinter` no construtor; `onDestroy()` passou a fechar também `empresaService`.
- **`PedidosScreen`**: botão "Imprimir venda" adicionado na `Row` junto ao "Excluir venda selecionada", ambos atrás de `Show.when(vm.temPedidoSelecionado, ...)`.
- **Teste manual**: `testes-gerais.md` caso #181 (seção PedidosScreen).

### 2026-08-01: RelatoriosScreen — produtos sem venda no período
- **Card "Produtos sem venda no período"** na RelatoriosScreen, logo abaixo do top 3 mais vendidos: lista todos os produtos cadastrados que **não tiveram nenhuma venda no período selecionado** (mesma janela do ranking), ordenados por descrição, com unidade entre parênteses. Quando todos os produtos venderam, mostra "Todos os produtos tiveram venda no período".
- **`RelatorioService`**: agregação extraída para `quantidadesVendidas(long, long)` (privado, reutilizado por `produtosMaisVendidos` e `produtosSemVenda`); `produtosSemVenda` = todos os `produtos` (via `ProdutoService.listar()`) cujo `codigo_barras` não está no conjunto de vendidos do período, com `quantidade = ZERO` e descrição resolvida do cadastro. `RelatorioDados` ganhou o campo `produtosSemVenda`.
- **ViewModel/Screen**: `State<String> produtosSemVenda` renderizado por um `Text(State<String>)` multi-linha (`\n` separa cada produto) — o `Text` do framework subscribe o `ReadableState`, então reage a cada relatório novo.
- **PDF**: não incluído — o `RelatorioPdfExporter` é de página única e a lista pode ser longa (decisão em `docs/DECISIONS.md`).
- **Testes**: 273 → 275 (`RelatorioServiceTest` +2 sem-venda no período e venda fora do período; asserts extras nos casos existentes), 0 falhas.
- **Teste manual**: `testes-gerais.md` RelatoriosScreen +1 caso (#180 produtos sem venda no período).

### 2026-08-01: RelatoriosScreen — top 3 produtos mais vendidos do período
- **Card "Produtos mais vendidos do período"** na RelatoriosScreen: mostra os 3 produtos com maior quantidade vendida no período escolhido, somando as duas fontes de venda — `pedido_itens` (PDV, filtrado pelo período via `pedidos.dataCriacao`) e `vendas` (VendaMercadoriaScreen) — com descrição/unidade resolvidas da tabela `produtos`. Produto vendido sem cadastro cai para o `codigo_barras` como descrição.
- **`RelatorioService.produtosMaisVendidos(long, long)`** (novo): agrega em Java (`Map<codigo, quantidade>` com `BigDecimal::add`), ordena desc, limita a 3 — mesmo estilo dos "somarXPorPeriodo" (que já agregavam em Java, sem GROUP BY). `RelatorioDados` ganhou o campo `produtosMaisVendidos` (lista de `ProdutoMaisVendido` — record novo).
- **`VendaRepository`/`PedidoItemRepository`**: novos `listarPorPeriodo` (mesmo padrão de `somarVendasPorPeriodo`); delegates em `VendaService`/`PedidoItemService`.
- **PDF**: `RelatorioPdfExporter` ganhou a seção "PRODUTOS MAIS VENDIDOS DO PERÍODO" (espelha a tela; "Nenhum produto vendido no período" quando vazio).
- **Testes**: 265 → 273 (`VendaRepositoryTest` +2, `PedidoItemRepositoryTest` +2, `RelatorioServiceTest` +3 ranking e soma PDV+vendas, `RelatorioPdfExporterTest` +1 seção no PDF), 0 falhas.
- **Teste manual**: `testes-gerais.md` RelatoriosScreen +2 casos (#178 ranking, #179 período sem vendas).

### 2026-07-31: RelatoriosScreen — relatório financeiro por período, com gráficos e exportação em PDF
- **Nova tela** (`AppRoutes.Screens.RELATORIOS`, acessível via Home > menu "Gerencial" > "Relatórios"): seleciona Data Início/Fim (padrão: mês atual, igual à Home) e mostra Receitas/Despesas/Lucro Líquido do período + situação atual de contas em aberto — reaproveita os mesmos métodos `somarXPorPeriodo`/`getTotalEmAberto` já usados pela `HomeScreenViewModel`, só que com período livre em vez de fixo no mês atual
- **`RelatorioService`** (novo, `my_app.services`): orquestra `VendaService`, `PedidoService`, `ContaAreceberService`, `CompraService`, `ContasPagarService` — nenhuma lógica de agregação nova, só soma o que cada Service já expõe
- **Gráficos**: `javafx.scene.chart` nativo (`BarChart` Receitas x Despesas x Lucro; 2x `PieChart` de composição). O framework de UI do projeto não tem "reconstruir componente reativo" — os objetos `PieChart.Data`/`XYChart.Data` são criados uma vez e só têm o valor mutado a cada novo relatório, que é como o JavaFX Chart já atualiza sozinho
- **PDF**: `Apache PDFBox 2.0.29` (nova dependência, Apache 2.0) — `RelatorioPdfExporter` escreve o relatório em texto simples num PDF (cabeçalho com dados da empresa, igual ao já usado no ESC/POS). Botão "Baixar PDF" abre `FileChooser` nativo
- **Testes**: `RelatorioServiceTest` (soma real de vendas/pedidos/contas/compras num período + período vazio retorna zeros) e `RelatorioPdfExporterTest` (gera um PDF de verdade e confere o texto extraído via `PDFTextStripper`). 265 testes, 0 falhas
- **Teste manual**: nova seção RelatoriosScreen em `testes-gerais.md` (#171-#177) + `testes.md` reindexado

### 2026-07-31: Excluir venda do histórico do caixa (PedidosScreen)
- **`PedidosScreenViewModel`/`PedidosScreen`**: tela era só leitura (`handleClickMenuDelete()` vazio). Agora tem botão "Excluir venda selecionada" (só aparece quando há um pedido selecionado, via `ComputedState temPedidoSelecionado`), com confirmação antes de excluir.
- **`PDVService.excluirVenda(int pedidoId)`** (novo, transacional — reverso de `finalizarVenda()`): devolve o estoque de cada item vendido (`ProdutoService.incrementarEstoque`), apaga os `pedido_itens` (`PedidoItemRepository.excluirPorPedidoId`, novo), apaga as contas a receber vinculadas se a venda era fiada (`ContaAreceberService.excluirPorVendaId`, já existia) e por fim o `pedido`. Lança `IllegalArgumentException("Venda não encontrada")` se o id não existir — `Session.withTransaction()` do Persism envolve isso numa `PersismException`, mas preserva a mensagem original.
- **Reflexo nos cards da Home**: `EventBus.publish(DadosFinanceirosAtualizadosEvent.getInstance())` após excluir — `HomeScreenViewModel` já escuta esse evento e recalcula Receitas/Despesas/Lucro/"Hoje você fez" (que somam `pedidos.totalLiquido`), sem precisar de nenhuma mudança na Home.
- **Melhorias de UI aproveitadas na mesma tela**: campo de busca (`Components.searchInput` — a infraestrutura de `filteredList`/`matchesSearch` já existia mas não estava exposta na tela) e coluna "Cliente" agora mostra o nome (antes mostrava o `clienteId` numérico) — fecha a pendência registrada em 2026-07-27.
- **Testes**: `PDVServiceTest` +4 casos (estoque restaurado, contas a receber apagadas só da venda excluída — não mexe em outra venda fiada —, exceção em venda inexistente). 260 testes, 0 falhas.
- **Teste manual**: `testes-gerais.md` — seção PedidosScreen ganhou casos de busca (#167) e exclusão de venda à vista/fiada/sem seleção (#168-#170).

### 2026-07-28: APP_VERSION lido em runtime (fim do hardcode duplicado em Main.java)
- **Problema**: `Main.APP_VERSION` era uma string hardcoded (`"1.1.1.1_Patch_5"`) mantida manualmente em paralelo a `gradle.properties.appVersion` (`"1.1.1.1"`) — já tinham desincronizado (números diferentes) antes desta correção.
- **`gradle.properties`**: campo único `appVersion` virou dois campos — `appVersion` (base, x.x.x) e `appPatch` (contador). Versão final = `appVersion` se `appPatch=0`, senão `appVersion.appPatch`.
- **`Main.APP_VERSION`**: agora lê `System.getProperty("plics.appVersion", "dev")` — mesmo padrão do `isMicrosoftStore`. Setado via `-Dplics.appVersion=...` na task `run` do Gradle (`build.gradle.kts`) e em `scripts/config.py` (`run_jpackage()`, então todo build empacotado — msi/deb, com/sem updater — já embute automaticamente).
- **`scripts/bump_version.py`** (novo): `patch` incrementa `appPatch`; `release X.Y.Z` define nova base e zera o patch. Substitui a edição manual de `gradle.properties`.
- **`docs/AI_RULES.md`**: checklist de "alterar versão do app" atualizado — não há mais passo de editar `Main.java`.

### 2026-07-28: Desconto e impressão alternativa no PDVScreen
- **Desconto**: PDVScreen não tinha input de desconto — `PedidoModel.desconto`/`pedido_itens.desconto` já existiam na model/migration, mas `PDVService.finalizarVenda()` sempre gravava `BigDecimal.ZERO`. Adicionado `PDVScreenViewModel.desconto` (currency input) + `totalAPagar` (subtotal - desconto, nunca negativo) recalculados a cada mudança em itens/desconto/recebido. `PDVService.finalizarVenda()` ganhou overload com parâmetro `desconto`, aplicado ao `totalLiquido` do pedido e às parcelas geradas (fiado usa o valor líquido, não o bruto)
- **Impressão alternativa**: PDVScreen não tinha o botão "Imprimir (modo alternativo)" que já existe em VendaMercadoriaScreen (envia bytes ESC/POS crus via `WinRawPrinter`, fallback pra impressoras não reconhecidas pelo Java Print Service). `EscPosPrinter.gerarBytesEscPos(PedidoModel, ...)` criado (só existia a versão pra `VendaModel`); `PDVScreenViewModel.imprimirNotaAlternativa()` + `resolverNomeImpressoraTermica()` replicam o padrão da tela de vendas
- **Testes**: +3 casos em `PDVServiceTest` (desconto aplicado ao total líquido, total não fica negativo se desconto > total, parcelas geradas com valor já descontado) — 256 testes, 0 falhas

### 2026-07-28: Correções de publicação (publisher, erro no Telegram, NPE na 1ª execução, build sem updater pra Store)
- **Publisher "Unknown" no instalador**: `appVendor` existia no `gradle.properties` mas nenhum script chegava a lê-lo. `scripts/config.py` ganhou a constante `VENDOR` e `--vendor` no `run_jpackage()` (usado por todos os scripts de empacotamento). `appVendor` = "Coerente Inc."
- **Notificação de erro no Telegram chegava como "null"**: `Main.handleAppError()` mandava só `t.getMessage()` (null em exceções comuns como `NullPointerException` sem mensagem). Agora inclui tipo da exceção + 3 primeiras linhas do stack trace. `TelegramNotifier` também perdeu o `parse_mode=Markdown` (nunca usado pra formatação e arriscava rejeitar a mensagem inteira por causa de `_` em nomes de pacote como `my_app`) e ganhou `URLEncoder` no texto
- **NPE em `AuthScreenViewModel.isLicenseInvalid()` na primeira execução**: `LICENCAS_PRODUCAO.contains(license)` — lista imutável (`List.of`), `contains(null)` lança NPE. `licensa` é `null` em toda instalação nova (`V16__dados_padrao.sql` não preenche essa coluna), e `Main.initialize()` chama essa validação antes de sair da splash — quebrava a primeira execução em qualquer máquina nova (reportado durante teste de publicação na Microsoft). Fix: retorna `true` direto quando `license == null`
- **Updater com mensagem amigável quando aberto sem argumentos**: "Erro: args insuficientes" (visível em fonte grande pra quem abre o `.exe` do updater manualmente, ex. revisor de loja de app) virou "Updater deve ser aberto programaticamente, você não precisa executá-lo"
- **Build sem updater pra Microsoft Store**: `scripts/create-msi.py` (que estava desatualizado em relação a `create-msi-with-updater.py` — faltava `--win-upgrade-uuid`, não limpava `temp_dir`) foi atualizado e ganhou `--java-options -Dplics.microsoftStore=true`. `Main.isMicrosoftStore` lê essa property; `HomeScreen.menuBar()` não monta mais o item "Buscar atualização" nesse caso (mesmo padrão do `Main.isFlatpak`). `UPGRADE_UUID` movido de `updater_config.py` pra `config.py` (não é específico do updater, é usado em qualquer MSI)

### 2026-07-27: Padronização do contrato CRUD, validações movidas para as Services, cobertura de testes manuais
- **`ViewModelScreenContract.populateFromModel()` → `populateFieldsFromModel()`**: nome antigo era ambíguo (parecia "popular o model", mas populava os campos da UI a partir do model). Renomeado em `ViewModelScreenContract`, `ContratoTelaCrudV3` e nas 12 ViewModels que implementam o contrato
- **`populateModelFromFields()`**: novo método abstrato, inverso do anterior — monta e retorna um `Model` a partir do estado atual dos campos do formulário, sem parâmetros (decide internamente criação vs edição via `modoEdicao`). Padroniza um único jeito de montar Model em todas as 12 ViewModels, substituindo `getModelFromFields`/`fillModelFromForm` (com aridades diferentes) e trechos inline que cada ViewModel fazia à sua maneira. `VendaMercadoriaScreenViewModel` é a única exceção que sempre retorna um Model **novo** mesmo em edição (necessário porque `allDataList.updateIf()` compara por referência)
- **Validações de CRUD centralizadas nas Services**: removidas checagens de negócio duplicadas nas ViewModels (nome/técnico/cliente/produto obrigatórios, limites de pagamento/recebimento) que já existiam nas Services (`FornecedorService`, `OrdemServicoService`, `TecnicoService`, `VendaService`, `ContasPagarService`, `ContaAreceberService`). Duas validações que só existiam na ViewModel foram movidas: `ProdutoService.validar()` ganhou a checagem de validade não-passada; `PreferenciasService` ganhou `validar()` (login/senha obrigatórios com credenciais habilitadas — antes não tinha validação nenhuma)
- **`FornecedorModel.pessoaFisica`** (Migration V31): tipo de pessoa (Física/Jurídica) do form agora é persistido, igual a `ClienteModel`. `FornecedorService.validar()` passou a exigir CPF ou CNPJ de acordo com esse campo quando informado (fallback OR para registros legados sem o campo)
- **`CompraService.salvar(CompraModel)`**: novo overload Model-based (o `salvar(CompraDto)` existente passou a delegar pra ele), permitindo que `ComprasScreenViewModel` retorne um `CompraModel` de `populateModelFromFields()` como as demais telas
- **Testes**: suíte foi de 237 para 253 (`PreferenciasServiceTest` e `ContasPagarServiceTest` de `my_app.services` criados do zero; `FornecedorServiceTest`/`ProdutoServiceTest`/`CompraServiceTest` ganharam casos novos), 0 falhas
- **Testes manuais (`.md`)**: auditoria completa de campos de tela vs colunas testadas nos 7 arquivos `.md`; adicionadas colunas/casos faltantes (Estoque Mínimo, Tipo de Pessoa em Fornecedor, Observação em Compras, logomarca, PDV forma de pagamento/criar cliente, logout); corrigidas incoerências de cadastro referenciado sem existir (nova tabela de categorias de teste em `testes-gerais.md`, `ClienteScreen #9`, ordem de `#35`/`#36`, passos de "Fluxo Completo" que recriavam cadastros já feitos) e numeração duplicada (`#66` em loja-de-roupas, `#122-124` em petshop)
- **Achados de UI sem controle correspondente** (registrados em `docs/TODO.md`, não corrigidos nesta sessão): OrdemServicoScreen não tem select de Status no form; PedidosScreen não tem busca e mostra ID do cliente em vez do nome

### 2026-07-18: Release v1.1.0
- **Versão atualizada**: Main.java → "1.1.0", gradle.properties → 1.1.0, README.md → 1.1.0
- **updates.json**: nova entrada v1.1.0 com 25 notas (Feat, Fix, Chore)
- **Notas incluem**: splash screen, HotReload+Lombok, busca/filtro global, estoque mínimo, impressão crediário com parcelas, redesign boas-vindas, logout, maximize/minimize, FlowRow, ícones animados, e 10 correções



### 2026-07-14: Impressão de nota de venda no crediário com parcelas e assinatura
- **`PDVScreenViewModel`**: state `numeroParcelas` (default "1"), `finalizarVenda()` passa `numeroParcelas` e `formaPagamento` ("CREDIARIO" fiado, "A VISTA" contrário)
- **`PDVScreen`**: input "Nº Parcelas" visível quando fiado marcado
- **`PDVService.finalizarVenda()`**: novo param `numeroParcelas`, usa `Parcela.gerarParcelas()` para N parcelas mensais
- **`EscPosPrinter`**: sobrecarga `imprimir(VendaModel, List<ContaAreceberModel>)`, seção "PARCELAS" + assinatura do cliente na nota
- **`VendaMercadoriaScreenViewModel`**: `imprimirNotaDeVenda()` busca parcelas quando "A PRAZO"
- **`PDVServiceTest`**: atualizado para nova assinatura, teste fiado com 3 parcelas

### 2026-07-13: Correção — barra de título com maximize desabilitado após modal/alerta
- **`ShowModal()`**: adicionado `initOwner(context.selfStage())` sem `initModality` (default NONE), `setOnHidden(requestFocus + toFront)` no owner, conteúdo envolto em `Scroll` (width 800)
- **`ShowAlertError()`**: adicionado `alert.initModality(Modality.NONE)` — Glass toolkit não toca nas decorações da janela owner. Trocado `showAndWait()` por `show()`. Removido `setOnCloseRequest`

### 2026-07-12: Estoque mínimo no ProdutoScreen
- **Migration V29**: adicionada coluna `estoque_minimo REAL DEFAULT 0` à tabela `produtos`
- **`ProdutoModel`**: adicionado campo `estoqueMinimo` (BigDecimal)
- **`ProdutoScreenViewModel`**: state `estoqueMinimo`, preenchido em `fillModelFromForm`, `populateFromModel`, resetado em `clearForm`, copiado em `asyncAtualizar`
- **`ProdutoScreen`**: input numérico no formulário, coluna na tabela, detalhe no modal

### 2026-07-12: Busca/Filtro global em todas as telas CRUD
- **`ViewModelScreenContract<Model>`**: adicionado `searchState`, `allDataList`, `filteredList`, `matchesSearch()` abstrato, `fetchListData()` abstrato
- **Padrão**: cada ViewModel agora declara campos `matchesSearch()` e `fetchListData()`, substituindo listas locais por `allDataList`/`filteredList`
- **`Components.searchInput()`**: adicionado ao topo das telas que usam `mainView()` e às telas com `render()` customizado
- **Screens refatoradas**: Categoria, Tecnico, Fornecedor, Compras, VendaMercadoria, ContasAReceber, ComprasAPagar, OrdemServico, Pedidos

### 2026-07-08: Substituição do jSerialComm por JSSC
- **Problema**: jSerialComm 2.10.4 falhava ao extrair DLL nativa (Acesso negado + DLL ARM64 em CPU AMD64).
- **Solução**: Trocado para `io.github.java-native:jssc:2.10.2` (JSSC).
- **PreferenciasViewModel**: `SerialPortList.getPortNames()` em vez de `SerialPort.getCommPorts()`.
- **EscPosPrinter**: `new jssc.SerialPort(porta)` + OutputStream wrapper.
- **Main.java**: `corrigirArquiteturaNativa()` mantida (corrige `os.arch` se `aarch64` em CPU AMD64).

### 2026-07-08: Listagem de impressoras Windows no select de preferências
- **PreferenciasViewModel.load()**: agora também lista impressoras Windows via `PrintServiceLookup.lookupPrintServices()` como `"Nome - Spooler"`.
- **EscPosPrinter.resolverOutputStream()**: detecta se o nome é serial (`COM\d+`) ou impressora Windows. Se for impressora, busca `PrintService` por nome.
- **isPrinterAcceptingJobs()**: verifica atributo `PrinterIsAcceptingJobs` antes de criar `PrinterOutputStream`. Se offline/não aceitando, loga `warn` e tenta fallback (impressora padrão → preview .txt).
- **DevicesTest.java**: teste de diagnóstico para listar portas seriais e impressoras Windows.

### 2026-07-08: Porta da impressora salva em preferências e usada nas telas de venda
- **Migration V23**: adicionada coluna `porta_impressora TEXT` à tabela `preferencias`.
- **`PreferenciasModel`**: adicionado campo `portaImpressora` com `@Column(name = "porta_impressora")`.
- **`PreferenciasDto`**: adicionado campo `portaImpressora`.
- **`PreferenciasViewModel.load()`**: restaura `comportsStateSelected` a partir da porta salva.
- **`PreferenciasViewModel.salvar()`**: extrai o nome da porta do item selecionado (parte antes de " - ") e salva no model.
- **`VendaMercadoriaScreenViewModel`**: porta carregada de `PreferenciasService` em vez de hardcoded `/dev/rfcomm0`.
- **`PDVScreenViewModel`**: porta carregada de `PreferenciasService` e passada ao `EscPosPrinter`; se houver porta definida, usa o construtor com porta, senão usa o fallback (impressora do sistema).

### 2026-07-04: Adicionadas propriedades cor, tamanho, modelo ao ProdutoScreen
- **`ProdutoModel`**: adicionados campos `cor`, `tamanho`, `modelo` (String).
- **`ProdutoDto`**: adicionados campos `cor`, `tamanho`, `modelo`.
- **Migration V22**: `ALTER TABLE produtos ADD COLUMN cor/tamanho/modelo TEXT`.
- **`Data.java`**: adicionadas listas `listaCores` (16 cores) e `listaTamanhos` (PP, P, M, G, GG, XG, UN).
- **`ProdutoScreenViewModel`**: adicionados states `corSelected`, `tamanhoSelected`, `modelo`; atualizados `fillModelFromForm`, `clearForm`, `populateFromModel`.
- **`ProdutoScreen`**: adicionados selects de Cor/Tamanho e input de Modelo no formulário; colunas na tabela; detalhes no modal.
- **`ProdutoServiceTest`**: +1 teste (`deveSalvarProdutoComPropriedades`).
- **`ProdutoScreenViewModelTest`**: +1 teste (`deveSalvarProdutoComPropriedades`).
- **Arquivos `.md` de teste**: adicionadas colunas Cor, Tamanho, Modelo nas tabelas de ProdutoScreen de todos os 5 perfis.

### 2026-07-03: Impressão de nota de venda com ESC/POS
- **`EscPosPrinter`**: implementado `imprimir(VendaModel)` e `imprimirNotaVenda(PedidoModel, List<PedidoItemModel>, ClienteModel, EmpresaModel)` usando `escpos-coffee` (4.1.0). Gera ESC/POS com cabeçalho da empresa, itens, totais, pagamento, rodapé e corte de papel. Saída vai para impressora térmica padrão do sistema ou arquivo `.bin` como fallback.
- **`PDVScreenViewModel`**: `finalizarVenda()` agora armazena `lastPedido` (PedidoModel). `imprimirNota()` implementado — busca itens via `PedidoItemService`, empresa via `EmpresaService`, cliente via `ClienteService` e delega ao `EscPosPrinter`.
- **`ComprovanteBuilder`**: interface mantida com `imprimir(VendaModel)`.
- Dependência `escpos-coffee` já declarada no `build.gradle.kts` (não adicionada).

### 2026-07-03: Correção de valores gigantes em subtotal/troco no PDV
- **`PDVScreenViewModel`**: subtotal e troco agora armazenam centavos inteiros via `Utils.deRealParaCentavos()`.
- **`Utils.deRealParaCentavos()`**: substituído `intValue()` por `setScale(0, HALF_UP).toBigInteger()` — elimina truncamento e overflow.
- **`FornecedorServiceTest`**: corrigido CNPJ no teste de duplicidade.

### 2026-07-03: Validação de CNPJ simplificada — suporte ao formato alfanumérico
- **`Utils.isValidCnpj()`**: removido cálculo de dígitos verificadores (módulo 11). Agora valida apenas formato: 14 caracteres (0-9, A-Z), últimos 2 obrigatoriamente dígitos.
- **`calcMod11()` e constantes de peso removidos** por obsolescência.
- **Arquivos `.md` de teste**: todos os 16 CNPJs corrigidos para dígitos verificadores válidos.

### 2026-07-02: Testes para tipo de pessoa (Física/Jurídica) em FornecedorScreen
- **Select "Tipo de pessoa"** adicionado em FornecedorScreen com `Data.tiposPessoaList` ("Física", "Jurídica").
- **ViewModel**: `tipoPessoaSelected` (State), `tipoPessoaEhFisica` (ComputedState). Valida CPF se física, CNPJ se jurídica. Só valida CPF/CNPJ quando campo preenchido (opcional).
- **Service**: `validar()` agora aceita CPF (11 dígitos) ou CNPJ (14 dígitos) — antes só aceitava CNPJ.
- **FornecedorServiceTest**: +3 testes (CPF válido, CPF inválido, CPF no update) — total 14 testes, 0 falhas.
- **FornecedorScreenViewModelTest**: reescrito com 8 testes (tipoPessoa inicial, ComputedState, física, jurídica, sem doc, nome vazio, clearForm).

### 2026-07-01: Correção de requestFocus() no input de quantidade (ComprasScreen)
- **`InputRef.requestFocus()`**: agora percorre os filhos do `StackPane` e foca o `TextField` interno diretamente, em vez de chamar `requestFocus()` no `StackPane` (que era ignorado por não ser focusable).
- **Causa raiz**: `InputBase` (megalodonte) envolve o `TextField` em um `StackPane`. `getJavaFxNode()` retorna o `StackPane`, que tem `focusTraversable = false` — o `requestFocus()` no `StackPane` não delega para o `TextField`.

### 2026-06-30: Correção de vazamento de Sessions SQLite
- **`DB.closeAllSessions()`** agora é chamado no shutdown (`Main.handleClose()`)
- **`reloadProdutos()`** em ComprasScreenVM e VendaMercadoriaScreenVM reusa service existente em vez de criar novo
- **`ClienteService` e `EmpresaService`** alinhados ao padrão `DB.getPersismSession()`
- **Import não utilizado removido** de `CategoriaModel.java`

### 2026-06-30: Correção Flyway + SQLite — migrations modificadas e inicialização duplicada
- **V1 e V7 restaurados ao original**: checksums agora correspondem ao primeiro commit. V20 e V21 convertem tipos para `REAL` em bancos existentes.
- **`flyway.repair()` adicionado** antes de `migrate()` no startup — recalcula checksums automaticamente para qualquer banco, eliminando `FlywayValidateException` em upgrades.
- **Flyway removido de `DB.getPersismSession()`**: agora é chamado uma única vez em `Main.initialize()`.
- **`CategoriaService` alinhado** ao padrão `DB.getPersismSession()`.
- **Migration V21** criada para corrigir tipos das colunas de `compras` em bancos existentes.

### 2026-07-04: Correção — validação inconsistente da licença de teste
- **`AuthScreenViewModel.entrar()`**: agora usa `isLicensaTesteExpirada()` em vez de `day > 11` — alinhado com `load()`.
- **`HomeScreenViewModel.isLicensaTesteExpirada()`**: `day > 11` → `day > 3` — mesmo threshold do AuthScreenVM.
- **Efeito**: licença de teste expira no dia 4 (validade até dia 3). Antes, o campo de licença era exibido mas o login ainda era permitido até dia 11.

### 2026-06-29: Produtos sem cadastro válido em testes de venda
- `testes-loja-de-roupas.md`: SKU004 do #18 alterado para SKU006 (libera SKU004). Adicionados #143 (Jaqueta SKU003) e #144 (Calça Jeans SKU004) como cadastros válidos.
- `testes-mercado.md`: Adicionada seção ProdutoScreen com #145 (Arroz 5kg), #146 (Feijão 1kg), #147 (Óleo 900ml).
- Demais perfis verificados — nenhum outro arquivo .md apresenta o mesmo problema.

### 2026-06-29: Clientes de perfil sem cadastro válido em testes-gerais.md
- `testes-gerais.md` ClienteScreen: adicionados 12 clientes (#12-#23)

### 2026-07-08: Tabela `cores` criada — cores vêm do banco em vez de lista fixa
- **Migration V24**: `CREATE TABLE cores` com as 16 cores pré-populadas
- **CorModel, CorRepository, CorService** criados seguindo padrão do projeto
- **ProdutoScreenViewModel**: `cores` carregado via `CorService`; `coresSelecionadas` mantém nomes selecionados
- **ProdutoScreen**: checkboxes agora iteram sobre `vm.cores` (do banco)
- **Data.java**: `listaCores` pode ser removida futuramente (não usada mais pela tela)
- **Arquivos criados**: `V24__criar_tabela_cores.sql`, `CorModel.java`, `CorRepository.java`, `CorService.java`
- **Arquivos alterados**: `ProdutoScreenViewModel.java`, `ProdutoScreen.java`

### 2026-07-08: Tabela de produtos não atualizava após CRUD
- **`handleClickMenuDelete()`**: adicionado `produtos.removeIf()` para remover item da lista após exclusão
- **`asyncAtualizar()`**: substituído recarregamento completo por `updateIf` com nova instância (padrão FornecedorScreenViewModel)
- **`loadInicial()`**: adicionado `this.produtos.clear()` antes de `addAll()` para evitar duplicação
- **Arquivo alterado**: `ProdutoScreenViewModel.java`

---

- **Correção do erro "For input string: 1797044400000" em produtos perecíveis**: A coluna `validade` da tabela `produtos` era `INTEGER`, mas Persism mapeia `INTEGER` do SQLite para `Integer` em Java. Como o valor armazenado é epoch millis (Long), o `Converter.convert()` do Persism tentava `Integer.parseInt("1797044400000")`, lançando `NumberFormatException`. Alterado para `REAL` (mesmo tipo usado por `vendas.data_validade` e `compras.data_validade`), que Persism mapeia para `Double` — compatível com `Long`. Adicionada migration V20 para converter tabelas existentes.
- **`fillModelFromForm()` no ProdutoScreenViewModel**: Só seta `validade` no model quando perecível é "Sim". Evita que data residual do DatePicker seja salva ao desmarcar "É perecível?".

## Screens refatoradas
- categoriaScreen, clienteScreen, comprasScreen, empresaScreen, fornecedorScreen
- homeScreen, pdvScreen, comprasAPagarScreen, contasAReceberScreen
- pedidosScreen, produtoScreen, vendaScreen
- preferenciasScreen, tecnicoScreen
- RelatarErroScreen, SugerirMelhoriaScreen, InfoUpdateScreen (ViewModel adicionadas)
- welcomeScreen (ViewModel criada)
- relatoriosScreen (nova, 2026-07-31)

## Funcionalidades
- **Excluir todos os dados**: botão destrutivo na PreferenciasScreen, apaga todas as 16 tabelas com confirmação e transação. Após exclusão, exibe popup modal sempre-no-topo com botão "Fechar aplicativo" que chama `Platform.exit()`.
- **Components.ShowPopupForced**: Stage modal (`APPLICATION_MODAL`) com `setAlwaysOnTop(true)` para mensagens que exigem ação do usuário antes de continuar.

## Documentação de testes
- `testes.md` criado na raiz do projeto com 104 casos de teste distribuídos por 14 telas
- 5 perfis de negócio simulados: Loja de Roupas, PetShop, Lanchonete, Açougue, Mercado
- Cada caso de teste inclui campo para registro de erro/inconsistência
- `testes-gerais.md` ClienteScreen: adicionados 12 clientes faltantes (#12-#23) que eram referenciados em testes de perfil mas não tinham cadastro válido definido (João Pedro, Carla Lima, Carlos Mendes, Sofia Rocha, Luana Costa, Thiago Santos, José Moura, Renata Oliveira, Paulo Sérgio, Fernanda Lima, Ricardo Gomes, Juliana Costa)
