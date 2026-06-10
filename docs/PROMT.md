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
ao tentar executar o script create-msi-with-updater.py dá erro:
PS C:\Users\Usuário\hidden\plics-sw> python .\scripts\create-msi-with-updater.py
Traceback (most recent call last):
File "C:\Users\Usuário\hidden\plics-sw\scripts\create-msi-with-updater.py", line 3, in <module>
from updater_config import *
ModuleNotFoundError: No module named 'updater_config'
PS C:\Users\Usuário\hidden\plics-sw> python .\scripts\create-msi-with-updater.py
Traceback (most recent call last):
File "C:\Users\Usuário\hidden\plics-sw\scripts\create-msi-with-updater.py", line 3, in <module>
from updater_config import *
ModuleNotFoundError: No module named 'updater_config'
PS C:\Users\Usuário\hidden\plics-sw> cd scripts\
PS C:\Users\Usuário\hidden\plics-sw\scripts> python create-msi-with-updater.py          
Traceback (most recent call last):
File "C:\Users\Usuário\hidden\plics-sw\scripts\create-msi-with-updater.py", line 3, in <module>
from updater_config import *
ModuleNotFoundError: No module named 'updater_config'
PS C:\Users\Usuário\hidden\plics-sw\scripts> 