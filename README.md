# TRABALHO PRÁTICO AEDs III - GRUPO 12

## PARTICIPANTES
- Jean Carlos Lopes Lellis
- Miro Brito de Area Leao
- Luiz Fernando Santos Langa
- André Luiz Baptista Esteves Bassini 

## DESCRIÇÃO DO SISTEMA
O **G12 TP1 1.2** é um sistema textual em Java para gestão de cursos livres entre alunos da PUC Minas. Cada usuário cadastrado pode ofertar seus próprios cursos e inscrever-se nos cursos de outros usuários. O acesso é feito por email e senha, e toda a persistência é baseada em arquivos binários, com o CRUD genérico Arquivo do pacote aed3 como base, estendido por índices em Hash Extensível, Árvore B+ e Lista Invertida. O sistema implementa todos os relacionamentos previstos no TP1 (1:N entre usuário e seus cursos), no TP2 (N:N entre cursos e inscritos, via entidade de associação `CursoUsuario`) e a busca por palavras-chave do TP3 (índice invertido sobre os nomes dos cursos, com ordenação pelo modelo TFxIDF).

### Arquitetura (MVC)
A classe de entrada é Principal, que abre os arquivos, instancia os controles/visões e centraliza o fechamento seguro dos arquivos ao sair. O projeto segue o padrão MVC separando dados, lógica e apresentação, com três pacotes de entidade:

- Modelo (dados): Usuario, Curso, CursoUsuario, ArquivoUsuario, ArquivoCurso, ArquivoCursoUsuario, ParEmailId, ParCodigoId, além das classes do pacote aed3 (Arquivo, HashExtensivel, ArvoreBMais, ParIdId, ParNomeId).
- Controle (lógica): ControleUsuario, ControleCurso e ControleInscricao.
- Visão (interface textual): VisaoUsuario, VisaoCurso e VisaoInscricao.

### Entidades
- Usuario: id, nome, email, hashSenha, perguntaSecreta e hashRespostaSecreta. A senha nunca é armazenada em claro; o mesmo vale para a resposta secreta, usada na recuperação de senha.
- Curso: id, idUsuario (chave estrangeira), nome, descrição, data de início, código compartilhável NanoID (10 caracteres alfanuméricos gerado automaticamente) e estado (0: ativo recebendo inscrições, 1: ativo com inscrições encerradas, 2: concluído, 3: cancelado).
- CursoUsuario: id, idCurso (FK), idUsuario (FK), dataInscricao. Entidade de associação do relacionamento N:N — cada registro representa uma inscrição de um usuário em um curso.

### Índices implementados
- ArquivoUsuario: Hash Extensível indiceEmail (ParEmailId) para localização do usuário por email durante o login. Mantido sincronizado em create, update e delete.
- ArquivoCurso: duas Árvores B+ e uma Hash Extensível:
- indiceUsuarioCurso (ParIdId(idUsuario, idCurso)): registra o relacionamento 1:N entre usuário e cursos; uma busca com ParIdId(idUsuario, -1) retorna todos os cursos daquele usuário, graças ao compareTo tratando -1 como coringa.
- indiceNomeCurso (ParNomeId(nome, idCurso)): índice indireto por nome, usado para listar os cursos do usuário logado em ordem alfabética no menu.
- indiceCodigoCurso (ParCodigoId(codigo, idCurso)): índice indireto por NanoID, usado na busca de cursos por código compartilhável no menu Minhas Inscrições. Hash Extensível porque a busca é por igualdade exata (O(1)).
- ArquivoCursoUsuario: duas Árvores B+ que sustentam o relacionamento N:N do TP2:
- indiceCursoInscricao (ParIdId(idCurso, idCursoUsuario)): consulta inscrições do ponto de vista do dono do curso (todos os inscritos no curso X).
- indiceUsuarioInscricao (ParIdId(idUsuario, idCursoUsuario)): consulta inscrições do ponto de vista do aluno (todos os cursos em que o usuário Y está inscrito). A escolha de duas Árvores B+ separadas (em vez de uma só) é exatamente o que a especificação pede: o N:N precisa ser navegável a partir de qualquer um dos dois lados, e uma B+ só admite um único critério de ordenação por vez.
- ArquivoCurso (TP3): índice invertido sobre o nome do curso:
- indiceInvertido (`ListaInvertida` do prof, dicionário + blocos encadeados): para cada termo do nome (após tokenização, lowercase, remoção de acentos e filtragem de stop words em português) guarda a lista de pares `(idCurso, TF)` onde TF é a frequência do termo no nome. O IDF é calculado on-the-fly no momento da busca como `log10(N/df) + 1`, com N obtido do próprio contador interno da `ListaInvertida` e df sendo o tamanho da lista do termo. Mantido sincronizado em `create`, `delete` e `update` (re-indexa apenas quando o nome muda). O construtor de `ArquivoCurso` faz bootstrap automático: se o índice está vazio e há cursos no `dados.db` (caso de banco antigo do TP2), reindexa todos automaticamente.

