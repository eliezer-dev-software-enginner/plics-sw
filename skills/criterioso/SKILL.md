---
name: criterioso
description: Investigar, implementar e validar mudanças de software de ponta a ponta, especialmente quando há múltiplos projetos, geradores, bibliotecas ou ambientes.
---

# Desenvolvimento criterioso

Use esta skill em tarefas de manutenção e evolução de software. Preserve o objetivo e as restrições do usuário; não transforme um exemplo de um produto em regra universal. Aplique as verificações pertinentes ao risco e à mudança, sem criar cerimônias desnecessárias.

Quando um pedido acumula correções e complementos, mantenha uma lista curta dos resultados exigidos e confira cada um antes de encerrar. Uma resposta anterior ou um commit não prova que a etapa atual foi concluída; não deixe requisitos implícitos, como a aplicação já aberta ou o artefato consumido, fora da validação.

Quando o usuário pedir uma quantidade de itens ou funcionalidades do TODO, escolha e conclua essa quantidade de itens distintos do TODO. Não apresente operações ou variações de um único componente como vários itens concluídos, a menos que o próprio TODO os trate assim. Se a implementação for parcial, mantenha o item aberto e informe o que falta; não encerre antecipadamente um pedido de continuidade enquanto houver trabalho autorizado viável.

## Uso eficiente de contexto

Quando o context-mode estiver disponível, use-o quando filtrar, resumir ou pesquisar saídas extensas realmente reduzir o conteúdo trazido ao contexto. Para resultados pequenos ou trechos exatos necessários à edição, prefira leitura direta e concisa, respeitando as instruções das ferramentas da sessão. Agrupe consultas relacionadas quando isso reduzir chamadas e resultados; não indexe nem invoque o plugin automaticamente para toda operação. Respeite as permissões, evite indexar segredos e não afirme economia de tokens sem evidência confiável. Se o plugin não estiver disponível, limite as saídas e processe dados localmente. A economia não reduz as verificações necessárias nem substitui a documentação.

## Investigar o alvo real

Antes de alterar código, identifique o arquivo, versão, processo e projeto que o usuário está usando. Confira o caminho aberto pela aplicação e, antes de commit ou push, `git rev-parse --show-toplevel` e o remoto. Compare cópias geradas, checkouts e dados locais; não conclua que uma mudança aparece na interface apenas porque existe no repositório que você editou. Não crie um clone nem substitua ou remova dados locais por suposição. Ao corrigir uma divergência, deixe explícita uma única fonte de verdade e preserve o que é próprio de cada ambiente.

Reproduza ou rastreie o cenário descrito, inclusive a sequência de cliques, entrada, estado salvo e saída. Busque a causa no caminho completo antes de modificar a primeira função suspeita. Quando o usuário informa que uma correção não funcionou, trate a observação como evidência nova: confira a versão efetivamente carregada, caches, artefatos publicados e outros pontos do mesmo fluxo antes de afirmar que o defeito persiste no código.

## Implementar no lugar certo

Em projetos com Java 25 ou posterior, o método de entrada `main` não precisa ser `public`; omita esse modificador quando não houver exigência externa de API ou compatibilidade. Omita também `String[] args` quando não for usado. Se os argumentos forem encaminhados ao framework ou necessários ao comportamento da aplicação, mantenha-os; em projetos com Java anterior, preserve as exigências da versão suportada.

Separe mecanismos genéricos de regras do produto. Um editor ou gerador não deve esconder comandos, respostas e decisões que pertencem ao projeto gerado. Alterar o gerador não atualiza automaticamente projetos existentes: ajuste o projeto em uso e o gerador quando ambos fazem parte do objetivo. Migre a lógica completa, com estado e testes relevantes, antes de remover o código antigo; verifique que o empacotamento inclui os arquivos e APIs necessários.

Procure usos equivalentes do comportamento defeituoso e corrija os afetados, respeitando diferenças de domínio. Prefira bibliotecas e componentes existentes, verificando a API real antes de usá-la. Ao mudar uma biblioteca consumida localmente, execute seus testes, publique em Maven Local quando esse for o mecanismo de consumo, e confira o artefato atualizado no projeto-alvo. Não rode novamente testes de outras aplicações sem pedido explícito do usuário. `includeBuild` e compilação da fonte não provam que o aplicativo instalado recebeu a mudança.

Para cores, estilos e capacidades de terminal em CLIs Python, prefira uma
biblioteca mantida e multiplataforma, como Rich, em vez de sequências ANSI
implementadas manualmente. Preserve saída limpa quando redirecionada e respeite
`NO_COLOR`. Em outras linguagens, escolha a integração equivalente sem confundir
fachada de logging com backend ou renderizador de terminal.

## Validar o resultado observável

Teste apenas o projeto alterado e o menor percurso que demonstra o requisito: entrada, ação, persistência, reabertura e resultado visível. Para prévia e exportação, confirme que ambas usam o mesmo estado. Para CLI, teste defaults, opções disponíveis e término de entrada multiline. Para integrações, valide o evento e o destino sem afirmar entrega quando só a chamada local foi testada. Respeite restrições do usuário, como não iniciar uma exportação demorada.

Uma compilação bem-sucedida é evidência de compilação, não de comportamento. Quando o teste real não for possível, declare exatamente o que foi validado e o que resta. Não diga “tudo aparece”, “está funcionando” ou “foi entregue” sem verificar a instância, o projeto e o resultado correspondentes. Se houver falha, explique a causa encontrada e continue o trabalho autorizado em vez de encerrar com uma correção parcial.

Confira se os testes relevantes realmente executaram: testes ignorados, abortados ou considerados atualizados não comprovam o comportamento. Quando a entrada do teste inclui arquivos externos que a ferramenta de build pode não rastrear, force a execução pertinente após modificá-los. Se faltar ambiente gráfico, preserve o teste e relate a limitação em vez de alegar validação visual; não crie capturas de tela sem pedido explícito.

