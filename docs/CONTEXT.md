# Contexto do Projeto

## Estrutura
- JavaFX + Megalodonte (UI framework)
- Persism (ORM) + SQLite
- Flyway (migrations)
- Padrão: Screen + ViewModel + Service + Repository
- Events movidos para core/events com EntityEvent<T> genérico
- plics-sw-updater: subprojeto separado, empacotado junto com a aplicação (updater.jar)

## Scripts de empacotamento
- `scripts/config.py`: funções compartilhadas (`build_updater()`, `run_gradle()`, `run_jlink()`, `run_jpackage()`, etc.)
- `scripts/create-msi.py`: gera instalador Windows (.msi) com updater incluso + smoke test
- `scripts/create-deb.py`: gera instalador Linux (.deb) com updater incluso + smoke test

## Última alteração
- **PDV - NPE clienteId**: `PDVScreenViewModel.finalizarVenda()` agora usa o "CLIENTE PADRÃO" (id=1) quando nenhum cliente é selecionado em vendas à vista.
- **PDV - dataCriacao em itens**: `PDVService.finalizarVenda()` agora seta `dataCriacao` nos itens do pedido, corrigindo `NOT NULL constraint failed: pedido_itens.dataCriacao`.
- **PDV - outras correções**: `PedidoModel.clienteId` `Long`→`Integer`; `PDVService` ganhou construtor `Session`; `ContaAreceberService.gerarContasDeVenda()` seta `dataCriacao`; `PDVServiceTest` criado com 4 testes.

## Screens refatoradas
- categoriaScreen, clienteScreen, comprasScreen, empresaScreen, fornecedorScreen
- homeScreen, pdvScreen, comprasAPagarScreen, contasAReceberScreen
- pedidosScreen, produtoScreen, vendaScreen
- preferenciasScreen, tecnicoScreen
- RelatarErroScreen, SugerirMelhoriaScreen, InfoUpdateScreen (ViewModel adicionadas)
- welcomeScreen (ViewModel criada)