### Fluxo de uso
1. Tela de acesso (deslogado): Login / Novo usuário / Recuperar senha / Sair.
2. Menu principal (logado): Meus dados / Meus cursos / Minhas inscrições / Sair.
3. Meus dados: visualização, edição e exclusão da conta do usuário logado.
4. Meus cursos: lista numerada em ordem alfabética (obtida via índice B+ de nome); digitar o número abre a tela de detalhe do curso. Opções A–E na tela de detalhe: **(A) Gerenciar inscritos no curso** abre a lista numerada dos inscritos com data de inscrição e oferece a opção de exportar a lista em CSV ou abrir o detalhe de cada inscrito (com cancelamento individual); (B) corrigir dados; (C) encerrar inscrições; (D) concluir; (E) cancelar curso (aviso o número de inscritos antes e cancela todas as inscrições em cascata).
5. Novo curso: o idUsuario é preenchido automaticamente com o do usuário ativo e o código NanoID é gerado pelo sistema. A data de início é informada pelo usuário em formato dd/MM/yyyy (o parser aceita também variações com 1 dígito como 5/8/2026 e o display normaliza para 05/08/2026 com zero à esquerda).
6. Minhas inscrições: lista no topo as inscrições atuais do usuário (com indicação de estado do curso quando aplicável: INSCRIÇÕES ENCERRADAS, CURSO CONCLUÍDO, CURSO CANCELADO); digitar o número da inscrição abre o detalhe com opção de cancelar. Três opções de busca: (A) por código NanoID, que abre direto a tela de detalhe; (B) por palavras-chave do nome (TP3), que aplica o modelo TFxIDF sobre o índice invertido e retorna a lista ordenada por relevância, com paginação de 10 em 10; (C) listagem paginada de todos os cursos disponíveis (10 por página, ordenados por data de início). A tela de detalhe do curso visitado exibe CÓDIGO, CURSO, AUTOR, DESCRIÇÃO e DATA DE INÍCIO e, conforme o caso, oferece o botão "(A) Fazer minha inscrição" (curso em estado 0, visitante não é o dono e não está inscrito) ou "(A) Cancelar minha inscrição" (já inscrito).

### Operações especiais

#### TP1
- Recuperação de senha por pergunta secreta: o usuário informa email e a resposta; se o hash bater com o armazenado, pode definir uma nova senha (ControleUsuario.recuperarSenha).
- Exclusão de conta com validação de cursos ativos: um usuário só pode ser excluído se não tiver nenhum curso em estado 0 ou 1; cursos em estado 2 ou 3 são removidos em cascata junto com a conta (ControleUsuario.excluirUsuario).
- Listagem alfabética por usuário: ArquivoCurso.readAllOrdenadoPorNome(idUsuario) combina o índice 1:N com o índice de nomes para montar o menu conforme a especificação.
- Geração automática de NanoID: ControleCurso.cadastrarCurso gera o código compartilhável de 10 caracteres no padrão NanoID usando `java.security.SecureRandom` (padrão do NanoID real) e regera em caso raríssimo de colisão verificada via `ArquivoCurso.readByCodigo`.
- Gestão de estado do curso: as opções C, D e E da tela de detalhe alteram o estado (encerrar inscrições, concluir, cancelar). O cancelamento equivale à exclusão quando não há inscritos.
- Unicidade de email: verificada no cadastro e na edição de dados do usuário via ArquivoUsuario.readEmail.
- Manutenção sincronizada dos índices: todos os create/update/delete de ArquivoUsuario e ArquivoCurso atualizam os índices correspondentes, garantindo consistência mesmo quando o email do usuário ou o nome/dono do curso mudam.
- Fechamento seguro dos arquivos: Principal fecha ArquivoUsuario, ArquivoCurso, ArquivoCursoUsuario e o Scanner em um bloco finally, evitando escritas pendentes truncadas.

