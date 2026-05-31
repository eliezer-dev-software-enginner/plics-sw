# Contexto do Projeto

## Estrutura
- JavaFX + Megalodonte (UI framework)
- Persism (ORM) + SQLite
- Flyway (migrations)
- Padrão: Screen + ViewModel + Service + Repository

## Screens refatoradas
- categoriaScreen, clienteScreen, comprasScreen, empresaScreen, fornecedorScreen
- homeScreen, pdvScreen, comprasAPagarScreen, contasAReceberScreen
- pedidosScreen, produtoScreen, vendaScreen
- preferenciasScreen

## Última alteração
- Refatoração do PedidosScreen:
  - PedidoModel: @Column annotations, Integer id, LocalDateTime dataCriacao
  - PedidoItemModel: @Column annotations, Integer id, LocalDateTime dataCriacao
  - PedidoRepository: implementado com Persism (BaseRepository)
  - PedidoItemRepository: implementado com Persism (BaseRepository + listarPorPedido)
  - PedidoService: criado
  - PedidosScreenViewModel: migrado para new models/repos, getters
  - PedidosScreen: getters no lugar de field access direto
  - Migration V15: dataCriacao INTEGER -> TIMESTAMP
  - Testes de ambos os repositories criados e passando

## Screens refatoradas (lista completa)
- categoriaScreen, clienteScreen, comprasScreen, empresaScreen, fornecedorScreen
- homeScreen, pdvScreen, comprasAPagarScreen, contasAReceberScreen
- pedidosScreen, produtoScreen, vendaScreen
- preferenciasScreen, tecnicoScreen
