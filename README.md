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