#### TP2 — Busca de cursos
- Busca de cursos por código NanoID: `ControleCurso.buscarPorCodigo` consulta o `indiceCodigoCurso` em O(1) e ainda confirma por igualdade exata o código recuperado, blindando contra colisões teóricas de `hashCode()`. A tela `VisaoCurso.telaBuscaPorCodigoInscricao` pede o código e abre direto o detalhe do curso conforme a especificação.
- Listagem paginada de cursos disponíveis: `ControleCurso.listarTodosCursosDisponiveis` filtra apenas cursos em estado 0 e ordena por data de início; a paginação fica na visão (10 por página, item 10 → `(0)`, navegação A/B condicionada à existência da página anterior/próxima).
- Varredura completa do arquivo de cursos: `ArquivoCurso.readAllCursos` abre um `RandomAccessFile` próprio em modo read-only sobre o `dados.db` e percorre o arquivo respeitando o cabeçalho (12 bytes) e o esquema de lápides do `Arquivo` do prof, sem alterar o pacote `aed3`.
- Tela de detalhe do visitante (`telaDetalheCursoVisitante`): exibe CÓDIGO, CURSO, AUTOR (resolvido pelo nome via `ControleCurso.buscarAutor`), DESCRIÇÃO e DATA DE INÍCIO. O botão de ação se adapta ao contexto.

#### TP2 — Relacionamento N:N
- Entidade de associação `CursoUsuario` (id, idCurso, idUsuario, dataInscricao) com 20 bytes fixos por registro e duas Árvores B+ sincronizadas em `create`/`delete`/`update`: `indiceCursoInscricao` (lado dono) e `indiceUsuarioInscricao` (lado aluno).
- Consulta bidirecional do relacionamento: a partir de qualquer um dos dois lados (curso ou usuário), uma única consulta na Árvore B+ correspondente com `ParIdId(id, -1)` retorna todas as inscrições daquele lado em O(log n) + tamanho do resultado, graças ao coringa do `compareTo` do `ParIdId`.
- Integridade referencial em cascata: cancelar um curso (`ControleCurso.excluirCurso`) cancela automaticamente todas as inscrições daquele curso antes de remover o registro. Excluir uma conta de usuário (`ControleUsuario.excluirUsuario`) cancela as inscrições do usuário em cursos de terceiros e também as inscrições de terceiros nos cursos do usuário que serão deletados. Nenhuma inscrição órfã pode ficar apontando para um curso ou usuário inexistente.

#### TP2 — Gerenciamento das próprias inscrições
- Inscrição com validação de regras de negócio: `ControleInscricao.inscrever` valida em ordem: curso existe, curso em estado 0, dono não se inscreve no próprio curso, inscrição dupla. Retorna códigos de status (OK ou ERRO_*) para que a Visão renderize a mensagem adequada sem precisar lidar com exceptions.
- Menu Minhas Inscrições (`VisaoInscricao.menuMinhasInscricoes`): lista as inscrições atuais do usuário no topo, numeradas, com sufixo de estado do curso quando aplicável: `(INSCRICOES ENCERRADAS)`, `(CURSO CONCLUIDO)`, `(CURSO CANCELADO)`. Oferece as três opções de busca (A/B/C) e selecionar pelo número abre o detalhe da inscrição.
- Tela de detalhe de uma inscrição própria (`telaDetalheMinhaInscricao`): exibe CÓDIGO, CURSO, AUTOR, DESCRIÇÃO, DATA DE INÍCIO, INSCRITO EM e oferece o botão "(A) Cancelar minha inscricao no curso" com confirmação S/N.
- Botão contextual na tela do visitante: `VisaoCurso.telaDetalheCursoVisitante` consulta `estaInscrito` ao abrir e alterna entre "(A) Fazer minha inscrição no curso" e "(A) Cancelar minha inscrição no curso" — sem botão se o usuário é o dono ou se o curso não está mais aceitando inscrições.

