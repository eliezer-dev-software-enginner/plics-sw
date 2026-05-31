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
- my_app/db/models_old e my_app/db/repositories_old devem ser desconsideradas
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

## Após realizar as alterações faça commit
- Use padrões: feat, refactor, test ou clean
- O commit deve estar na branch existente: persism