Você é o agente oficial deste projeto.

Antes de executar qualquer tarefa:

1. Leia docs/AI_RULES.md.
2. Leia README.md.
3. Analise a estrutura atual do projeto.
4. Identifique padrões já utilizados.
5. Siga os padrões existentes.
6. Nunca introduza tecnologias diferentes sem autorização.
7. Sempre explique brevemente o plano antes de modificar arquivos.
8. Mantenha um histórico das decisões em docs/DECISIONS.md.
9. Ao finalizar uma tarefa, registre:
    - O que foi alterado.
    - Motivo da alteração.
    - Arquivos modificados.
    - Próximos passos recomendados.

Toda nova sessão deve consultar AI_RULES.md e DECISIONS.md antes de iniciar.

Leia docs/AI_RULES.md, docs/CONTEXT.md, docs/DECISIONS.md e docs/TODO.md.

Entenda o projeto antes de agir.

Após cada tarefa:
- Atualize docs/CONTEXT.md.
- Atualize docs/DECISIONS.md se houver decisão arquitetural.
- Atualize docs/TODO.md.
- Mantenha os arquivos concisos.

Prompt:
Estou enfrentando um problema com migrations utilizando **Flyway + SQLite**.

Analise toda a implementação relacionada ao banco de dados e identifique por que, ao lançar uma nova versão da aplicação contendo novas migrations, o Flyway lança uma exceção e a aplicação só volta a funcionar após apagar manualmente o banco de dados SQLite.

Quero que você investigue toda a arquitetura de migrations, incluindo:

* Como o Flyway está sendo inicializado.
* Em que momento `migrate()` é chamado.
* Como o banco SQLite é criado.
* Como é configurado o caminho do banco.
* Se existe alguma configuração incorreta do Flyway.
* Se alguma migration antiga foi alterada após ter sido publicada.
* Se existem problemas de checksum.
* Se há migrations fora de ordem ou com versões duplicadas.
* Se alguma migration está sendo removida ou renomeada.
* Se há scripts incompatíveis com SQLite.
* Se existe algum problema na tabela `flyway_schema_history`.
* Se o banco está sendo aberto antes da execução das migrations.
* Se existe alguma estratégia incorreta de versionamento.

Após identificar a causa, explique detalhadamente:

1. Qual é o problema.
2. Por que ele acontece.
3. Como corrigir definitivamente.
4. Quais arquivos precisam ser alterados.
5. Quais boas práticas estão sendo violadas.
6. Como deixar o projeto preparado para futuras versões, de forma que o usuário possa atualizar o aplicativo sem perder seus dados.

**Importante:** Não quero soluções paliativas como apagar o banco de dados, executar `clean()`, remover a tabela de histórico ou desabilitar validações do Flyway. Quero que seja encontrada a causa raiz e aplicada uma solução compatível com um ambiente de produção, preservando todos os dados dos usuários.

Caso seja necessário, percorra todo o projeto para localizar qualquer trecho de código que possa estar contribuindo para o problema e apresente um plano completo de correção.
