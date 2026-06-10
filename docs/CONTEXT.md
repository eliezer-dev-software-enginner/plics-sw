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
 Plics SW instalado (MSI)
       │
       └─ Menu "Suporte" > "Buscar atualização"
              │
              ├─ [args.length >= 2] ── Usa updaterPath + msiPath fornecidos via CLI
              │
              └─ [args.length < 2] ── Produção:
                     ├─ Descobre "Plics SW Updater.exe" (mesmo diretório)
                     └─ GitHub API → baixa MSI da última release
              │
              ▼
       Lança Plics SW Updater.exe <PID> <msiPath>  →  System.exit(0)
              │
              ▼
       Aguarda PID morrer (onExit().join())
              │
              ▼
       Cria run-update.bat e lança cmd /c:
         taskkill (mata java.exe, javaw.exe, Plics SW.exe)
         timeout 10s
         msiexec /i <msi> /quiet (retry 3x c/ 10s se 1603)
         msg.exe notifica usuário do resultado
              │
              ▼
       System.exit(0) — saída graciosa (libera handles de DLLs)
```

## Scripts de empacotamento
- `scripts/config.py`: funções compartilhadas (`run_gradle()`, `run_jlink()`, `run_jpackage()`, etc.)
- `scripts/create-msi.py`: gera instalador Windows (.msi) sem updater (original)
- `scripts/create-deb.py`: gera instalador Linux (.deb) sem updater (original)
- `scripts/updater_config.py`: constantes do updater (nome, main class, UUID)
- `scripts/create-msi-with-updater.py`: gera MSI com updater via `--add-launcher`
- `scripts/create-deb-with-updater.py`: gera DEB com updater via `--add-launcher`

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
