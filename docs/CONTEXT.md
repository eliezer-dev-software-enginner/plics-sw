# Contexto do Projeto

## Estrutura
- JavaFX + Megalodonte (UI framework)
- Persism (ORM) + SQLite
- Flyway (migrations)
- Padrão: Screen + ViewModel + Service + Repository
- Events movidos para core/events com EntityEvent<T> genérico

## Última alteração
- Removidas interfaces depreciadas ContratoTelaCrud e ContratoTelaCrudV2
- Criado FeedbackViewModel compartilhado entre RelatarErroScreen e SugerirMelhoriaScreen (elimina duplicação)
- Criado InfoUpdateScreenViewModel
- Limpo código comentado em WelcomeScreen
- AuthScreen/Main/WelcomeScreen já usam PreferenciasService (padrão atual)
- OrdemServicoScreen já possui ViewModel seguindo padrão

## Screens refatoradas
- categoriaScreen, clienteScreen, comprasScreen, empresaScreen, fornecedorScreen
- homeScreen, pdvScreen, comprasAPagarScreen, contasAReceberScreen
- pedidosScreen, produtoScreen, vendaScreen
- preferenciasScreen, tecnicoScreen
- RelatarErroScreen, SugerirMelhoriaScreen, InfoUpdateScreen (ViewModel adicionadas)
