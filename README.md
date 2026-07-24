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

### Atualização automática

O Plics SW possui um sistema de atualização embutido. No menu "Suporte" > "Buscar atualização", o aplicativo:

1. Descobre o executável do updater (`Plics SW Updater.exe`) no mesmo diretório
2. Baixa o MSI da última release do GitHub
3. Lança o updater que mata os processos Java, executa o MSI e notifica o usuário

**Empacotamento com updater** (não altera os scripts originais):
```bash
python scripts/create-msi-with-updater.py   # Windows
python scripts/create-deb-with-updater.py   # Linux
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

Manifest e metadados ficam em `flatpak/`. Antes de submeter ao Flathub de verdade,
veja as ressalvas em `docs/DECISIONS.md` (2026-07-24) — tem pontos em aberto sobre
licença, permissão de acesso à home e ao dispositivo.

## Versão

**Versão:** 1.1.0  
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