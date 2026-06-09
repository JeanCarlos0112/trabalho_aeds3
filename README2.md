# README QUE USAMOS PARA ORGANIZAÇÃO DO PROJETO

## PROPOSTA
A proposta é basicamente as informações previas que o professor responsavel pela disciplina atual (Kutova) passou para gente estruturado em um markdown aqui no repositorio do github para facilitar o acesso a essas informações, se precisar, vai estar tudo nesse arquivo a seguir:

[Proposta de trabalho do professor](https://github.com/JeanCarlos0112/trabalho_aeds3/blob/main/PROPOSTA.md)

## CHANGELOG:
As mudanças detalhadas e a checklist estará no arquivo ao final dessa seção, irei colocar um resumo do changelog para poder ficar de mais facil visualização o que cada participante fez de forma resumida:

**09/06 — LUIZ (TP3)**: Implementou wrapper com cálculo TFxIDF (TF na inserção, IDF on-the-fly via `log10(N/df)+1` na busca), sincronização do índice em `ArquivoCurso.create/delete/update` e bootstrap automático para cursos legados do TP2. `ControleCurso.buscarPorPalavras` filtrando estado 0. `VisaoCurso.telaBuscaPorPalavrasInscricao` com paginação 10/página. `VisaoInscricao` case "B" agora chama a busca real. 

**09/06 — JEAN + ANDRÉ (TP3)**: Implementou o índice invertido completo do TP3 — `aed3/ListaInvertida` e `aed3/ElementoLista` do código fornecido pelo prof. (com adição apenas do `close()`), `entidades/cursos/TermosUtil` com o pipeline de tokenização + normalização (lowercase + sem acentos via `Normalizer.NFD`) + filtragem de stop words em português, `entidades/cursos/IndiceInvertidoCurso`. `TesteIndiceInvertido` com 23 verificações cobrindo o cenário completo da spec.

**09/06 — JEAN (TP2 fix)**: Corrigiu o apontamento do professor — a data de início do curso agora é informada pelo usuário em `dd/MM/yyyy` em vez de ser preenchida automaticamente com `LocalDate.now()`. `VisaoCurso.telaNovoCurso` ganhou loop de validação de formato com parser tolerante (aceita `5/8/2026` e `05/08/2026`); `telaCorrecaoDados` recebeu o mesmo parser por consistência. Bateria subiu para 75 testes (+3 verificações sobre persistência da data).

**19/05 — JEAN + LUIZ + ANDRÉ (TP2)**: Implementou o relacionamento N:N completo do TP2 — entidade de associação `CursoUsuario`, CRUD com duas Árvores B+ sincronizadas (`indiceCursoInscricao` e `indiceUsuarioInscricao`), `ControleInscricao` com regras de negócio (curso inexistente, fora do estado 0, auto-inscrição, inscrição dupla), `VisaoInscricao` com quatro telas (menu Minhas Inscrições com lista das atuais, detalhe da inscrição com cancelar, gerenciar inscritos do dono, detalhe do inscrito com cancelar). Integridade referencial em cascata: cancelar curso cancela inscrições, excluir conta cancela inscrições em ambas as direções. Exportação CSV dos inscritos com escape RFC 4180. `TesteInscricaoNN` com 29 verificações.

**12/05 — MIRO (TP2)**: Implementou a Busca de Cursos completa do menu Minhas Inscrições — busca por código NanoID com novo índice Hash Extensível (ParCodigoId), listagem paginada de cursos disponíveis (10 por página, ordenada por data de início) e tela de detalhe do visitante com resolução de autor. Adicionou também o TesteBuscaCursos com 20 verificações.

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

### TP2 — completo:

#### Busca de cursos
- [x] Implementar a busca de cursos por código NanoID (índice Hash Extensível indiceCodigoCurso). (Responsabilidade: **JEAN**)
- [x] Implementar a lista paginada de todos os cursos disponíveis (10 por página, ordenada por data de início). (Responsabilidade: **JEAN**)
- [x] Implementar o menu Minhas Inscrições com as três opções de busca (código, palavras-chave, listar todos). (Responsabilidade: **JEAN**)

#### Relacionamento N:N
- [x] Implementar o relacionamento N:N entre Cursos e Usuários usando a entidade de associação CursoUsuario e duas Árvores B+. (Responsabilidade: **JEAN**)
- [x] Implementar o CRUD de Inscrições (entidade CursoUsuario). (Responsabilidade: **JEAN**)
- [x] Assegurar a integridade de dados entre todas as entidades nas operações de cancelamento de curso e exclusão de conta. (Responsabilidade: **JEAN**)

#### Gerenciamento das próprias inscrições
- [x] Criar a visão e o controle de inscrições, incluindo a efetivação do botão "Fazer minha inscrição". (Responsabilidade: **JEAN**)
- [x] Implementar a listagem das inscrições atuais do usuário no topo do menu Minhas Inscrições, com tag de estado do curso. (Responsabilidade: **JEAN**)
- [x] Implementar o cancelamento de inscrição pelo aluno (tela de detalhe da inscrição). (Responsabilidade: **JEAN**)

#### Visão dos inscritos nos seus cursos
- [x] Implementar a gestão de inscritos pelo proponente do curso (menu Meus Cursos → Gerenciar inscritos). (Responsabilidade: **JEAN**)
- [x] Implementar a exportação da lista de inscritos em formato CSV com escape RFC 4180. (Responsabilidade: **JEAN**)
- [x] Implementar o cancelamento individual de inscrição pelo dono do curso (tela de detalhe do inscrito). (Responsabilidade: **JEAN**)

#### Documentação e fechamento
- [x] Preencher checklist do relatório do TP2 no README.md. (Responsabilidade: **JEAN**)
- [ ] Gravar novo vídeo de demonstração do TP2 (até 3 minutos).


### TP3 — completo:

- [x] Implementar o índice invertido para os cursos usando as palavras dos nomes dos cursos, com a classe `ListaInvertida` do prof. (Responsabilidade: **JEAN**)
- [x] Implementar a busca de cursos por palavras oferecendo respostas ordenadas pelo valor TFxIDF. (Responsabilidade: **JEAN**)
- [x] Integrar a busca por palavras no menu Minhas Inscrições (opção B) — substituiu o placeholder do TP2. (Responsabilidade: **JEAN**)
- [x] Implementar a sincronização do índice em create/delete/update de cursos. (Responsabilidade: **JEAN**)
- [x] Implementar bootstrap automático do índice para cursos legados do TP2. (Responsabilidade: **JEAN**)
- [x] Implementar o pipeline de processamento de termos (tokenização + normalização + stop words PT). (Responsabilidade: **JEAN**)
- [x] Criar `TesteIndiceInvertido` reproduzindo o cenário da spec. (Responsabilidade: **JEAN**)
- [x] Preencher checklist do relatório do TP3 no README.md. (Responsabilidade: **JEAN**)
- [ ] Gravar vídeo de demonstração do TP3 (até 3 minutos).