#### TP2 — Visão dos inscritos nos seus cursos
- Tela "Gerenciar inscritos no curso" (`VisaoInscricao.telaGerenciarInscritos`): acessada do menu Meus Cursos > curso > opção A. Lista numerada dos inscritos com nome + data de inscrição em ordem alfabética. Selecionar pelo número abre o detalhe do inscrito; opção "(A) Exportar lista" gera arquivo CSV.
- Tela de detalhe do inscrito (`telaDetalheInscrito`): exibe NOME, EMAIL e DATA DE INSCRIÇÃO do aluno (visão do dono do curso), com opção "(A) Cancelar a inscrição deste aluno" e confirmação S/N. Permite ao dono remover individualmente cada inscrição.
- Exportação CSV de inscritos: `ControleInscricao.exportarCSV` gera CSV com cabeçalho (Nome, Email, DataInscricao) e escapa adequadamente campos contendo vírgulas, aspas duplas (duplicadas conforme RFC 4180) ou quebras de linha. A `VisaoInscricao` grava em `./exportacoes/inscritos_<codigo>.csv` e informa o caminho ao usuário.
- Aviso de inscritos no cancelamento de curso: a opção "(E) Cancelar curso" da tela de detalhe do dono consulta `controleInscricao.contarInscritos` antes da confirmação e mostra quantas inscrições serão atingidas pela cascata.

#### TP3 — Busca por palavras-chave (índice invertido + TFxIDF)
- Pipeline de processamento de termos (`TermosUtil.extrairTermos`): tokenização por separadores não-letra/dígito (vírgulas, hifens, parênteses, pontuação geral), normalização lowercase + remoção de acentos via `java.text.Normalizer.NFD` + remoção das marcas diacríticas, filtragem de stop words em português (artigos, preposições, conjunções, pronomes, numerais por extenso). Aplicado simetricamente na indexação do nome do curso e na query do usuário — garante que `"Introdução à Inteligência Artificial"`, `"INTRODUCAO INTELIGENCIA ARTIFICIAL"` e a busca `"inteligência"` casem com os mesmos termos normalizados.
- Indexação na criação do curso (`IndiceInvertidoCurso.inserir`): extrai os termos do nome, conta ocorrências por termo, calcula o TF como `ocorrências/total_termos_válidos` (ex: para "Introdução à Inteligência Artificial" o TF de cada termo é 1/3 = 0.333), insere os pares `(idCurso, TF)` no termo correspondente e incrementa o contador de entidades N usado depois pelo IDF.
- Sincronização em delete/update: ao excluir um curso, todos os termos do nome são removidos da lista invertida e N é decrementado; ao atualizar o nome de um curso, os termos antigos são removidos e os novos inseridos, sem mexer em N (é o mesmo curso). Garante que o índice nunca aponte para cursos que não existem mais.
- Busca por palavras-chave com TFxIDF (`IndiceInvertidoCurso.buscar` + `ControleCurso.buscarPorPalavras`): aplica o pipeline na query, para cada termo único calcula o IDF como `log10(N/df) + 1` (N = total de cursos indexados, df = tamanho da lista do termo), multiplica pelos TFs armazenados, soma scores por idCurso (um curso pode receber contribuição de vários termos da query) e ordena decrescente. Filtra para mostrar apenas cursos em estado 0 — o objetivo é descoberta para inscrição.
- Bootstrap automático do índice (`ArquivoCurso` construtor): se o `numeroEntidades()` da `ListaInvertida` é zero mas há cursos no `dados.db`, percorre todos e indexa um a um. Cobre o caso de upgrade — banco antigo do TP2 sendo aberto pela primeira vez no executável do TP3.
- Tela paginada de resultados (`VisaoCurso.telaBuscaPorPalavrasInscricao`): mostra "Página N de M — X resultado(s) ordenados por relevância", 10 cursos por página, item 10 → `(0)`, navegação A/B condicional à existência da página, selecionar pelo número abre o `telaDetalheCursoVisitante` já existente (reusa todo o fluxo de inscrição do TP2).


