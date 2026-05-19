# TRABALHO PRÁTICO AEDs III - GRUPO 12

## PARTICIPANTES
- Jean Carlos Lopes Lellis
- Miro Brito de Area Leao
- Luiz Fernando Santos Langa
- André Luiz Baptista Esteves Bassini 

## DESCRIÇÃO DO SISTEMA
O **G12 TP1 1.2** é um sistema textual em Java para gestão de cursos livres entre alunos da PUC Minas. Cada usuário cadastrado pode ofertar seus próprios cursos e inscrever-se nos cursos de outros usuários. O acesso é feito por email e senha, e toda a persistência é baseada em arquivos binários, com o CRUD genérico Arquivo do pacote aed3 como base, estendido por índices em Hash Extensível e Árvore B+. O sistema implementa todos os relacionamentos previstos no TP1 (1:N entre usuário e seus cursos) e no TP2 (N:N entre cursos e inscritos, via entidade de associação `CursoUsuario`).

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

### Fluxo de uso
1. Tela de acesso (deslogado): Login / Novo usuário / Recuperar senha / Sair.
2. Menu principal (logado): Meus dados / Meus cursos / Minhas inscrições / Sair.
3. Meus dados: visualização, edição e exclusão da conta do usuário logado.
4. Meus cursos: lista numerada em ordem alfabética (obtida via índice B+ de nome); digitar o número abre a tela de detalhe do curso. Opções A–E na tela de detalhe: **(A) Gerenciar inscritos no curso** abre a lista numerada dos inscritos com data de inscrição e oferece a opção de exportar a lista em CSV ou abrir o detalhe de cada inscrito (com cancelamento individual); (B) corrigir dados; (C) encerrar inscrições; (D) concluir; (E) cancelar curso (aviso o número de inscritos antes e cancela todas as inscrições em cascata).
5. Novo curso: o idUsuario é preenchido automaticamente com o do usuário ativo e o código NanoID é gerado pelo sistema.
6. Minhas inscrições: lista no topo as inscrições atuais do usuário (com indicação de estado do curso quando aplicável: INSCRIÇÕES ENCERRADAS, CURSO CONCLUÍDO, CURSO CANCELADO); digitar o número da inscrição abre o detalhe com opção de cancelar. Três opções de busca: (A) por código NanoID, que abre direto a tela de detalhe; (B) por palavras-chave (reservado para o TP3); (C) listagem paginada de todos os cursos disponíveis (10 por página, ordenados por data de início). A tela de detalhe do curso visitado exibe CÓDIGO, CURSO, AUTOR, DESCRIÇÃO e DATA DE INÍCIO e, conforme o caso, oferece o botão "(A) Fazer minha inscrição" (curso em estado 0, visitante não é o dono e não está inscrito) ou "(A) Cancelar minha inscrição" (já inscrito).

### Operações especiais
- Recuperação de senha por pergunta secreta: o usuário informa email e a resposta; se o hash bater com o armazenado, pode definir uma nova senha (ControleUsuario.recuperarSenha).
- Exclusão de conta com validação de cursos ativos: um usuário só pode ser excluído se não tiver nenhum curso em estado 0 ou 1; cursos em estado 2 ou 3 são removidos em cascata junto com a conta (ControleUsuario.excluirUsuario).
- Listagem alfabética por usuário: ArquivoCurso.readAllOrdenadoPorNome(idUsuario) combina o índice 1:N com o índice de nomes para montar o menu conforme a especificação.
- Geração automática de NanoID: ControleCurso.cadastrarCurso gera o código compartilhável de 10 caracteres no padrão NanoID usando `java.security.SecureRandom` (padrão do NanoID real) e regera em caso raríssimo de colisão verificada via `ArquivoCurso.readByCodigo`.
- Busca de cursos por código NanoID (TP2): `ControleCurso.buscarPorCodigo` consulta o `indiceCodigoCurso` em O(1) e ainda confirma por igualdade exata o código recuperado, blindando contra colisões teóricas de `hashCode()`.
- Listagem paginada de cursos disponíveis (TP2): `ControleCurso.listarTodosCursosDisponiveis` filtra apenas cursos em estado 0 e ordena por data de início; a paginação fica na visão (10 por página, item 10 → `(0)`, navegação A/B condicionada à existência da página anterior/próxima).
- Varredura completa do arquivo de cursos (TP2): `ArquivoCurso.readAllCursos` abre um `RandomAccessFile` próprio em modo read-only sobre o `dados.db` e percorre o arquivo respeitando o cabeçalho (12 bytes) e o esquema de lápides do `Arquivo` do prof, sem alterar o pacote `aed3`.
- Inscrição com validação de regras de negócio (TP2): `ControleInscricao.inscrever` valida em ordem: curso existe, curso em estado 0, dono não se inscreve no próprio curso, inscrição dupla. Retorna códigos de status (OK ou ERRO_*) para que a Visão renderize a mensagem adequada sem precisar lidar com exceptions.
- Consulta bidirecional do relacionamento N:N (TP2): a partir de qualquer um dos dois lados (curso ou usuário), uma única consulta na Árvore B+ correspondente com `ParIdId(id, -1)` retorna todas as inscrições daquele lado em O(log n) + tamanho do resultado. `ControleInscricao.listarMinhasInscricoes` (lado aluno) e `listarInscritos` (lado dono) materializam isso em objetos Curso/Usuário emparelhados com a inscrição.
- Integridade referencial em cascata (TP2): cancelar um curso (`ControleCurso.excluirCurso`) cancela automaticamente todas as inscrições daquele curso antes de remover o registro. Excluir uma conta de usuário (`ControleUsuario.excluirUsuario`) cancela as inscrições do usuário em cursos de terceiros e também as inscrições de terceiros nos cursos do usuário (que serão deletados em seguida). Nenhuma inscrição órfã pode ficar apontando para um curso ou usuário inexistente.
- Exportação CSV de inscritos (TP2): `ControleInscricao.exportarCSV` gera CSV com cabeçalho (Nome, Email, DataInscricao) e escapa adequadamente campos contendo vírgulas, aspas duplas (duplicadas conforme RFC 4180) ou quebras de linha. A `VisaoInscricao` grava em `./exportacoes/inscritos_<codigo>.csv` e informa o caminho ao usuário.
- Gestão de estado do curso: as opções C, D e E da tela de detalhe alteram o estado (encerrar inscrições, concluir, cancelar). O cancelamento equivale à exclusão quando não há inscritos (requisito já preparado para o TP2).
- Unicidade de email: verificada no cadastro e na edição de dados do usuário via ArquivoUsuario.readEmail.
- Manutenção sincronizada dos índices: todos os create/update/delete de ArquivoUsuario e ArquivoCurso atualizam os índices correspondentes, garantindo consistência mesmo quando o email do usuário ou o nome/dono do curso mudam.
- Fechamento seguro dos arquivos: Principal fecha ArquivoUsuario, ArquivoCurso e o Scanner em um bloco finally, evitando escritas pendentes truncadas.

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

## ADICIONAIS
Como membros deste grupo decidimos em colocar como o nosso grupo foi organizado aqui no **Github** e como mantemos a consistencia através de diversas implementações, tudo estará no arquivo `README2.md` abaixo:

[Arquivo que usamos para organizar o nosso fluxo de trabalho](https://github.com/JeanCarlos0112/trabalho_aeds3/blob/main/README2.md)
