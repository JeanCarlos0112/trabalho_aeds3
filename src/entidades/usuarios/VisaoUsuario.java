package entidades.usuarios;

import java.util.Scanner;

import entidades.cursos.VisaoCurso;

/**
 * Visão de Usuário — menu principal e telas de login/cadastro/meus dados.
 *
 * Fluxo conforme PROPOSTA.md:
 *
 *   Tela inicial (deslogado):
 *     (A) Login
 *     (B) Novo usuario
 *     (C) Recuperar senha
 *     (S) Sair
 *
 *   Tela principal (logado):
 *     (A) Meus dados
 *     (B) Meus cursos
 *     (C) Minhas inscricoes
 *     (S) Sair (logout)
 */
public class VisaoUsuario {
    private ControleUsuario controle;
    private VisaoCurso visaoCurso;
    private Scanner console;
    private Usuario usuarioLogado;

    public VisaoUsuario(ControleUsuario controle, VisaoCurso visaoCurso, Scanner console) {
        this.controle = controle;
        this.visaoCurso = visaoCurso;
        this.console = console;
        this.usuarioLogado = null;
    }

    /**
     * Menu do usuario
    */
    public void menuUsuario() {
        while (true) {
            if (usuarioLogado == null) {
                if (!menuDeslogado()) return; // usuário escolheu sair
            } else {
                if (!menuLogado()) {
                    this.usuarioLogado = null;  // logout
                }
            }
        }
    }

    /**
     * Menu Principal Usuario (Deslogado)
     * @return retorna false quando o usuário escolhe "Sair".
     */
    private boolean menuDeslogado() {
        System.out.println("\nG12 TP1 1.2");
        System.out.println("--------------\n");
        System.out.println("(A) Login");
        System.out.println("(B) Novo usuario");
        System.out.println("(C) Recuperar senha");
        System.out.println("\n(S) Sair");
        System.out.print("\nOpcao: ");

        String op = console.nextLine().trim().toUpperCase();
        switch (op) {
            case "A": telaLogin(); return true;
            case "B": telaCadastro(); return true;
            case "C": telaRecuperacaoSenha(); return true;
            case "S":
                System.out.println("\nSaindo do sistema...");
                return false;
            default:
                System.out.println("Opcao invalida.");
                return true;
        }
    }

    /**
     * Menu Principal Usuario (logado)
     * @return retorna false quando o usuário faz logout ou exclui a conta.
     */
    private boolean menuLogado() {
        System.out.println("\nG12 TP1 1.2");
        System.out.println("--------------");
        System.out.println("> Inicio  (usuario: " + usuarioLogado.getNome() + ")\n");
        System.out.println("(A) Meus dados");
        System.out.println("(B) Meus cursos");
        System.out.println("(C) Minhas inscricoes");
        System.out.println("(S) Sair (logout)");
        System.out.print("\nOpcao: ");

        String op = console.nextLine().trim().toUpperCase();
        switch (op) {
            case "A":
                return telaMeusDados();
            case "B":
                visaoCurso.menuCurso(usuarioLogado.getID());
                return true;
            case "C":
                System.out.println("\n(Minhas inscricoes sera implementado no TP2.)");
                return true;
            case "S":
                System.out.println("\nLogout realizado.");
                return false;
            default:
                System.out.println("Opcao invalida.");
                return true;
        }
    }

    /**
     * TELA "MEUS DADOS" - exibe dados e oferece editar/excluir conta
     * @return retorna false se a conta foi excluída (obriga logout).
     */
    private boolean telaMeusDados() {
        while (true) {
            System.out.println("\nG12 TP1 1.2");
            System.out.println("--------------");
            System.out.println("> Inicio > Meus Dados\n");
            System.out.println("NOME.....: " + usuarioLogado.getNome());
            System.out.println("EMAIL....: " + usuarioLogado.getEmail());
            System.out.println("PERGUNTA.: " + usuarioLogado.getPerguntaSecreta());
            System.out.println();
            System.out.println("(A) Editar dados");
            System.out.println("(B) Excluir minha conta");
            System.out.println("(R) Retornar ao menu anterior");
            System.out.print("\nOpcao: ");

            String op = console.nextLine().trim().toUpperCase();
            switch (op) {
                case "A":
                    telaAtualizacao();
                    break;
                case "B":
                    if (telaExclusao()) return false;  // conta excluída
                    break;
                case "R":
                    return true;
                default:
                    System.out.println("Opcao invalida.");
            }
        }
    }

    /**
     * TELAS DE ENTRADA/SAÍDA
     */
    public void telaCadastro() {
        System.out.println("\nG12 TP1 1.2");
        System.out.println("--------------");
        System.out.println("> Inicio > Novo Usuario\n");

        System.out.print("Nome: ");
        String nome = console.nextLine();

        System.out.print("Email: ");
        String email = console.nextLine();

        System.out.print("Senha: ");
        int senha = console.nextLine().hashCode();

        System.out.print("Pergunta secreta: ");
        String perguntaSecreta = console.nextLine();

        System.out.print("Resposta secreta: ");
        int respostaSecreta = console.nextLine().hashCode();

        try {
            boolean sucesso = controle.cadastrarUsuario(nome, email, senha, perguntaSecreta, respostaSecreta);
            if (sucesso) {
                System.out.println("\nUsuario cadastrado com sucesso! Use a opcao (A) para fazer login.");
            } else {
                System.out.println("\nErro: Este email ja esta cadastrado no sistema.");
            }
        } catch (Exception e) {
            System.out.println("\nErro no sistema: " + e.getMessage());
        }
    }

