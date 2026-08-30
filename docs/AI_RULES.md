# Regras do Projeto

## Exclusão
- desconsidere o arquivo docs/PROMPT.md

## Linguagem
- Sempre usar Java moderno (25).
- SQLITE

## Código
- Não criar arquivos desnecessários.
- Não gerar comentários óbvios.
- Priorizar simplicidade.
- as Screens dentro de my_app/screens sempre devem possuir sua ViewModel correspondente.
- se a ViewModel correspondente ficar muito extensa, fragmente-a em uma service
- sempre faça teste da repositorie cuja Screen tiver sido refatorada
- alterações nas models devem refletir nas migrations dentro de /resources/flyway_migrations
- dataCriacao nas models deve ser do tipo localDateTime e id deve ser do tipo Integer
- erros do persism que não conseguir resolver consultar a api deles no site oficial: https://sproket.github.io/Persism/manual2.html
- para Objetos hierárquicos dentro da model pode-se utilizar a annotation @NotColumn

## Antes de qualquer alteração
- Ler este arquivo.
- Ler README.md.
- Analisar estrutura existente.
- Analisar arquivos de testes *.md (testes.md, testes-gerais.md, testes-*.md) por erros relatados e resolvê-los na sessão atual.
- Não substituir funcionalidades sem autorização.

## Versão do app (fonte única: gradle.properties)
- `Main.APP_VERSION` **não é mais hardcoded** — lê a system property `plics.appVersion` em runtime (`System.getProperty`), setada via `-Dplics.appVersion=...` tanto na task `run` do Gradle quanto em `scripts/config.py` (`run_jpackage()`, usado por todo build empacotado). Nunca edite `APP_VERSION` direto no `Main.java`.
- `gradle.properties` tem dois campos: `appVersion` (base, x.x.x — só muda em release nova) e `appPatch` (contador, incrementado a cada patch da mesma base). Versão final = `appVersion` sozinho se `appPatch=0`, senão `appVersion.appPatch` (ex: base `1.1.1` + patch `5` = `1.1.1.5`).
- Use `python scripts/bump_version.py patch` pra incrementar o patch, ou `python scripts/bump_version.py release X.Y.Z` pra definir uma versão base nova (zera o patch) — nunca edite `appVersion`/`appPatch` à mão.

## Ao alterar versão do app (etapas obrigatórias)
1. Ler todos os commits desde a última versão: `git log --since="DATA_ULTIMA_RELEASE" --reverse --format="%h %s"`
2. Ler `docs/DECISIONS.md` para contexto das decisões arquiteturais relevantes
3. Interpretar e agrupar commits em Feat, Fix, Refactor e Chore (notas concisas em português)
4. Adicionar nova entrada no topo de `src/main/resources/updates.json` com version e notes
5. Rodar `python scripts/bump_version.py patch` (correção) ou `python scripts/bump_version.py release X.Y.Z` (versão nova) — atualiza `gradle.properties`, e `Main.APP_VERSION`/`scripts/config.py` já leem o valor automaticamente, sem precisar editar mais nada
6. Atualizar versão em `README.md`: campo `Versão`

## O que NÃO entra no updates.json
- Envios automáticos de log/banco de dados pro Telegram (monitoramento de suporte)
- Mecanismos de verificação de acesso/licença
- Refatorações internas sem efeito visível pro usuário final
- Commits de teste/experimentação

## O que entra no updates.json (somente o que faz sentido ao software)
- Adicionar no `updates.json` apenas notas que façam sentido pro usuário final do software — ou
  seja, o que tem impacto visível/utilitário no dia a dia de uso (telas, fluxos, valores,
  comportamento).
- **Não** incluir informação técnica de desenvolvimento (renomeação de métodos/classes internos,
  troca de componente interno, refactor de layout de código, `RunnableThrowing`, `padding`/`maxWidth`,
  mudança de nomes `FormTitle`→`FormSubtitle`, etc.) — isso é interno e não interessa ao usuário.
- Agrupar e escrever como nota concisa em português, com o prefixo do tipo (Feat/Fix/Ui/Refactor),
  sempre pensando em "o que o usuário percebe/ganha" com aquela mudança.

## Ao executar testes (workaround para pipe closed / GradleWorkerMain)
- O path `C:\Users\Usuário` contém `ç` (caractere não-ASCII). O Gradle gera arquivos `@` classpath que corrompem esse caractere, causando `ClassNotFoundException: GradleWorkerMain`.
- **Workaround:** copiar o projeto para `C:\temp\plics` e rodar os testes de lá:
```powershell
# Limpar build anterior no destino
Remove-Item -Recurse -Force -LiteralPath "C:\temp\plics" -ErrorAction SilentlyContinue
# Copiar projeto (excluindo build .gradle)
Copy-Item -Recurse -Force -LiteralPath "C:\Users\Usuário\hidden\megalodonte-context\plics-sw" -Destination "C:\temp\plics" -Exclude @('build', '.gradle')
# Executar testes
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25.0.3"
$env:GRADLE_USER_HOME = "C:\temp\gradle"
$env:TMP = "C:\temp"
$env:TEMP = "C:\temp"
& "C:\temp\plics\gradlew.bat" -p "C:\temp\plics" test --tests "<TestClass>" --no-daemon
```

## Após realizar as alterações faça commit
- Use padrões: feat, refactor, test ou clean