# Contexto do Projeto

## Estrutura
- JavaFX + Megalodonte (UI framework)
- Persism (ORM) + SQLite
- Flyway (migrations)
- Padrão: Screen + ViewModel + Service + Repository
- Events movidos para core/events com EntityEvent<T> genérico

## Última alteração
- Eventos movidos de `my_app.events` para `my_app.core.events`
- Criado `EntityEvent<T>` genérico eliminando repetição de ClienteEvents, TecnicoEvents, ProdutoEvents
- Bug fix: ProdutoEvents.java importava TecnicoModel ao invés de ProdutoModel (arquivo deletado)
- EventBus e DadosFinanceirosAtualizadosEvent movidos para core/events
- 8 ViewModels atualizados com novos imports e usos do EntityEvent

## Screens refatoradas
- categoriaScreen, clienteScreen, comprasScreen, empresaScreen, fornecedorScreen
- homeScreen, pdvScreen, comprasAPagarScreen, contasAReceberScreen
- pedidosScreen, produtoScreen, vendaScreen
- preferenciasScreen, tecnicoScreen
