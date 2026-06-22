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
- Não substituir funcionalidades sem autorização.

## Ao alterar versão do app
- Atualizar em: `gradle.properties`, `src/main/java/my_app/Main.java`, `src/main/resources/updates.json`
- `scripts/config.py` lê `gradle.properties` automaticamente

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