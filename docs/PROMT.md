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
Analise todos os campos que estão presentes nas screens, e veja se os testes manuais nos arquivos ".md" cobrem todos os casos de uso.Se necessário crie novas colunas nos testes manuais e novos casos de uso para cobrir os testes manuais.
Se encontrar testes incoerentes como por exemplo se em um lugar requer a existencia de cliente X mas esse cliente x não foi adicionado ao cadastro, corrija. 
E quero também que você também tira validações de crud das viewmodels e leve as validações para as services, e corrija testes automatizados que quebrarem. Também quero que você crie um método na ContratoTelaCrud que seja inverso ao populateFromModel(), que se chama populateModelFromFields() isso se dá pois atualmente não existe padronização para criar models nas ViewModels, cada viewmodel faz de um jeito. E ao final troque o nome de populateFromModel() para populateFieldsFromModel().
