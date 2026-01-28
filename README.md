# ERP Local v2

Um sistema desktop completo para gestão de pequenas e médias empresas, desenvolvido com JavaFX e Megalodonte Router.

## 🎯 Propósito

Sistema ERP completo para controle de:
- Cadastros (Produtos, Categorias, Clientes, Fornecedores)
- Compras com **controle inteligente de estoque**
- Gestão financeira básica
- Relatórios e visualizações

## 🚀 Características Principais

### 💼 **Gestão de Cadastros**
- Produtos com controle de estoque
- Categorias personalizáveis
- Clientes com informações completas
- Fornecedores com dados detalhados

### 📦 **Controle de Estoque Inteligente**
- **Controle por operação**: Opção de refletir no estoque individualmente
- **Visualização em tempo real**: Campos mostram estoque anterior e posterior
- **Validação automática**: Impede estoque negativo
- **Migração automática**: Atualiza bancos existentes

### 🛒 **Fluxo de Compras**
- Cadastro completo de compras
- Cálculo automático de totais
- Controle financeiro integrado
- Relatórios de compras por período

### 💻 **Tecnologia**
- **Java 17** com performance otimizada
- **JavaFX 17** para interface moderna e responsiva
- **SQLite** para banco local e offline
- **Megalodonte Router** para navegação limpa e centralizada

## 📁 Estrutura Modular

- **megalodonte-base**: Interfaces e utilitários
- **megalodonte-components**: Componentes UI reutilizáveis
- **megalodonte-reactivity**: Gerenciamento de estado
- **megalodonte-router**: Sistema de navegação
- **megalodonte-theme**: Padronização visual

## 🎨 Interface

- Moderna e intuitiva
- Layout responsivo para diferentes tamanhos de tela
- Componentes customizados e otimizados
- Navegação estruturada e contextual

## 📊 Relatórios

- Relatórios de compras e vendas
- Análise de estoque
- Relatórios financeiros
- Exportação para CSV/PDF

## 🔧 Instalação

### Pré-requisitos
- Java 17 ou superior
- Windows 10+ (compatível com Linux via adaptador)

### Build
```bash
./gradlew clean build
```

### Execução
```bash
./gradlew run
```

## 📋 Versão

**Versão:** 1.0.0  
**Status:** ✅ Estável para Produção

## 🎯 Benefícios

- **Offline-first**: Funciona sem conexão com internet
- **Desktop nativo**: Performance otimizada e integração com sistema operacional
- **Modular**: Fácil manutenção e evolução
- **Custo-benefício**: Reduz necessidade de sistemas ERP caros

## 📝 Melhorias Futuras

- [ ] Relatórios avançados
- [ ] Integração com NFe
- [ ] Módulo de vendas
- [ ] Backup e restauração automático
- [ ] Multi-empresa

## 👥 Suporte

**Para suporte e dúvidas:**
- Verifique a documentação interna
- Consulte os relatórios de sistema
- Analise logs de aplicação

---

**Desenvolvido com ❤️ usando tecnologias nacionais e internacionais**