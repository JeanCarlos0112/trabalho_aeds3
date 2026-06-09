### [2026-06-09] — LUIZ — TP3: Busca por palavras-chave (TFxIDF)
#### Arquivos criados:

- entidades/cursos/IndiceInvertidoCurso.java — Wrapper sobre `ListaInvertida` com a lógica TFxIDF. `inserir(idCurso, nome)` extrai termos, calcula TF como `ocorrências/total_termos_válidos`, insere os pares e incrementa o contador de entidades (N). `remover(idCurso, nome)` deleta os termos e decrementa N. `atualizar(idCurso, nomeAntigo, nomeNovo)` re-indexa sem mexer em N. `buscar(query)` aplica o modelo TFxIDF da especificação: para cada termo único da query, calcula `IDF = log10(N/df) + 1`, multiplica pelos TFs armazenados, soma scores por idCurso e ordena decrescente.
- TesteIndiceInvertido.java — 23 verificações em 6 blocos cobrindo: TermosUtil (tokenização, acentos, lowercase, stop words, edge cases null/vazio), cenário completo da spec (4 cursos, busca "Inteligencia Artificial" → ordem 1, 3, 2 com curso 4 ausente), sincronização em delete/update do nome, robustez da busca (stop words puras, termos inexistentes, case-insensitive, null), filtragem por estado, e bootstrap automático de índice vazio com cursos legados.

#### Arquivos modificados:

- entidades/cursos/VisaoCurso.java — Nova tela pública `telaBuscaPorPalavrasInscricao(idUsuarioLogado)`: pede a query ao usuário, exibe contagem total de resultados e relevância, paginação 10/página (item 10 → `(0)`), navegação A/B condicional, e selecionar pelo número abre o `telaDetalheCursoVisitante` já existente (reusa todo o fluxo de inscrição do TP2).
- entidades/inscricoes/VisaoInscricao.java — Case "B" do menu Minhas Inscrições deixou de ser placeholder e passou a chamar `visaoCurso.telaBuscaPorPalavrasInscricao(idUsuarioLogado)`.

#### Observações:

- O número de entidades N usado no IDF é mantido pela própria `ListaInvertida.numeroEntidades()` via `incrementa/decrementaEntidades()`.
- Stop words são gravadas já normalizadas (lowercase, sem acentos) para casarem com o resultado da `normalizar()` na comparação.

#### Testes (todos verdes):

- TesteRelacionamento1N: 23/23 (regressão TP1)
- TesteBuscaCursos: 23/23 (regressão TP2-busca)
- TesteInscricaoNN: 29/29 (regressão TP2-N:N)
- TesteIndiceInvertido: 23/23 (TP3 novo)
- **Total acumulado: 98/98 verificações automáticas.**

### [2026-06-09] — JEAN — TP3: Índice invertido
#### Arquivos criados:

- aed3/ListaInvertida.java — Lista invertida com dicionário + blocos encadeados, do código fornecido pelo Prof. Marcos Kutova (`kutova/AEDsIII/ListaInvertida`). Adicionado apenas o método `close()` para integrar com o ciclo de vida do projeto (todos os índices fecham em sequência no `ArquivoCurso.close()`).
- aed3/ElementoLista.java — Par `(idCurso, frequência)` armazenado em cada termo da lista invertida (também do prof, sem alterações).
- entidades/cursos/TermosUtil.java — Utilitário de processamento de termos com pipeline em três etapas: (1) tokenização por separadores não-letra/dígito, (2) normalização lowercase + remoção de acentos via `Normalizer.NFD` + remoção de combining marks, (3) filtragem de stop words em português (artigos, preposições, conjunções, pronomes, numerais por extenso, formas verbais auxiliares).

#### Arquivos modificados:

