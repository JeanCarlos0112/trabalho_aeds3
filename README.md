# TRABALHO PRÁTICO AEDS III

## PROPOSTA
A proposta é basicamente as informações previas que o professor passou pra gente estruturado em um markdown aqui no repositorio do github para facilitar o acesso a essas informações se precisar, vai estar tudo nesse arquivo a seguir:

[Proposta de trabalho do professor](https://github.com/JeanCarlos0112/trabalho_aeds3/blob/main/PROPOSTA.md])

## CHANGELOG (O QUE FOI FEITO):
### [2026-04-02] — JEAN — Relacionamento 1:N e índices
#### Arquivos criados:

- entidades/usuarios/ParEmailId.java — Implementação completa do par (email, id) para a Tabela Hash Extensível. Tamanho fixo de 54 bytes (50 para email + 4 para id). Permite busca de usuário por email no login.
- entidades/cursos/ArquivoCurso.java — CRUD de cursos com dois índices Árvore B+:

 - indiceUsuarioCurso (ParIdId) — Relacionamento 1:N entre usuários e cursos. Busca com ParIdId(idUsuario, -1) retorna todos os cursos do usuário.
 - indiceNomeCurso (ParNomeId) — Índice indireto por nome para listagem em ordem alfabética.
   
- Métodos adicionados: readAll(int idUsuario), readAllOrdenadoPorNome(int idUsuario), verificaUsuarioTemCursos(int idUsuario).
- Overrides de create, delete e update para manter os índices sincronizados automaticamente.

- TesteRelacionamento1N.java — Arquivo de teste com 20 verificações cobrindo: criação com vínculo, readAll, ordenação alfabética, verificaUsuarioTemCursos, exclusão com limpeza de índice, update com atualização de índice de nomes, exclusão total de cursos de um usuário e busca por email.
- GUIA_ESTRUTURA.md — Documento com a estrutura de diretórios recomendada para o projeto, explicação da implementação 1:N e instruções de integração para os demais membros.

#### Arquivos modificados:

- entidades/usuarios/ArquivoUsuario.java — Adicionados overrides de delete e update para manter o índice de email (ParEmailId) sincronizado em todas as operações CRUD. A versão anterior (do MIRO) só mantinha o índice no create e readEmail.

#### Arquivo removido:

- entidades/cursos/ParIdId.java — Arquivo vazio criado pelo MIRO como placeholder. Deletado porque o ParIdId completo já existe no pacote aed3/ (código do professor) e é esse que o ArquivoCurso utiliza. Manter os dois causa conflito de pacote.


### [2026-04-01] — MIRO — Entidades e CRUD base
#### Arquivos criados:

- aed3/* — Pacote do professor Kutova copiado integralmente (Arquivo.java, ArvoreBMais.java, HashExtensivel.java, interfaces, ParIDEndereco, ParIdId, ParNomeId).
- entidades/usuarios/Usuario.java — Entidade usuário com atributos: idUsuario, nome, email, hashSenha, perguntaSecreta, hashRespostaSecreta. Serialização via toByteArray/fromByteArray.
- entidades/usuarios/ArquivoUsuario.java — CRUD de usuários estendendo Arquivo genérico, com índice Hash Extensível por email (parcial, sem delete/update do índice).
- entidades/usuarios/ParEmailId.java — Arquivo stub (métodos vazios) para o **JEAN** implementar.
- entidades/cursos/Curso.java — Entidade curso com atributos: idCurso, idUsuario (FK), nome, descricao, dataInicio, codigo (NanoID), estado. Serialização implementada.
- entidades/cursos/ArquivoCurso.java — CRUD base de cursos estendendo Arquivo genérico (sem índices B+).
- entidades/cursos/ParIdId.java — Arquivo stub (vazio) para o **JEAN** implementar.

### Observações do MIRO:

- Código não testado por falta de acesso ao computador (viagem Semana Santa).
- O ArquivoUsuario dependia do ParEmailId para funcionar completamente.

### Pendências
#### LUIZ — Visão e Controle de Usuários

 - [ ] Criar entidades/usuarios/VisaoUsuario.java com métodos de entrada/saída de dados (ex: leUsuario(), mostraUsuario(), leLogin()).
 - [ ] Criar entidades/usuarios/ControleUsuario.java com menu "Meus Dados" e lógica de login, cadastro, alteração e exclusão.
 - [ ] No controle de exclusão de usuário: usar arqCurso.verificaUsuarioTemCursos(idUsuario) para impedir exclusão se houver cursos ativos. Se houver apenas cursos inativos (estado 2 ou 3), removê-los junto.
 - [ ] Implementar recuperação de senha via pergunta secreta.

#### ANDRÉ — Visão e Controle de Cursos

 - [ ] Criar entidades/cursos/VisaoCurso.java com métodos de entrada/saída de dados (ex: leCurso(), mostraCurso()).
 - [ ] Criar entidades/cursos/ControleCurso.java com menu "Meus Cursos" e lógica de inclusão, alteração, visualização e gerenciamento de estado.
 - [ ] Usar arqCurso.readAllOrdenadoPorNome(idUsuarioAtivo) para montar o menu de cursos em ordem alfabética.
 - [ ] Gerar código NanoID de 10 caracteres alfanuméricos ao criar curso novo.
 - [ ] Implementar mudanças de estado: encerrar inscrições (0→1), concluir (→2), cancelar/excluir (→3 ou delete).
 - [ ] Adicionar getters faltantes em Curso.java: getDescricao(), getDataInicio(), getCodigo(), getEstado(), setEstado(int).

### TODOS — Integração final

 - [ ] Criar Principal.java com tela de acesso (login / novo usuário) e menu principal (Meus dados / Meus cursos / Minhas inscrições).
 - [ ] Definir e alinhar a estrutura de diretórios final do projeto (ver [GUIA_ESTRUTURA.md]()).
 - [ ] Gravar vídeo de demonstração (até 3 minutos).
 - [ ] Preencher checklist do relatório no README.md.

## GUIA PARA A ESTRUTURA DE DIRETORIOS ATUAL, DO QUE FOI FEITO E DO QUE FAZER
O arquivo contempla uma especie de guia para a estrutura de pastas que **MIRO** criou e eu (**JEAN**) organizei para ficar mais facil de visualizar o projeto e fazer implementação e/ou ajustes adicionais, além disso no guia também tem cada alteração de forma detalhada de como eu (**JEAN**) fiz cada coisa e algumas dicas para as outras partes, é bom dar uma olhada pode ser um bom começo para vocês, o guia estará no arquivo abaixo:

[Guia para a estrutura de pastas do projeto]()

## To-Do List (O QUE DEVE SER FEITO):

- [ ] Implementar o CRUD de Usuários. (Responsabilidade: **MIRO**)
- [ ] Implementar o CRUD de Cursos, assegurando que cada curso pertença a um usuário específico. (Responsabilidade: **MIRO**)
- [ ] Implementar o relacionamento 1:N com o par (idUsuario; idCurso) usando a Árvore B+. (Responsabilidade: **JEAN**)
- [ ] Criar a visão e o controle de usuários. Assegurar que um usuário não possa ser excluído se algum curso ativo estiver vinculada a ele. Se não, os cursos inativos devem ser removidos também. (Responsabilidade: **LUIZ**)
- [ ] Criar a visão e o controle de cursos. Um novo curso deverá ser automaticamente vinculado ao usuário ativo no sistema. (Responsabilidade: **ANDRÉ**)
