# Plics SW

![Ícone do Plics SW](logo_300x300_microsoft_store.png)

Sistema desktop para acompanhar vendas, produtos, clientes e resultados do negócio. A interface é construída com o framework Megalodonte e usa JavaFX como plataforma gráfica.

## Funcionalidades

- Painel com receitas, despesas, lucro e anúncios carregados.
- Cadastro de produtos e clientes, pedidos e ponto de venda.
- Relatórios e rotinas administrativas acessíveis pela navegação do aplicativo.

## Executar a partir do código-fonte

Requer JDK 25 e um ambiente gráfico. Na raiz deste repositório:

```sh
./gradlew run
```

Para verificar o projeto, execute `./gradlew test`. O empacotamento e suas dependências são definidos em [packaging.toml](packaging.toml) e nos [scripts](scripts/).

## Documentação

As opções de configuração e ferramentas usadas pelo aplicativo estão em [docs/CONFIGURATION_AUDIT.md](docs/CONFIGURATION_AUDIT.md). O histórico de decisões e o contexto de desenvolvimento ficam em [docs/DECISIONS.md](docs/DECISIONS.md) e [docs/CONTEXT.md](docs/CONTEXT.md).