- entidades/cursos/ArquivoCurso.java — Adicionado o quarto índice `indiceInvertido` (instância de `IndiceInvertidoCurso`). Sincronizado em `create` (insere termos do nome novo), `delete` (remove termos do nome do curso excluído) e `update` (re-indexa apenas quando o nome muda — preserva o N porque é o mesmo curso). Bootstrap no construtor: se `numeroEntidades() == 0` e `readAllCursos()` retorna cursos, indexa todos automaticamente (cobre o caso de banco antigo do TP2 reaberto agora). Novo método público `readByPalavras(query)` que delega para o índice invertido e materializa os `Curso`. Fechamento sequencial em `close()`.
- entidades/cursos/ControleCurso.java — Método `buscarPorPalavras(String)` que chama `arqCurso.readByPalavras` e filtra para estado 0 (apenas cursos recebendo inscrições aparecem no menu de descoberta para inscrição).

#### Observações:

- **Validação contra os números da spec**: a especificação dá os scores esperados como `(1; 0.808), (3; 0.656), (2; 0.375)`. Minha implementação calcula `(1; 0.809), (3; 0.710), (2; 0.375)`. Os valores do Curso 1 e Curso 2 batem (diferença em 0.001 é arredondamento float). O valor do Curso 3 difere porque a spec tem um typo numérico: ela mesma documenta que TF(artificial,3) × IDF(artificial) deveria dar `0.260` (`0.2 × 1.301`) e em vez disso escreve `0.206` no texto. O cálculo correto pela fórmula que a própria spec define dá `0.710`. **A ordem final dos cursos é a mesma (1, 3, 2)** e o curso 4 sai do resultado nos dois cálculos.


#### Testes (todos verdes):

- TesteRelacionamento1N: 23/23 (regressão TP1)
- TesteBuscaCursos: 23/23 (regressão TP2-busca)
- TesteInscricaoNN: 29/29 (regressão TP2-N:N)
- TesteIndiceInvertido: 23/23 (TP3 novo)
- **Total acumulado: 98/98 verificações automáticas.**


### [2026-05-26] — JEAN — TP2 (fix): Data de início informada pelo usuário no cadastro do curso
#### Arquivos modificados:

- entidades/cursos/ControleCurso.java — Método `cadastrarCurso` agora recebe `LocalDate dataInicio` como parâmetro. Antes da correção, a data era atribuída automaticamente via `LocalDate.now()` no ato do cadastro, o que impedia o proponente de cadastrar hoje um curso que começaria em data futura (apontamento do professor na entrega do TP2).
- entidades/cursos/VisaoCurso.java — `telaNovoCurso` agora pede a data de início ao usuário no formato `dd/MM/yyyy`, com loop de validação que rejeita entradas mal formatadas e pede de novo até receber uma data válida. Adicionado `PARSER_DATA` (`d/M/yyyy`) tolerante a um ou dois dígitos no dia/mês (aceita `5/8/2026` e `05/08/2026`); o display continua usando `FMT_DATA` (`dd/MM/yyyy`) para alinhamento visual. `telaCorrecaoDados` (edição de curso existente) também passou a usar `PARSER_DATA` para consistência.
- TesteBuscaCursos.java — As 5 chamadas de `cadastrarCurso` foram atualizadas para passar datas explícitas. Adicionadas 3 verificações novas no Teste 1 confirmando que a data informada pelo usuário é persistida corretamente. O Teste 5 (ordenação por data de início) foi simplificado removendo a gambiarra antiga de `update` que existia justamente porque o cadastro forçava `LocalDate.now()`.
- TesteInscricaoNN.java — As 3 chamadas de `cadastrarCurso` foram atualizadas para passar datas explícitas.

#### Observações:

- Sem alteração na entidade Curso ou no formato em disco — a correção é puramente do construtor/visão.
- Bateria de testes subiu de 72 para 75: TesteRelacionamento1N (23/23), TesteBuscaCursos (23/23, +3 sobre persistência de data), TesteInscricaoNN (29/29).
- Validado E2E: entradas como "hoje", "01-08-2026", "2026/08/01" são rejeitadas; "01/08/2026" e "15/8/2026" são aceitas; o curso aparece na lista de Meus Cursos com a data correta formatada.


### [2026-05-19] — ANDRÉ — TP2: Visão dos inscritos nos seus cursos
#### Arquivos criados:

