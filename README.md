# Plics SW

Sistema desktop completo para gestão de pequenas e médias empresas, desenvolvido com JavaFX.

## Propósito

Sistema ERP completo para controle de:
- Cadastros (Produtos, Categorias, Clientes, Fornecedores)
- Compras com controle inteligente de estoque
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
- Validação automática: Impede estoque negativo
- Migração automática: Atualiza bancos existentes

### Fluxo de Compras
- Cadastro completo de compras
- Cálculo automático de totais
- Controle financeiro integrado
- Relatórios de compras por período

## Tecnologia

- Java 25 com performance otimizada
- JavaFX 25 para interface moderna e responsiva
- SQLite para banco local e offline
- Megalodonte Router para navegação limpa e centralizada

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
```bash
./gradlew clean build
```

### Execução
```bash
./gradlew run
```

### Teste de desempenho — 20 mil produtos

O projeto tem dois scripts Python que inserem e depois apagam **20 mil produtos no banco real**
(`%APPDATA%\plics-sw\erp.db` no Windows, `~/.plics-sw/erp.db` nos demais) pra você medir como o
app se comporta com muitos registros. Não passam pelo Gradle e não têm dependência externa (só
`sqlite3`).

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

O Plics SW não baixa/instala nem verifica versão sozinho. O menu "Suporte" > "Buscar
atualização" só abre `plics-sw-webpage.vercel.app/atualizacao?versao=X.X.X` no
navegador — a própria página busca a versão mais recente no GitHub e mostra a versão
atual vs a mais recente lado a lado, com os instaladores pra baixar.

**Subir versão**
```bash
python scripts/bump_version.py release 1.1.2
```

**Empacotamento:**
```bash
python scripts/create-msi.py         # Windows
python scripts/create-deb.py         # Linux
python scripts/create-msi-store.py   # Windows, build específico pra Microsoft Store
```

**Rodar em modo watch dog:
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

Dentro do Flatpak, o updater automático (menu Suporte > Buscar atualização) fica
desativado — quem atualiza é o próprio `flatpak update` (ver `Main.isFlatpak`).

Manifest e metadados ficam em `flatpak/`. Pra **publicar de verdade** no Flathub
(não só testar local), veja o passo a passo em [`flatpak/README.md`](flatpak/README.md)
— tem um bloqueio importante ali (a fonte do manifest hoje não é reproduzível, só
funciona pra build local) e as ressalvas sobre licença/permissões.

## Versão

**Versão:** 1.1.2  
**Status:** Estável para Produção

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
