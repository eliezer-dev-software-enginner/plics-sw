# Plics SW

Sistema desktop para gestão de pequenas e médias empresas, desenvolvido com JavaFX.

O Plics SW é gratuito para usar: não exige chave de ativação, licença de teste ou verificação remota de acesso. O login local, quando habilitado, protege os dados da instalação.

## Propósito

Aplicação desktop para controle de:
- Cadastros (Produtos, Categorias, Clientes, Fornecedores)
- Vendas e pedidos no ponto de venda (PDV)
- Estoque de produtos
- Gestão financeira básica
- Relatórios e visualizações

## Características Principais

### Gestão de Cadastros
- Produtos com controle de estoque
- Categorias personalizáveis
- Clientes com informações completas
- Fornecedores com dados detalhados

### Controle de Estoque Inteligente
- Controle por operação: Opção de refletir no estoque individualmente
- Visualização em tempo real: Campos mostram estoque anterior e posterior
- Alerta ao vender abaixo do estoque mínimo, com opção de confirmar a operação
- Migração automática: Atualiza bancos existentes

Os serviços de compras existem no código e alimentam indicadores financeiros, mas as rotas de cadastro de compras não estão registradas na navegação atual.

## Tecnologia

- Java 25 com performance otimizada
- JavaFX 25 para interface moderna e responsiva
- SQLite para banco local e offline
- Megalodonte Router para navegação limpa e centralizada
- Megalodonte JSONC UI para declarar as telas; o código Java mantém estado, validação e acesso a dados

### Interface orientada a dados

As telas e formulários ficam em [`src/main/resources/views`](src/main/resources/views). O renderer JSONC usa controles de `megalodonte-components`, sem WebView. Cadastros de produto, cliente e fornecedor, vendas, PDV, contas a receber, pedidos, relatórios e início usam esse contrato. Os respectivos `Screen` ligam estados e ações à lógica de domínio. Execute `./gradlew test` após mudar um contrato JSONC.

### AppImage local

O empacotamento é fornecido por `megalodonte-packaging`, configurado em [`packaging.toml`](packaging.toml). Com a biblioteca instalada (`python -m pip install -e /caminho/para/megalodonte-packaging`), execute na raiz deste projeto:

```bash
megalodonte-package --project . build appimage --run
```

O arquivo fica em `dist/`; a saída da execução fica em `dist/appimage-run.log`.

## Estrutura Modular

- megalodonte-base: Interfaces e utilitários
- megalodonte-components: Componentes UI reutilizáveis
- megalodonte-reactivity: Gerenciamento de estado
- megalodonte-router: Sistema de navegação

### Exportação orientada a dados

As tabelas das telas cadastrais podem ser baixadas em CSV e PDF. O formato é definido em templates
JSONC, incluindo cabeçalho empresarial, alinhamento, separadores e tabela; consulte
[`docs/EXPORT_TEMPLATES.md`](docs/EXPORT_TEMPLATES.md) para o contrato e a evolução planejada para
notas/recibos.

## Interface

Moderna, intuitiva e responsiva com navegação estruturada e componentes otimizados.

### Build

Requer JDK 25. O Gradle Wrapper está incluído. O build consulta `mavenLocal()` antes
do Maven Central; as bibliotecas `megalodonte:*`, `pack.utilities` e `disgust`
declaradas em [`build.gradle.kts`](build.gradle.kts) precisam estar disponíveis
nos repositórios configurados. O repositório não inclui o código dessas bibliotecas
nem instruções suficientes para instalá-las separadamente. Se o Gradle não resolver
uma delas, obtenha os artefatos correspondentes antes de continuar.

```bash
./gradlew clean build
```

### Execução

Além das dependências do build, a tarefa `run` exige a variável
`JAVAFX_MODULES_HOME` apontando para um diretório que contenha
`linux-25.0.1/lib` no Linux ou `windows-25.0.1/lib` no Windows. O script
`build.gradle.kts` seleciona essas pastas conforme o sistema operacional.
Configure a variável no seu ambiente antes de executar:

```bash
./gradlew run
```

### Teste de desempenho — 20 mil produtos

O projeto tem dois scripts Python que inserem e depois apagam **20 mil produtos no banco real**
(`%APPDATA%\plics-sw\erp.db` no Windows, `~/.plics-sw/erp.db` nos demais) pra você medir como o
app se comporta com muitos registros. Faça backup desse banco antes do teste: a criação
remove registros cujo código de barras começa com `PERFTEST`, e o script de limpeza
remove novamente todos os registros com esse prefixo. Abra o aplicativo uma vez para
criar ou migrar o banco antes de rodar os scripts. Eles não passam pelo Gradle e não
têm dependência externa (só `sqlite3` da biblioteca padrão do Python).

```powershell
# 1) Criar os 20 mil produtos (idempotente: apaga os "PERFTEST%" antigos antes de inserir)
python scripts/criar_produtos_teste.py

# 2) Testar o app com os 20 mil no banco
.\gradlew.bat run

# 3) Depois, apagar os produtos de teste
python scripts/apagar_produtos_teste.py
```

- Os scripts validam a contagem (20 mil após criar, zero após apagar) e imprimem o tempo gasto.
- Os produtos de teste têm `codigo_barras = PERFTEST...` — dá pra apagá-los de outro jeito (ex.:
  tela de Produtos), se preferir; o marcador garante identificação inequívoca.