- entidades/inscricoes/VisaoInscricao.java (parte 1/2) — Tela `telaGerenciarInscritos(Curso)` acessada de Meus Cursos > curso > "(A) Gerenciar inscritos no curso". Lista numerada dos inscritos no curso com nome + data de inscrição, em ordem alfabética. Tela `telaDetalheInscrito(curso, ui)` aberta ao selecionar pelo número: exibe NOME, EMAIL, INSCRITO EM, e oferece botão "(A) Cancelar a inscricao deste aluno" com confirmação S/N. Método `exportarLista(curso)` gera arquivo CSV em `./exportacoes/inscritos_<codigo>.csv`.

#### Arquivos modificados:

- entidades/inscricoes/ControleInscricao.java (parte 1/3) — Métodos `listarInscritos(idCurso)` (retorna UsuarioComInscricao[], ordem alfabética por nome do aluno), `contarInscritos(idCurso)`, `exportarCSV(idCurso)` (cabeçalho Nome,Email,DataInscricao com escape RFC 4180 para vírgulas, aspas duplas e quebras de linha). Estrutura auxiliar `UsuarioComInscricao` para emparelhar objetos sem fazer lookups extras na View.
- entidades/cursos/VisaoCurso.java (parte 1/3) — `telaDetalheCurso` (visão do dono): o case "A" (Gerenciar inscritos no curso) agora delega para `visaoInscricao.telaGerenciarInscritos(curso)` em vez de mostrar placeholder. O case "E" (Cancelar curso) consulta `controleInscricao.contarInscritos(idCurso)` antes da confirmação e avisa o número de inscritos que serão atingidos pela cascata.

#### Observações:

- TesteInscricaoNN — seção 7: 4 verificações sobre CSV (cabeçalho correto, escape de vírgula em `"Dan, Junior"`, escape de aspas em `"dan@""x"""`, presença de ambos os inscritos).


### [2026-05-19] — LUIZ — TP2: Gerenciamento das próprias inscrições
#### Arquivos criados:

- entidades/inscricoes/VisaoInscricao.java (parte 2/2) — Método `menuMinhasInscricoes(idUsuarioLogado)`: ponto de entrada do menu Minhas Inscrições (acessado do menu logado via opção C). Lista no topo as inscrições atuais do usuário (numeradas), com sufixo de estado do curso quando aplicável: `(INSCRICOES ENCERRADAS)`, `(CURSO CONCLUIDO)`, `(CURSO CANCELADO)`. Oferece as três opções de busca (A/B/C) delegando para VisaoCurso. Digitar o número de uma inscrição abre `telaDetalheMinhaInscricao(idCursoUsuario, idUsuarioLogado)` que exibe CODIGO, CURSO, AUTOR, DESCRICAO, DATA DE INICIO, INSCRITO EM e oferece botão "(A) Cancelar minha inscricao no curso" com confirmação.

#### Arquivos modificados:

- entidades/inscricoes/ControleInscricao.java (parte 2/3) — Método `inscrever(idCurso, idUsuario)` com validação em ordem (curso existe, estado 0, dono não se inscreve no próprio, sem duplicação) e códigos de status (`OK_INSCRITO`, `ERRO_CURSO_INEXISTENTE`, `ERRO_CURSO_NAO_DISPONIVEL`, `ERRO_DONO_INSCREVENDO_NO_PROPRIO`, `ERRO_JA_INSCRITO`). Métodos `cancelarInscricao(idCursoUsuario)`, `cancelarInscricaoCursoUsuario(idCurso, idUsuario)`, `estaInscrito(idCurso, idUsuario)` e `listarMinhasInscricoes(idUsuario)` (retorna CursoComInscricao[] ordenado por data de início).
- entidades/cursos/VisaoCurso.java (parte 2/3) — `telaDetalheCursoVisitante` agora consulta `controleInscricao.estaInscrito` ao abrir e renderiza o botão correto: "(A) Fazer minha inscricao" se o curso está no estado 0 e o usuário não é o dono nem está inscrito; "(A) Cancelar minha inscricao" se o usuário já está inscrito; nenhum botão de ação se é o dono ou o curso não está mais aceitando inscrições. Trata todos os códigos de status retornados pelo `inscrever`.
- entidades/usuarios/VisaoUsuario.java — Construtor recebe `VisaoInscricao`. O case "C" do menu logado roteia para `visaoInscricao.menuMinhasInscricoes(usuarioLogado.getID())`.