## Checklist Relatorio

### TP1
- [x] Há um CRUD de usuários (que estende a classe ArquivoIndexado, acrescentando Tabelas Hash Extensíveis e Árvores B+ como índices diretos e indiretos conforme necessidade) que funciona corretamente? Sim.
- [x] Há um CRUD de cursos (que estende a classe ArquivoIndexado, acrescentando Tabelas Hash Extensíveis e Árvores B+ como índices diretos e indiretos conforme necessidade) que funciona corretamente? Sim.
- [x] Os cursos estão vinculados aos usuários usando o idUsuario como chave estrangeira? Sim.
- [x] Há uma árvore B+ que registre o relacionamento 1:N entre usuários e cursos? Sim.
- [x] Há um CRUD de usuários (que estende a classe ArquivoIndexado, acrescentando Tabelas Hash Extensíveis e Árvores B+ como índices diretos e indiretos conforme necessidade)? Sim.
- [x] O trabalho compila corretamente? Sim.
- [x] O trabalho está completo e funcionando sem erros de execução? Sim.
- [x] O trabalho é original e não a cópia de um trabalho de outro grupo? Sim, o trabalho é de total autoria dos respectivos membros do grupo.

### TP2
- [x] Há um CRUD da entidade de associação CursoUsuario (que estende a classe ArquivoIndexado, acrescentando Tabelas Hash Extensíveis e Árvores B+ como índices diretos e indiretos conforme necessidade) que funciona corretamente? Sim. A entidade está em `entidades/inscricoes/CursoUsuario.java` e o CRUD em `ArquivoCursoUsuario.java`, que estende `Arquivo<CursoUsuario>` e mantém duas Árvores B+ (`indiceCursoInscricao` e `indiceUsuarioInscricao`) sincronizadas em create/delete/update. Validado pelo `TesteInscricaoNN` (29/29 verificações).
- [x] A visão de inscrições está corretamente implementada e permite consultas aos cursos em que um usuário está inscrito? Sim. `VisaoInscricao.menuMinhasInscricoes` lista no topo do menu Minhas Inscrições os cursos em que o usuário está inscrito (com indicação do estado do curso quando aplicável). A consulta usa `ControleInscricao.listarMinhasInscricoes`, que percorre a Árvore B+ `indiceUsuarioInscricao` e devolve os cursos correspondentes ordenados por data de início.
- [x] A visão de cursos funciona corretamente e permite a gestão dos usuários inscritos em um curso? Sim. No menu Meus Cursos > curso > "(A) Gerenciar inscritos no curso", o dono vê a lista numerada dos inscritos com nome e data de inscrição, pode exportar a lista em CSV (com escape RFC 4180) ou abrir o detalhe de cada inscrito (com cancelamento individual).
- [x] Há uma visualização dos cursos de outras pessoas por meio de um código NanoID? Sim. No menu Minhas Inscrições > "(A) Buscar curso por código", o usuário cola o NanoID de 10 caracteres e é levado direto à tela de detalhe do curso com CÓDIGO, CURSO, AUTOR, DESCRIÇÃO e DATA DE INÍCIO. A busca usa o `indiceCodigoCurso` (Hash Extensível) com confirmação por igualdade exata para blindar contra colisões teóricas de hashCode.
- [x] A integridade do relacionamento entre cursos e usuários está mantida em todas as operações? Sim. Cancelar um curso (`ControleCurso.excluirCurso`) cancela todas as inscrições daquele curso em cascata antes de remover o registro. Excluir uma conta (`ControleUsuario.excluirUsuario`) bloqueia se houver cursos ativos, e em seguida cancela as inscrições do usuário em cursos de terceiros e as inscrições de terceiros nos cursos do usuário antes da remoção. Os índices B+ são mantidos sincronizados pelos overrides de create/delete/update em todos os arquivos. Nenhuma inscrição órfã pode existir.
- [x] O trabalho compila corretamente? Sim, com `javac` openjdk-21 sem warnings.
- [x] O trabalho está completo e funcionando sem erros de execução? Sim. Três suítes de teste automáticas: TesteRelacionamento1N (23/23), TesteBuscaCursos (20/20), TesteInscricaoNN (29/29). Total 72/72.
- [x] O trabalho é original e não a cópia de um trabalho de outro grupo? Sim, autoria do Grupo 12 conforme histórico do repositório.

