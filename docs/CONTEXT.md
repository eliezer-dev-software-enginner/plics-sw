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

## Última alteração
- **Validação de CPF/CNPJ duplicado**: ClienteService agora valida unicidade de CPF/CNPJ. Migration V18 adiciona UNIQUE INDEX condicional. ClienteRepository ganhou `buscarPorCpfCnpj()`. ClienteServiceTest com 4 novos testes (13 testes no total).

## Última alteração
- **Updater com saída graciosa**: updater agora chama `System.exit(0)` após lançar o batch script, liberando handles de DLLs antes do msiexec. Removido updater do `taskkill` no script.
- **App principal fecha após lançar updater**: adicionado `System.exit(0)` após `pb.start()` no `HomeScreenViewModel.update()`.
- **Updater implementado**: pacote `my_app.updater` (Main, HomeScreen, HomeScreenViewModel) adaptado do projeto `testes-atualizacao-app/app-v1`. `Main.java` ganhou `public static void main(String[] args)`. "Buscar atualização" descomentado no menu HomeScreen. Novos scripts de empacotamento com `--add-launcher`.

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
