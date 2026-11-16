<p align="center">

  <img src="docs/images/megalodonte-logo.png" width="180" alt="Megalodonte logo">
</p>


# plics-sw-new


Aplicação desktop Megalodonte com interface nativa. Este projeto inicial mostra uma tela nativa e serve como ponto de partida para a interface e as regras da aplicação.


## Executar


Requer JDK 25 e ambiente gráfico. Na raiz do repositório:


```sh
./gradlew test
./gradlew run
```


## Live reload


Para telas Java nativas, execute `megalodonte dev --project .` com o CLI e as
bibliotecas core/base atualizados (e reactivity, se usada). O build ocorre numa
área temporária. O primeiro envio ao mundo de compilação registra o manifesto do
projeto; os seguintes enviam deltas de arquivos, tokens e símbolos. Recursos são
armazenados por hash fora do JSON. Alterações em `.env` exigem reiniciar a sessão
e seu conteúdo não é gravado no protocolo. Mudanças compatíveis atualizam as janelas gerenciadas mantendo
o estado das telas. Erros de compilação conservam a versão em execução.
Mudanças estruturais (incluindo novos métodos `static`, campos, classes e assinaturas)
ou de dependências reiniciam automaticamente o processo da aplicação para aplicar a
geração compilada; nesse fallback, as janelas gerenciadas recriadas pelo startup
recuperam posição, tamanho, modo e intenção de foco, mas o estado em memória é
perdido. A ordem exata entre janelas de outros aplicativos depende do sistema
operacional e não é garantida pelo JavaFX. Use `--no-restart-on-incompatible` para manter a versão anterior ativa,
`--offline` para dependências já disponíveis no cache e Ctrl+C para
encerrar. 
Veja os [limites e exemplos do live reload](https://github.com/eliezer-dev-software-enginner/megalodonte-base/blob/main/docs/LIVE_RELOAD.md).


![Funcionamento do live reload](docs/images/live-reload.svg)


Há também um hospedeiro nativo experimental para workers Java que implementam
explicitamente `NativeWorker.Application`: `megalodonte dev --native-host
--worker-main seu.pacote.WorkerMain` (em uma única linha). Ele mantém as janelas
ao substituir o processo do worker. Não adapta automaticamente o `Main` gerado
ou os componentes existentes. Consulte a [API e as limitações](https://github.com/eliezer-dev-software-enginner/megalodonte-base/blob/main/docs/NATIVE_MESSAGING.md).
Esse protótipo é separado da mensageria incremental entre projeto e compilador.


## Ícone no Linux


No Linux, o ícone no dock pode depender do `WM_CLASS` da janela e da entrada `.desktop`, além do PNG da janela. Este projeto nativo já configura `MegalodonteApp.appName(...)`, `MegalodonteApp.appIcon("/icon.png")` e um `AppHost` exclusivo em `Main.java`. Substitua `src/main/resources/icon.png` pelo ícone desejado e mantenha o mesmo caminho em `packaging.toml`. Ao atualizar o ícone ou o host, reconstrua e reinicie a aplicação; para uma versão instalada, reconstrua e reinstale o pacote.


O padrão de inicialização é:


```java
import megalodonte.application.MegalodonteApp;
import megalodonte.application.MegalodonteApplication;
import megalodonte.base.theme.ThemeManager;


public final class Main {

    public static final class AppHost extends MegalodonteApplication {}



    static void main(String[] args) {

        ThemeManager.setTheme(new MyTheme());

        MegalodonteApp.appName("Minha aplicação");

        MegalodonteApp.appIcon("/icon.png");

        MegalodonteApp.run(AppHost.class, args, context -> {

            context.javafxStage().setMinWidth(980);

            context.javafxStage().setMinHeight(650);

            context.useView(new HomeScreen(), 1260, 780);

        });

    }
}
```


`MyTheme` e `HomeScreen` representam o tema e a tela do projeto; substitua-os pelas classes existentes. Mantenha qualquer outra inicialização necessária.


Em sessões X11, `xprop WM_CLASS` e um clique na janela mostram a classe real. Compare-a com `StartupWMClass` em `~/.local/share/applications/` e na entrada do aplicativo instalado. O framework cria uma entrada de desenvolvimento com o nome da aplicação e um arquivo de ícone cujo nome inclui um hash do conteúdo; confira se a entrada aponta para o PNG esperado. O dock pode aplicar regras próprias e o diagnóstico no Wayland pode ser diferente.


## Empacotar


`python3 scripts/package.py build appimage` cria um AppImage no Linux. O workflow de GitHub Actions gera AppImage, DEB e MSI em tags `v*` ou por execução manual; consulte [o workflow](.github/workflows/package.yml) para os requisitos de cada sistema.


## JavaFX: quando preciso instalar os módulos?


Para `./gradlew run` e `megalodonte dev`, não é necessário instalar um SDK
JavaFX manualmente. O plugin OpenJFX do Gradle resolve a versão e os artefatos
nativos da plataforma; o CLI monta o module path automaticamente no live reload.
O primeiro build requer acesso aos repositórios configurados, salvo quando as
dependências já estiverem no cache. `--offline` depende desse cache.


Para *construir* AppImage, DEB ou MSI com `jlink`/`jpackage`, forneça o SDK
JavaFX completo por `javafx.sdk` no `packaging.toml` ou por
`JAVAFX_MODULES_HOME` (não `JAVAFX_MODULES`). Quem apenas executa o aplicativo já
empacotado não precisa dessa variável: o runtime gerado já contém os módulos.


## Código


A tela inicial está em `src/main/`.


## Skills para agentes


Este projeto inclui [`skills/megalodonte-native-ui/SKILL.md`](skills/megalodonte-native-ui/SKILL.md)
para interface e [`skills/criterioso/SKILL.md`](skills/criterioso/SKILL.md)
para investigação e entrega de mudanças.
O `AGENTS.md` exige que agentes leiam as skills pertinentes; isso
funciona mesmo sem instalá-la no catálogo. Para usar `$` no Codex, execute na
raiz do projeto:


```sh
megalodonte skills install --project .
```


O comando cria vínculos para as duas skills em `~/.codex/skills` (ou
`$CODEX_HOME/skills`), sem duplicar os arquivos. Reinicie a conversa se o menu
de `$` não atualizar. Após atualizar o Megalodonte CLI, sincronize as versões
mais recentes com:


```sh
megalodonte skills sync --project .
```


O projeto mantém cópias versionadas; a instalação no Codex acompanha as
alterações dessas cópias automaticamente.
