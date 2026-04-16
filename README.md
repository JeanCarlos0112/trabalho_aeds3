# TRABALHO PRÁTICO AEDs III - GRUPO 12

## PARTICIPANTES
- Jean Carlos Lopes Lellis
- Miro Brito de Area Leao
- Luiz Fernando Santos Langa
- André Luiz Baptista Esteves Bassini 

## DESCRIÇÃO DO SISTEMA
O **G12 TP1 1.2** é um sistema textual em Java para gestão de cursos livres entre alunos da PUC Minas. Cada usuário cadastrado pode ofertar seus próprios cursos, que ficarão disponíveis para que outros usuários se inscrevam (funcionalidade de inscrição reservada para o TP2). O acesso é feito por email e senha, e toda a persistência é baseada em arquivos binários, com o CRUD genérico Arquivo do pacote aed3 como base, estendido por índices em Hash Extensível e Árvore B+.

### Arquitetura (MVC)
A classe de entrada é Principal, que abre os arquivos, instancia os controles/visões e centraliza o fechamento seguro dos arquivos ao sair. O projeto segue o padrão MVC separando dados, lógica e apresentação:

- Modelo (dados): Usuario, Curso, ArquivoUsuario, ArquivoCurso, ParEmailId, além das classes do pacote aed3 (Arquivo, HashExtensivel, ArvoreBMais, ParIdId, ParNomeId).
- Controle (lógica): ControleUsuario e ControleCurso.
- Visão (interface textual): VisaoUsuario e VisaoCurso.

### Entidades
- Usuario — id, nome, email, hashSenha, perguntaSecreta e hashRespostaSecreta. A senha nunca é armazenada em claro; o mesmo vale para a resposta secreta, usada na recuperação de senha.
- Curso — id, idUsuario (chave estrangeira), nome, descrição, data de início, código compartilhável NanoID (10 caracteres alfanuméricos gerado automaticamente) e estado (0: ativo recebendo inscrições, 1: ativo com inscrições encerradas, 2: concluído, 3: cancelado).

### Índices implementados
- ArquivoUsuario — Hash Extensível indiceEmail (ParEmailId) para localização do usuário por email durante o login. Mantido sincronizado em create, update e delete.
- ArquivoCurso — duas Árvores B+:
- indiceUsuarioCurso (ParIdId(idUsuario, idCurso)): registra o relacionamento 1:N entre usuário e cursos; uma busca com ParIdId(idUsuario, -1) retorna todos os cursos daquele usuário, graças ao compareTo tratando -1 como coringa.
- indiceNomeCurso (ParNomeId(nome, idCurso)): índice indireto por nome, usado para listar os cursos do usuário logado em ordem alfabética no menu.

### Fluxo de uso
1. Tela de acesso (deslogado): Login / Novo usuário / Recuperar senha / Sair.
2. Menu principal (logado): Meus dados / Meus cursos / Minhas inscrições (placeholder do TP2) / Sair.
3. Meus dados: visualização, edição e exclusão da conta do usuário logado.
4. Meus cursos: lista numerada em ordem alfabética (obtida via índice B+ de nome); digitar o número abre a tela de detalhe do curso com opções para editar, encerrar inscrições, concluir ou cancelar/excluir.
5. Novo curso: o idUsuario é preenchido automaticamente com o do usuário ativo e o código NanoID é gerado pelo sistema. 

### Operações especiais
- Recuperação de senha por pergunta secreta: o usuário informa email e a resposta; se o hash bater com o armazenado, pode definir uma nova senha (ControleUsuario.recuperarSenha).
- Exclusão de conta com validação de cursos ativos: um usuário só pode ser excluído se não tiver nenhum curso em estado 0 ou 1; cursos em estado 2 ou 3 são removidos em cascata junto com a conta (ControleUsuario.excluirUsuario).
- Listagem alfabética por usuário: ArquivoCurso.readAllOrdenadoPorNome(idUsuario) combina o índice 1:N com o índice de nomes para montar o menu conforme a especificação.
- Geração automática de NanoID: ControleCurso.cadastrarCurso gera o código compartilhável de 10 caracteres no padrão NanoID.
- Gestão de estado do curso: as opções C, D e E da tela de detalhe alteram o estado (encerrar inscrições, concluir, cancelar). O cancelamento equivale à exclusão quando não há inscritos (requisito já preparado para o TP2).
- Unicidade de email: verificada no cadastro e na edição de dados do usuário via ArquivoUsuario.readEmail.
- Manutenção sincronizada dos índices: todos os create/update/delete de ArquivoUsuario e ArquivoCurso atualizam os índices correspondentes, garantindo consistência mesmo quando o email do usuário ou o nome/dono do curso mudam.
- Fechamento seguro dos arquivos: Principal fecha ArquivoUsuario, ArquivoCurso e o Scanner em um bloco finally, evitando escritas pendentes truncadas.

## Checklist Relatorio

- [x] Há um CRUD de usuários (que estende a classe ArquivoIndexado, acrescentando Tabelas Hash Extensíveis e Árvores B+ como índices diretos e indiretos conforme necessidade) que funciona corretamente? Sim
- [x] Há um CRUD de cursos (que estende a classe ArquivoIndexado, acrescentando Tabelas Hash Extensíveis e Árvores B+ como índices diretos e indiretos conforme necessidade) que funciona corretamente? Sim
- [x] Os cursos estão vinculados aos usuários usando o idUsuario como chave estrangeira? Sim
- [x] Há uma árvore B+ que registre o relacionamento 1:N entre usuários e cursos? Sim
- [x] Há um CRUD de usuários (que estende a classe ArquivoIndexado, acrescentando Tabelas Hash Extensíveis e Árvores B+ como índices diretos e indiretos conforme necessidade)? Sim
- [x] O trabalho compila corretamente? Sim
- [x] O trabalho está completo e funcionando sem erros de execução? Sim
- [x] O trabalho é original e não a cópia de um trabalho de outro grupo? Sim, o trabalho é de autoria total dos membros do grupo