    /**
     * Tela LOGIN
     */
    public void telaLogin() {
        System.out.println("\nG12 TP1 1.2");
        System.out.println("--------------");
        System.out.println("> Inicio > Login\n");

        System.out.print("Email: ");
        String email = console.nextLine();

        System.out.print("Senha: ");
        int senha = console.nextLine().hashCode();

        try {
            this.usuarioLogado = controle.logarUsuario(email, senha);
            if (usuarioLogado != null) {
                System.out.println("\nBem-vindo, " + usuarioLogado.getNome() + "!");
            } else {
                System.out.println("\nEmail e/ou senha invalidos.");
            }
        } catch (Exception e) {
            System.out.println("\nErro no sistema: " + e.getMessage());
        }
    }

    /**
     * Tela ATUALIZAÇÃO de dados
     */
    public void telaAtualizacao() {
        System.out.println("\nG12 TP1 1.2");
        System.out.println("--------------");
        System.out.println("> Inicio > Meus Dados > Editar\n");
        System.out.println("(Deixe em branco para manter o valor atual.)\n");

        System.out.print("Nome (" + usuarioLogado.getNome() + "): ");
        String nome = console.nextLine();
        if (nome.trim().isEmpty()) nome = usuarioLogado.getNome();

        System.out.print("Email (" + usuarioLogado.getEmail() + "): ");
        String email = console.nextLine();
        if (email.trim().isEmpty()) email = usuarioLogado.getEmail();

        System.out.print("Nova senha (em branco = manter): ");
        String senhaTxt = console.nextLine();
        int senha = senhaTxt.isEmpty() ? usuarioLogado.getHashSenha() : senhaTxt.hashCode();

        System.out.print("Pergunta secreta (" + usuarioLogado.getPerguntaSecreta() + "): ");
        String perguntaSecreta = console.nextLine();
        if (perguntaSecreta.trim().isEmpty()) perguntaSecreta = usuarioLogado.getPerguntaSecreta();

        System.out.print("Nova resposta secreta (em branco = manter): ");
        String respTxt = console.nextLine();
        int respostaSecreta = respTxt.isEmpty() ? usuarioLogado.getHashRespostaSecreta() : respTxt.hashCode();

        Usuario usuarioEditado = new Usuario(nome, email, senha, perguntaSecreta, respostaSecreta);
        usuarioEditado.setID(usuarioLogado.getID());

        try {
            boolean sucesso = controle.atualizarUsuario(usuarioEditado);
            if (sucesso) {
                System.out.println("\nDados atualizados.");
                this.usuarioLogado = usuarioEditado;
            } else {
                System.out.println("\nErro: O novo email ja esta em uso por outro usuario.");
            }
        } catch (Exception e) {
            System.out.println("\nErro no sistema: " + e.getMessage());
        }
    }

    /**
     * Tela RECUPERAÇÃO DE CONTA
     */
    public void telaRecuperacaoSenha() {
        System.out.println("\nG12 TP1 1.2");
        System.out.println("--------------");
        System.out.println("> Inicio > Recuperar Senha\n");

        System.out.print("Email: ");
        String email = console.nextLine();

        try {
            String pergunta = controle.obterPerguntaSecreta(email);
            if (pergunta == null) {
                System.out.println("\nEmail nao encontrado.");
                return;
            }

            System.out.println("\nSua pergunta secreta: " + pergunta);
            System.out.print("Resposta: ");
            int resposta = console.nextLine().hashCode();

            System.out.print("Nova senha: ");
            int novaSenha = console.nextLine().hashCode();

            boolean sucesso = controle.recuperarSenha(email, resposta, novaSenha);
            if (sucesso) {
                System.out.println("\nSenha alterada com sucesso!");
            } else {
                System.out.println("\nResposta secreta incorreta.");
            }
        } catch (Exception e) {
            System.out.println("\nErro: " + e.getMessage());
        }
    }

    /**
     * Exclusão de conta
     * @return  Retorna true se a conta foi de fato excluída (para que o menu force logout).
     */
    private boolean telaExclusao() {
        System.out.print("\nTem certeza que deseja excluir sua conta? (S/N): ");
        String confirmacao = console.nextLine();

        if (!confirmacao.equalsIgnoreCase("S")) {
            System.out.println("Exclusao cancelada.");
            return false;
        }

        try {
            boolean sucesso = controle.excluirUsuario(this.usuarioLogado.getID());
            if (sucesso) {
                System.out.println("\nConta excluida.");
                return true;
            } else {
                System.out.println("\nVoce possui cursos ativos (estado 0 ou 1). Cancele-os ou conclua-os antes de excluir a conta.");
                return false;
            }
        } catch (Exception e) {
            System.out.println("\nErro no sistema: " + e.getMessage());
            return false;
        }
    }
}