## Documentação contínua do projeto

Em toda mudança de software, atualize proativamente a documentação pertinente no próprio projeto, sem esperar solicitação: registre o estado real, as decisões e trade-offs, a forma de uso, os próximos passos e quaisquer limitações em `docs/CONTEXT.md`, `docs/DECISIONS.md` e `docs/TODO.md` quando esses arquivos fizerem parte da convenção do projeto. Atualize também o README ou a documentação pública quando o uso mudar. Preserve o conteúdo existente, evite repetir informações sem necessidade e nunca descreva uma proposta ou implementação parcial como concluída.

Se o projeto mantiver uma estimativa de progresso, revise-a em cada iteração conforme os critérios documentados; não aumente o percentual só pela quantidade de código ou de operações adicionadas. Corrija também trechos antigos do TODO e do contexto que passem a contradizer o estado implementado.

## Tornar o recurso utilizável e entregar

### Commits locais automáticos

Depois de concluir e validar uma mudança de software autorizada, crie o commit
local correspondente sem esperar um pedido adicional do usuário. Use uma mensagem
que descreva o resultado final e inclua somente arquivos pertencentes à tarefa;
preserve alterações preexistentes ou alheias. Em trabalhos com vários repositórios,
faça um commit coerente em cada repositório alterado. Se ainda houver trabalho
autorizado em andamento, faça commits intermediários em marcos estáveis e continue.

O commit automático não autoriza `push`, publicação, release ou deploy. Execute
essas ações somente quando já estiverem autorizadas e respeite as regras locais
de horário, branch e aprovação.

Recursos com sintaxe ou sequência não óbvia precisam de um exemplo de uso no README do projeto em uso e, se forem reutilizáveis, no modelo gerado. Na interface, use texto ou tooltip junto ao controle para explicar a ação; mostre o caminho entre clique, gatilho e resposta. Operações longas devem expor progresso, sucesso e erro também nos logs relevantes. Revise documentos antigos que contradigam o comportamento novo.

Quando documentar uma correção por código, inclua o trecho completo necessário para aplicá-la, com imports e a sequência relevante de inicialização, não apenas a declaração isolada. Identifique classes que representam exemplos e preserve as configurações existentes; distinga exemplos ilustrativos daqueles que funcionam diretamente em outro modo ou plataforma.

### README público e documentação interna

Mantenha cada README coeso em um único idioma, sem alternar português e inglês no mesmo texto. Para bibliotecas, ferramentas e outros projetos voltados ao público, escreva o README principal em inglês; se houver versão bilíngue, apresente a versão completa em inglês primeiro e uma tradução equivalente em português, claramente separada e mantida em sincronia. Um aplicativo pessoal sem público externo pode ter README integralmente em português. Preserve nomes próprios, comandos e termos técnicos necessários. Siga uma escolha de idioma explicitamente solicitada pelo usuário para o projeto.

Trate o README como a entrada de quem encontrou **apenas aquele repositório**. Comece pelo nome, propósito e estado real do produto; mostre suas funções principais, requisitos, instalação, primeiro uso e links para a documentação detalhada. Prefira exemplos executáveis e caminhos relativos à raiz do próprio repositório ou variáveis de ambiente documentadas. Um caminho absoluto do computador do autor, um checkout irmão presumido, um token, um ID privado ou instruções dirigidas ao agente não pertencem ao README. Mova decisões, migrações, notas de manutenção e instruções específicas do workspace para `docs/`, `CONTRIBUTING.md` ou `AGENTS.md`, conforme o público.

Use identidade visual com medida: inclua logotipo, ícone ou capturas **reais e versionadas** quando existirem e ajudarem a reconhecer ou usar o produto; escreva texto alternativo e verifique os links relativos no GitHub. Não tire novas capturas de tela da aplicação, mesmo para documentação ou conferência visual, salvo pedido explícito do usuário. Use capturas já fornecidas ou versionadas quando forem úteis. Não use imagem gerada como se fosse captura, não exponha dados pessoais ou credenciais e não acrescente espaços reservados vazios. Bibliotecas e ferramentas de terminal podem ser melhor explicadas por exemplos curtos de entrada e saída ou diagramas; não precisam de screenshots artificiais. Se ainda não houver captura adequada, registre a pendência em documentação de manutenção sem fingir que ela existe.

Quando o usuário fornecer anexos que sejam úteis como referência de desenvolvimento ou documentação, copie os arquivos pertinentes para dentro do projeto, em uma pasta de documentação ou recursos com nomes descritivos, e faça links relativos no documento que os utiliza. Verifique se os arquivos copiados abrem e se não contêm segredos ou dados privados indevidos antes de versioná-los. Não deixe a única cópia em caminhos temporários, anexos da conversa ou diretórios particulares da máquina do autor.

Ao atualizar muitos repositórios, faça inventário antes: identifique o público, o comando de execução verdadeiro, as imagens rastreadas pelo Git, links quebrados e trechos dependentes do computador do autor. Melhore cada README conforme seu produto, sem copiar a mesma estrutura ou marketing para todos. Confira a renderização Markdown, os caminhos dos recursos e os comandos apresentados; quando um gerador cria projetos, corrija também seu modelo de README e teste a saída gerada.

Antes de concluir, revise o diff e o status de cada repositório alterado, rode apenas as verificações do projeto modificado e respeite as regras locais de commit e push. Commit, publicação local de biblioteca e push são ações distintas; não presuma autorização de uma a partir da outra. Relate o que mudou, a evidência da validação e qualquer limite material, sem apresentar uma intenção ou um teste indireto como resultado concluído.
