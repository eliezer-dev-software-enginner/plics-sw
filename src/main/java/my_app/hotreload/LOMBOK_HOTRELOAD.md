# Análise: Por que Lombok causa erro no HotReload

## O problema

Ao usar `@Data`, `@Getter`, `@Setter` etc. do Lombok em models como `ProdutoModel`, o HotReload gera 1145+ erros do tipo:

```
error: cannot find symbol
  symbol:   method getEstoque()
  location: class ProdutoModel
```

## Causa raiz

O HotReload compila o código Java em runtime usando `javax.tools.JavaCompiler` (linha 216 de `HotReload.java`). A chamada atual:

```java
JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
// ...
int result = compiler.run(null, null, null, args.toArray(new String[0]));
```

**Não especifica o annotation processor path.** O `javac` do HotReload não encontra o Lombok e nunca invoca o annotation processor.

### Fluxo normal (Gradle)

```
ProdutoModel.java
    ↓
Lombok annotation processor roda durante a compilação
    ↓
Gera: getEstoque(), setEstoque(), getCodigoBarras(), setCodigoBarras(), etc.
    ↓
ProdutoModel.class com todos os métodos
```

### Fluxo HotReload (sem Lombok)

```
ProdutoModel.java
    ↓
javac roda SEM annotation processor
    ↓
NADA é gerado — só fields e anotações
    ↓
ProdutoModel.class SEM getEstoque(), getCodigoBarras(), etc.
    ↓
Todo arquivo que referencia esses métodos falha com "cannot find symbol"
```

## Por que são 1145+ erros

`ProdutoModel` éReferenciado por dezenas de arquivos (telas, services, repositories). Quando ele é compilado sem os getters/setters, **todos** esses arquivos falham — o erro se propaga em cascata.

## Solução

Adicionar o caminho do Lombok JAR ao `-processorpath` na chamada do `javac`:

```java
// No método compile() de HotReload.java:
args.add("-processorpath");
args.add(lombokJarPath);  // ex: "build/libs/compileOnly/lombok-1.18.38.jar"
```

Ou usar `--processor-module-path` se o Lombok estiver no module path.

### Alternativa sem Lombok

Remover o Lombok do projeto e escrever getters/setters manualmente — elimina a dependência do annotation processor mas aumenta a quantidade de código boilerplate.

## Arquivos envolvidos

- `src/main/java/my_app/hotreload/HotReload.java` — método `compile()` (linha 215)
- `build.gradle.kts` — dependência Lombok (linha 82-86)
- `src/main/java/my_app/db/models/ProdutoModel.java` — model de exemplo afetado

## Referência

- [Lombok and javac](https://projectlombok.org/setup/javac)
- [javax.tools.JavaCompiler API](https://docs.oracle.com/en/java/javase/21/docs/api/jdk.compiler/javax/tools/JavaCompiler.html)