### TP3
- [x] O índice invertido com os termos dos nomes dos cursos foi criado usando a classe ListaInvertida? Sim. A classe `aed3.ListaInvertida` é exatamente a do código fornecido pelo Prof. Marcos Kutova (`kutova/AEDsIII/ListaInvertida`); a única adição foi um método `close()` para integrar com o ciclo de vida dos demais arquivos do sistema (todos os índices fecham em sequência no `ArquivoCurso.close()`). O wrapper `entidades/cursos/IndiceInvertidoCurso.java` instancia uma `ListaInvertida` apontando para `./dados/cursos/indiceInvertido.dicionario.db` e `./dados/cursos/indiceInvertido.blocos.db` e cuida do cálculo TFxIDF, mantendo a `ListaInvertida` em si exatamente como o prof. entregou.
- [x] É possível buscar cursos por palavras no menu de inscrição? Sim. No menu Minhas Inscrições, a opção `(B) Buscar curso por palavras-chave` chama a tela `VisaoCurso.telaBuscaPorPalavrasInscricao`, que pede a query, aplica o pipeline de tokenização+normalização+stop words, faz a busca via `ControleCurso.buscarPorPalavras` (que delega para `IndiceInvertidoCurso.buscar`), filtra para apenas cursos em estado 0 e exibe os resultados paginados (10 por página) **ordenados por relevância TFxIDF descendente**. Selecionar pelo número abre a tela de detalhe do curso (`telaDetalheCursoVisitante`) já existente do TP2, reusando todo o fluxo de inscrição.
- [x] O trabalho compila corretamente? Sim, com `javac` (openjdk-21) sem warnings.
- [x] O trabalho está completo e funcionando sem erros de execução? Sim. Quatro suítes de teste automáticas: TesteRelacionamento1N (23/23), TesteBuscaCursos (23/23), TesteInscricaoNN (29/29), TesteIndiceInvertido (23/23). Total **98/98**. O `TesteIndiceInvertido` reproduz exatamente o cenário da especificação (4 cursos sobre Inteligência/Introdução/Gestão, busca por "Inteligencia Artificial" → ordem 1, 3, 2 com curso 4 fora) e ainda valida: processamento de termos (acentos, lowercase, stop words, edge cases null/vazio), sincronização em delete/update, robustez (termos inexistentes, query só com stop words), filtragem por estado, e bootstrap automático de índice vazio com cursos legados.
- [x] O trabalho é original e não a cópia de um trabalho de outro grupo? Sim, autoria do Grupo 12 conforme histórico do repositório.

#### Nota sobre os valores numéricos da spec

A especificação dá os scores esperados como `(1; 0.808), (3; 0.656), (2; 0.375)`. Minha implementação calcula `(1; 0.809), (3; 0.710), (2; 0.375)`. Os valores do Curso 1 e Curso 2 batem (diferença em 0.001 é arredondamento float). O valor do Curso 3 difere porque a spec tem um pequeno typo numérico: ela mesma documenta que TF(artificial,3) × IDF(artificial) deveria dar `0.260` (`0.2 × 1.301`) mas escreve `0.206` no texto. O cálculo correto pela fórmula que a própria spec define (`TF × (log10(N/df) + 1)`) dá `0.710` para o Curso 3. **A ordem final dos cursos é a mesma (1, 3, 2)** e o curso 4 fica de fora nos dois casos.

## ADICIONAIS
Como membros deste grupo decidimos em colocar como o nosso grupo foi organizado aqui no **Github** e como mantemos a consistencia através de diversas implementações, tudo estará no arquivo `README2.md` abaixo:

[Arquivo que usamos para organizar o nosso fluxo de trabalho](https://github.com/JeanCarlos0112/trabalho_aeds3/blob/main/README2.md)
