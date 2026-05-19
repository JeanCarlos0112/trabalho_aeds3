# README QUE USAMOS PARA ORGANIZAÇÃO DO PROJETO

## PROPOSTA
A proposta é basicamente as informações previas que o professor responsavel pela disciplina atual (Kutova) passou para gente estruturado em um markdown aqui no repositorio do github para facilitar o acesso a essas informações, se precisar, vai estar tudo nesse arquivo a seguir:

[Proposta de trabalho do professor](https://github.com/JeanCarlos0112/trabalho_aeds3/blob/main/PROPOSTA.md)

## CHANGELOG:
As mudanças detalhadas e a checklist estará no arquivo ao final dessa seção, irei colocar um resumo do changelog para poder ficar de mais facil visualização o que cada participante fez de forma resumida:

**12/05 — JEAN (TP2)**: Implementou a Busca de Cursos completa do menu Minhas Inscrições — busca por código NanoID com novo índice Hash Extensível (ParCodigoId), listagem paginada de cursos disponíveis (10 por página, ordenada por data de início) e tela de detalhe do visitante com resolução de autor. Adicionou também o TesteBuscaCursos com 20 verificações. Falta no TP2 apenas o relacionamento N:N (entidade CursoUsuario) e a efetivação da inscrição.

**15/04 — ANDRÉ**: Criou a Visão e o Controle de cursos, implementando a listagem ordenada alfabeticamente, o sistema de geração automática de códigos NanoID e a lógica de persistência vinculada ao ID do usuário logado.

**15/04 — LUIZ**: Criou a Visão e o Controle de usuários, implementando os menus de sessão (Logado/Deslogado), o sistema de recuperação de senha por pergunta secreta e a lógica de exclusão de conta com validação de cursos ativos.

**02/04 — JEAN**: Criou ParEmailId, ArquivoCurso com B+, teste, guia e ajustou ArquivoUsuario.

**01/04 — MIRO**: Criou as entidades, criou CRUD base (Usuario e Curso) e organizou pacote aed3 (Codigos que o professor forneceu para usar como base no desenvolvimento).

[Arquivo de changelog detalhado](https://github.com/JeanCarlos0112/trabalho_aeds3/blob/main/CHANGELOG.md)

## GUIA PARA A ESTRUTURA DE DIRETORIOS ATUAL
O arquivo contempla uma especie de guia para a estrutura de pastas que o **MIRO** criou e o **JEAN** organizou para ficar mais facil de visualizar o projeto e fazer implementações futuras e/ou ajustes adicionais, além disso no guia também tem cada alteração de forma detalhada de como cada participante fez cada coisa:

[Guia para a estrutura de pastas do projeto](https://github.com/JeanCarlos0112/trabalho_aeds3/blob/main/GUIA_ESTRUTURA.md)

## To-Do List:

- [x] Implementar o CRUD de Usuários. (Responsabilidade: **MIRO**)
- [x] Implementar o CRUD de Cursos, assegurando que cada curso pertença a um usuário específico. (Responsabilidade: **MIRO**)
- [x] Implementar o relacionamento 1:N com o par (idUsuario; idCurso) usando a Árvore B+. (Responsabilidade: **JEAN**)
- [x] Criar a visão e o controle de usuários. Assegurar que um usuário não possa ser excluído se algum curso ativo estiver vinculada a ele. Se não, os cursos inativos devem ser removidos também. (Responsabilidade: **LUIZ**)
- [x] Criar a visão e o controle de cursos. Um novo curso deverá ser automaticamente vinculado ao usuário ativo no sistema. (Responsabilidade: **ANDRÉ**)
- [x] Criar Principal.java com tela de acesso (login / novo usuário) e menu principal (Meus dados / Meus cursos / Minhas inscrições).
- [x] Definir e alinhar a estrutura de diretórios final do projeto (ver [GUIA_ESTRUTURA.md](https://github.com/JeanCarlos0112/trabalho_aeds3/blob/main/GUIA_ESTRUTURA.md)).
- [x] Gravar vídeo de demonstração (até 3 minutos).
- [x] Preencher checklist do relatório no README.md.

### TP2 — em andamento:

- [x] Implementar a busca de cursos por código NanoID (índice Hash Extensível indiceCodigoCurso). (Responsabilidade: **JEAN**)
- [x] Implementar a lista paginada de todos os cursos disponíveis (10 por página, ordenada por data de início). (Responsabilidade: **JEAN**)
- [x] Implementar o menu Minhas Inscrições com as opções de busca. (Responsabilidade: **JEAN**)
- [ ] Implementar o relacionamento N:N entre Cursos e Usuários usando a entidade de associação CursoUsuario e duas Árvores B+.
- [ ] Implementar o CRUD de Inscrições (entidade CursoUsuario).
- [ ] Criar a visão e o controle de inscrições, incluindo a efetivação do botão "Fazer minha inscrição".
- [ ] Implementar a gestão de inscritos pelo proponente do curso (menu Meus Cursos → Gerenciar inscritos), com exportação em CSV.
- [ ] Implementar o cancelamento de inscrição pelo aluno.
- [ ] Assegurar a integridade de dados entre todas as entidades nas operações de cancelamento de curso e exclusão de conta.
- [ ] Gravar novo vídeo de demonstração do TP2 (até 3 minutos).
- [ ] Preencher checklist do relatório do TP2 no README.md.
- [ ] Busca por palavras-chave (reservado para o TP3).
