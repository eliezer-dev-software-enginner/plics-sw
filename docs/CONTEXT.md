# Contexto do Projeto

## Estrutura
- JavaFX + Megalodonte (UI framework)
- Persism (ORM) + SQLite
- Flyway (migrations)
- Padrão: Screen + ViewModel + Service + Repository
- Events movidos para core/events com EntityEvent<T> genérico
- **Updater embutido no mesmo JAR** via `--add-launcher`: `my_app.updater.Main` (não é projeto separado)

## Fluxo de atualização
```
 Plics SW instalado (MSI ou DEB)
       │
       └─ Menu "Suporte" > "Buscar atualização"
              │
              ├─ [args.length >= 2] ── Usa updaterPath + pkgPath fornecidos via CLI
              │
              └─ [args.length < 2] ── Produção:
                     ├─ Descobre "Plics SW Updater" (.exe no Windows, sem ext. no Linux)
                     └─ GitHub API → baixa .msi (Windows) ou .deb (Linux)
              │
              ▼
       Lança updater <PID> <pkgPath>  →  System.exit(0)
              │
              ▼
       Aguarda PID morrer (onExit().join())
              │
              ▼
       ┌── Windows ── run-update.bat + cmd /c:
       │    taskkill (mata java.exe, javaw.exe, Plics SW.exe)
       │    timeout 10s
       │    msiexec /i <msi> /quiet (retry 3x)
       │    msg.exe notifica
       │
       └── Linux ── run-update.sh + bash:
            pkill -f "Plics SW"
            sleep 10
            pkexec dpkg -i <deb> (retry 3x)
            notify-send notifica
              │
              ▼
       System.exit(0) — saída graciosa
```

## Scripts de empacotamento
- `scripts/config.py`: funções compartilhadas (`run_gradle()`, `run_jlink()`, `run_jpackage()`, etc.)
- `scripts/create-msi.py`: gera instalador Windows (.msi) sem updater (original)
- `scripts/create-deb.py`: gera instalador Linux (.deb) sem updater (original)
- `scripts/updater_config.py`: constantes do updater (nome, main class, UUID)
- `scripts/create-msi-with-updater.py`: gera MSI com updater via `--add-launcher`
- `scripts/create-deb-with-updater.py`: gera DEB com updater via `--add-launcher`

## Últimas alterações

### 2026-06-29: Produtos sem cadastro válido em testes de venda
- `testes-loja-de-roupas.md`: SKU004 do #18 alterado para SKU006 (libera SKU004). Adicionados #143 (Jaqueta SKU003) e #144 (Calça Jeans SKU004) como cadastros válidos.
- `testes-mercado.md`: Adicionada seção ProdutoScreen com #145 (Arroz 5kg), #146 (Feijão 1kg), #147 (Óleo 900ml).
- Demais perfis verificados — nenhum outro arquivo .md apresenta o mesmo problema.

### 2026-06-29: Clientes de perfil sem cadastro válido em testes-gerais.md
- `testes-gerais.md` ClienteScreen: adicionados 12 clientes (#12-#23)

---

- **Correção do erro "For input string: 1797044400000" em produtos perecíveis**: A coluna `validade` da tabela `produtos` era `INTEGER`, mas Persism mapeia `INTEGER` do SQLite para `Integer` em Java. Como o valor armazenado é epoch millis (Long), o `Converter.convert()` do Persism tentava `Integer.parseInt("1797044400000")`, lançando `NumberFormatException`. Alterado para `REAL` (mesmo tipo usado por `vendas.data_validade` e `compras.data_validade`), que Persism mapeia para `Double` — compatível com `Long`. Adicionada migration V20 para converter tabelas existentes.
- **`fillModelFromForm()` no ProdutoScreenViewModel**: Só seta `validade` no model quando perecível é "Sim". Evita que data residual do DatePicker seja salva ao desmarcar "É perecível?".

## Screens refatoradas
- categoriaScreen, clienteScreen, comprasScreen, empresaScreen, fornecedorScreen
- homeScreen, pdvScreen, comprasAPagarScreen, contasAReceberScreen
- pedidosScreen, produtoScreen, vendaScreen
- preferenciasScreen, tecnicoScreen
- RelatarErroScreen, SugerirMelhoriaScreen, InfoUpdateScreen (ViewModel adicionadas)
- welcomeScreen (ViewModel criada)

## Funcionalidades
- **Excluir todos os dados**: botão destrutivo na PreferenciasScreen, apaga todas as 16 tabelas com confirmação e transação. Após exclusão, exibe popup modal sempre-no-topo com botão "Fechar aplicativo" que chama `Platform.exit()`.
- **Components.ShowPopupForced**: Stage modal (`APPLICATION_MODAL`) com `setAlwaysOnTop(true)` para mensagens que exigem ação do usuário antes de continuar.

## Documentação de testes
- `testes.md` criado na raiz do projeto com 104 casos de teste distribuídos por 14 telas
- 5 perfis de negócio simulados: Loja de Roupas, PetShop, Lanchonete, Açougue, Mercado
- Cada caso de teste inclui campo para registro de erro/inconsistência
- `testes-gerais.md` ClienteScreen: adicionados 12 clientes faltantes (#12-#23) que eram referenciados em testes de perfil mas não tinham cadastro válido definido (João Pedro, Carla Lima, Carlos Mendes, Sofia Rocha, Luana Costa, Thiago Santos, José Moura, Renata Oliveira, Paulo Sérgio, Fernanda Lima, Ricardo Gomes, Juliana Costa)