- Os scripts gravam/apagam no mesmo formato do app (id sequencial, `dataCriacao` epoch millis,
  `total_liquido` preenchido) e avisam se o banco estiver numa migration atrás do código.
- `python scripts/criar_produtos_teste.py --total N` limita a quantidade (útil pra um teste rápido
  antes de fazer os 20 mil).

### Verificação de atualização

O Plics SW não baixa nem instala atualizações sozinho. O menu "Suporte" > "Buscar
atualização" abre `plics-sw-webpage.vercel.app/atualizacao?versao=X.X.X` no
navegador; a consulta e a comparação de versões ocorrem na página web.

**Subir versão**
```bash
python scripts/bump_version.py release 1.1.6
```

**Empacotamento:**
```bash
python scripts/create-msi.py         # Windows
python scripts/create-deb.py         # Linux
python scripts/create-msi-store.py   # Windows, build específico pra Microsoft Store
```

**Rodar em modo watch dog:**
```bash
pip install watchdog
python dev.py
```

### Flatpak (teste local)

Empacotamento experimental pra rodar/testar como Flatpak antes de considerar publicar
no Flathub (a loja "Software" do GNOME/Zorin instala a partir de lá, entre outras
fontes). Isso aqui só builda e instala **localmente** — publicar de verdade exige um
Pull Request manual em `github.com/flathub/flathub` e passar pela revisão deles.

Requer `flatpak` e `flatpak-builder` instalados, e os runtimes:
```bash
sudo apt install flatpak-builder
flatpak install flathub org.freedesktop.Platform//24.08 org.freedesktop.Sdk//24.08
```

Buildar e instalar:
```bash
python3 scripts/create-flatpak.py
```

Rodar:
```bash
flatpak run io.github.eliezerdevsoftwareenginner.PlicsSW
```

Desinstalar:
```bash
flatpak uninstall io.github.eliezerdevsoftwareenginner.PlicsSW
```

Dentro do Flatpak, a opção "Buscar atualização" fica desativada; as atualizações
são gerenciadas pelo Flatpak (ver `Main.isFlatpak`).

Manifest e metadados ficam em `flatpak/`. Pra **publicar de verdade** no Flathub
(não só testar local), veja o passo a passo em [`flatpak/README.md`](flatpak/README.md)
— tem um bloqueio importante ali (a fonte do manifest hoje não é reproduzível, só
funciona pra build local) e as ressalvas sobre metadados de distribuição e permissões.

## Configuração de execução

Os valores de integração são lidos do ambiente **na máquina que executa o aplicativo**, não embutidos pelo workflow de release. Defina-os somente quando a integração correspondente for desejada:

| Variável | Uso | Quando ausente |
| --- | --- | --- |
| `PLICS_TELEGRAM_BOT_TOKEN` e `PLICS_TELEGRAM_CHAT_ID` | Envio de notificações de suporte pelo Telegram | Envios desativados |
| `PLICS_SUPPORT_WHATSAPP` | Número de suporte no link `wa.me` | Número público padrão |
| `PLICS_WEBSITE_URL` | Base dos links do site oficial | Site público padrão |

O aplicativo conserva o comportamento de envio automático de log e banco local ao Telegram **somente quando** as duas variáveis do Telegram são fornecidas. Administre esse recurso de acordo com a política de privacidade e a autorização dos usuários. Não coloque tokens em `packaging.toml`, no workflow, em argumentos de build ou em arquivos versionados. Credenciais que já estiveram em commits antigos devem ser substituídas no provedor; removê-las do código atual não apaga o histórico Git.

O [levantamento de configuração](docs/CONFIGURATION_AUDIT.md) identifica as credenciais administrativas iniciais ainda embutidas, com a migração necessária para não quebrar instalações existentes nem expor valores em instaladores.

## Interface declarativa

As telas de boas-vindas, autenticação, relato de erro, sugestão de melhoria, logs e notas de atualização usam `megalodonte-jsonc-ui`. Cadastros, vendas, PDV, contas a receber, pedidos e relatórios também carregam recursos em `src/main/resources/views/`. Esses recursos declaram a estrutura visual; as classes Java conectam estado, ações, validação e serviços. Alguns detalhes e ações ainda são montados em Java.

As listas de fornecedores e clientes também declaram tabela, busca e ações em
`fornecedor-list.view.jsonc` e `cliente-list.view.jsonc`; seleção, detalhes e
exportação permanecem em Java.

Os gráficos da tela de relatórios usam `BarChartView` e `PieChartView` de
`megalodonte-components`, alimentados pelo ViewModel via `State`; o layout da tela
é carregado de `relatorios.view.jsonc`.

## Versão

**Versão:** 1.1.6

## Benefícios

- Offline-first: Funciona sem conexão com internet
- Desktop nativo: Performance otimizada e integração com sistema operacional
- Modular: Fácil manutenção e evolução
- Custo-benefício: Reduz necessidade de sistemas ERP caros

## Suporte

Para suporte e dúvidas:
- Verifique a documentação interna
- Consulte os relatórios de sistema
- Analise logs de aplicação

---

Desenvolvido com tecnologias nacionais e foco em simplicidade e performance.

## Licença do código

Não há arquivo de licença neste repositório. A gratuidade de uso descrita acima
não define permissão para copiar, modificar ou redistribuir o código; isso requer
esclarecimento dos responsáveis pelo projeto.