#### Observações:

- TesteInscricaoNN — seções 1, 2 e 4: 11 verificações sobre inscrição básica, regras de negócio (curso inexistente, fora do estado 0, auto-inscrição, duplicação) e cancelamento individual.


### [2026-05-19] — JEAN — TP2: Relacionamento N:N (entidade CursoUsuario)
#### Arquivos criados:

- entidades/inscricoes/CursoUsuario.java — Entidade de associação do relacionamento N:N entre Curso e Usuário. Atributos: id, idCurso, idUsuario, dataInscricao. Serialização via DataOutputStream com data armazenada como long (epochDay), totalizando 20 bytes fixos.
- entidades/inscricoes/ArquivoCursoUsuario.java — CRUD da entidade com **duas Árvores B+** mantidas sincronizadas em create/delete/update: `indiceCursoInscricao` (ParIdId(idCurso, idCursoUsuario)) e `indiceUsuarioInscricao` (ParIdId(idUsuario, idCursoUsuario)). Métodos de consulta: `readByCurso`, `readByUsuario`, `existeInscricao`, `buscarIdInscricao`. Métodos de cascata: `deleteAllByCurso`, `deleteAllByUsuario`.
- TesteInscricaoNN.java — 29 verificações em 8 seções cobrindo CRUD, sincronização dos dois índices B+, regras de negócio, integridade referencial em cascata (cancelar curso → inscrições, excluir conta → inscrições), exportação CSV com escape RFC 4180, e consistência dos índices B+ após cascata.

#### Arquivos modificados:

- entidades/inscricoes/ControleInscricao.java (parte 3/3) — Esqueleto do controlador: construtor recebe ArquivoCursoUsuario + ArquivoCurso + ArquivoUsuario, e as estruturas auxiliares `CursoComInscricao` / `UsuarioComInscricao` usadas pelas visões.
- entidades/cursos/VisaoCurso.java (parte 3/3) — Construtor passa a receber ControleInscricao. Setter `setVisaoInscricao` para quebrar dependência circular com VisaoInscricao.
- entidades/cursos/ControleCurso.java — Recebe ArquivoCursoUsuario no construtor. `excluirCurso` agora cancela inscrições do curso em cascata antes de remover o registro (integridade referencial: nenhuma inscrição órfã apontando para curso inexistente).
- entidades/usuarios/ControleUsuario.java — Recebe ArquivoCursoUsuario no construtor. `excluirUsuario` agora cancela inscrições do usuário em cascata (duas direções: inscrições do usuário em cursos de terceiros + inscrições de terceiros nos cursos do usuário que serão deletados).
- Principal.java — Instancia ArquivoCursoUsuario, ControleInscricao e VisaoInscricao. Faz o wire dos setters bidirecionais entre VisaoCurso e VisaoInscricao. Fecha o novo arquivo no bloco finally.
- TesteBuscaCursos.java — Ajustado para o novo construtor de ControleCurso e fechamento do arqInscricao.

#### Observações:

- TesteRelacionamento1N: 23/23 (regressão TP1 intacta)
- TesteBuscaCursos: 20/20 (regressão TP2-busca intacta)
- TesteInscricaoNN: 29/29 (novo teste do N:N)
- Total acumulado: 72/72 verificações automáticas passando.

### [2026-05-12] — MIRO — TP2: Busca de Cursos (menu Minhas Inscrições)
#### Arquivos criados:

