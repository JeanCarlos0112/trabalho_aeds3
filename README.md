# TRABALHO PRÁTICO AEDS III

## PROPOSTA
A proposta é basicamente as informações previas que o professor passou pra gente estruturado em um markdown aqui no repositorio do github para facilitar o acesso a essas informações se precisar, vai estar tudo nesse arquivo a seguir:

[Proposta de trabalho do professor](https://github.com/JeanCarlos0112/trabalho_aeds3/blob/main/PROPOSTA.md)

## CHANGELOG (O QUE FOI FEITO):
As mudanças detalhadas e a checklist estará no arquivo ao final dessa seção, vou colocar um resumo do changelog só pra poder ficar de mais facil administração:

**02/04 — JEAN**: Criou ParEmailId, ArquivoCurso com B+, teste, guia e ajustou ArquivoUsuario.

**01/04 — MIRO**: Criou as entidades, criou CRUD base (Usuario e Curso) e organizou pacote aed3 (Codigos que o professor forneceu para usar como base no desenvolvimento).

**15/04 — LUIZ**: Criou a Visão e o Controle de usuários, implementando os menus de sessão (Logado/Deslogado), o sistema de recuperação de senha por pergunta secreta e a lógica de exclusão de conta com validação de cursos ativos.

[Arquivo de changelog detalhado](https://github.com/JeanCarlos0112/trabalho_aeds3/blob/main/CHANGELOG.md)

## GUIA PARA A ESTRUTURA DE DIRETORIOS ATUAL, DO QUE FOI FEITO E DO QUE FAZER
O arquivo contempla uma especie de guia para a estrutura de pastas que **MIRO** criou e eu (**JEAN**) organizei para ficar mais facil de visualizar o projeto e fazer implementação e/ou ajustes adicionais, além disso no guia também tem cada alteração de forma detalhada de como eu (**JEAN**) fiz cada coisa e algumas dicas para as outras partes, é bom dar uma olhada pode ser um bom começo para vocês, o guia estará no arquivo abaixo:

[Guia para a estrutura de pastas do projeto](https://github.com/JeanCarlos0112/trabalho_aeds3/blob/main/GUIA_ESTRUTURA.md)

## To-Do List (O QUE DEVE SER FEITO):

- [x] Implementar o CRUD de Usuários. (Responsabilidade: **MIRO**)
- [x] Implementar o CRUD de Cursos, assegurando que cada curso pertença a um usuário específico. (Responsabilidade: **MIRO**)
- [x] Implementar o relacionamento 1:N com o par (idUsuario; idCurso) usando a Árvore B+. (Responsabilidade: **JEAN**)
- [x] Criar a visão e o controle de usuários. Assegurar que um usuário não possa ser excluído se algum curso ativo estiver vinculada a ele. Se não, os cursos inativos devem ser removidos também. (Responsabilidade: **LUIZ**)
- [x] Criar a visão e o controle de cursos. Um novo curso deverá ser automaticamente vinculado ao usuário ativo no sistema. (Responsabilidade: **ANDRÉ**)
