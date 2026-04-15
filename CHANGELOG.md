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

### [2026-04-15] — LUIZ — Visão e Controle de Usuários
#### Arquivos criados:

- entidades/usuarios/ControleUsuario.java — Controlador de usuários com validação de email único (cadastro/atualização), autenticação (login), recuperação de senha, e lógica restrita de exclusão (bloqueia se o usuário tiver cursos ativos e remove cursos inativos em cascata). 
- entidades/usuarios/VisaoUsuario.java — Interface de linha de comando que gerencia a interação com o usuário. Contém o controle de sessão e os métodos de entrada/saída para as telas de cadastro, login, atualização, exclusão de conta e recuperação de senha. 

#### Arquivos modificados:
- entidades/usuarios/Usuario.java — Adicionado a função setHashSenha, para permitir a alteração da senha do usuário. 

### Pendências

#### ANDRÉ — Visão e Controle de Cursos

 - [ ] Criar entidades/cursos/VisaoCurso.java com métodos de entrada/saída de dados (ex: leCurso(), mostraCurso()).
 - [ ] Criar entidades/cursos/ControleCurso.java com menu "Meus Cursos" e lógica de inclusão, alteração, visualização e gerenciamento de estado.
 - [ ] Usar arqCurso.readAllOrdenadoPorNome(idUsuarioAtivo) para montar o menu de cursos em ordem alfabética.
 - [ ] Gerar código NanoID de 10 caracteres alfanuméricos ao criar curso novo.
 - [ ] Implementar mudanças de estado: encerrar inscrições (0→1), concluir (→2), cancelar/excluir (→3 ou delete).
 - [ ] Adicionar getters faltantes em Curso.java: getDescricao(), getDataInicio(), getCodigo(), getEstado(), setEstado(int).

### TODOS — Integração final

 - [ ] Criar Principal.java com tela de acesso (login / novo usuário) e menu principal (Meus dados / Meus cursos / Minhas inscrições).
 - [ ] Definir e alinhar a estrutura de diretórios final do projeto (ver [GUIA_ESTRUTURA.md](https://github.com/JeanCarlos0112/trabalho_aeds3/blob/main/GUIA_ESTRUTURA.md)).
 - [ ] Gravar vídeo de demonstração (até 3 minutos).
 - [ ] Preencher checklist do relatório no README.md.