- entidades/cursos/ParCodigoId.java — Par (codigo, idCurso) de 14 bytes fixos (10 código + 4 id) para uso na Tabela Hash Extensível. Permite localizar um curso pelo NanoID compartilhável em O(1).
- TesteBuscaCursos.java — Arquivo de teste com 20 verificações cobrindo: geração de NanoID único com SecureRandom, busca por código (positiva, negativa, código vazio/null), listagem filtrada por estado 0, ordenação por data de início, resolução de autor e sincronização do índice de código após delete.

#### Arquivos modificados:

- entidades/cursos/ArquivoCurso.java — Adicionado o terceiro índice `indiceCodigoCurso` (Hash Extensível) sincronizado em create/delete/update. Novos métodos: `readByCodigo(String codigo)` para a busca por NanoID e `readAllCursos()` que varre o `dados.db` diretamente via RandomAccessFile próprio em modo read-only (não altera a base Arquivo do prof).
- entidades/cursos/ControleCurso.java — Acrescentada dependência de ArquivoUsuario para resolver autores. Novos métodos: `buscarPorCodigo`, `listarTodosCursosDisponiveis` (filtra estado 0 e ordena por data) e `buscarAutor`. Geração do NanoID trocada de `java.util.Random` para `java.security.SecureRandom` (padrão NanoID), com dedup contra colisão.
- entidades/cursos/VisaoCurso.java — Adicionado o menu "Minhas Inscrições" (menuInscricoes) com as 3 opções: (A) buscar por código, (B) palavras-chave [placeholder TP3], (C) listar todos. Telas: telaBuscaPorCodigo, telaListaCursos (paginação 10/página, item 10 → "(0)"), telaDetalheCursoVisitante (com campo AUTOR e botão de inscrição condicional ao estado 0 e não-dono).
- entidades/usuarios/VisaoUsuario.java — Placeholder "Minhas inscrições sera implementado no TP2" substituído pelo wire correto que chama `visaoCurso.menuInscricoes(usuarioLogado.getID())`.
- Principal.java — Construtor de ControleCurso atualizado para receber também `arqUsuario`, necessário para a resolução do autor na tela de detalhe de busca.

#### Observações:

- A efetivação da inscrição (botão "Fazer minha inscrição") fica como placeholder e será ligada ao Controle de Inscrição na próxima etapa do TP2 (relacionamento N:N + CRUD CursoUsuario).
- TesteRelacionamento1N continua 23/23 verde (regressão TP1 intacta).
- TesteBuscaCursos: 20/20 verde.

### [2026-04-15] — LUIZ — Visão e Controle de Usuários
#### Arquivos criados:

- entidades/usuarios/ControleUsuario.java — Controlador de usuários com validação de email único (cadastro/atualização), autenticação (login), recuperação de senha, e lógica restrita de exclusão (bloqueia se o usuário tiver cursos ativos e remove cursos inativos em cascata). 
- entidades/usuarios/VisaoUsuario.java — Interface de linha de comando que gerencia a interação com o usuário. Contém o controle de sessão e os métodos de entrada/saída para as telas de cadastro, login, atualização, exclusão de conta e recuperação de senha. 

#### Arquivos modificados:
- entidades/usuarios/Usuario.java — Adicionado a função setHashSenha, para permitir a alteração da senha do usuário. 

### [2026-04-15] — ANDRÉ — Visão e Controle de Cursos
#### Arquivos criados:
- entidades/cursos/ControleCurso.java — Controlador de cursos através da geração automática do NanoID de 10 caracteres no cadastro, vinculação do curso ao usuário logado e execução das operações de busca e exclusão através do arquivo de dados.
- entidades/cursos/VisaoCurso.java — Interface de linha de comando que gerencia a interação relacionada aos cursos. Contém o menu de opções para o estudante e os métodos de entrada/saída para as telas de cadastro, exclusão, atualização de status e a listagem ordenada (alfabética) dos cursos vinculados ao usuário.

#### Arquivos modificados:
- entidades/cursos/Curso.java — Atualizado para incluir os métodos de acesso (getters/setters) necessários para a manipulação de metadados, como a descrição, o estado do curso e o código NanoID.

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
