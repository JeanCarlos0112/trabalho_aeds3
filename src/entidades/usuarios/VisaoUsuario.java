package entidades.usuarios;

import java.util.Scanner;

import entidades.cursos.VisaoCurso;

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

    public void menuUsuario() {
        int opcao = -1;
        do {
            if (usuarioLogado == null) {
                System.out.println("\n╔════════════════════╗");
                System.out.println("║    Menu Usuario    ║");
                System.out.println("╚════════════════════╝\n");
                System.out.println("Opcoes:");
                System.out.println("1 - Cadastrar");
                System.out.println("2 - Login");
                System.out.println("3 - Mudar Senha");
                System.out.println("0 - Sair");

                // FIX: try-catch no parseInt
                try {
                    opcao = Integer.parseInt(console.nextLine().trim());
                } catch (NumberFormatException e) {
                    System.out.println("Entrada invalida. Digite um numero.");
                    continue;
                }

                switch (opcao) {
                    case 1: telaCadastro(); break;
                    case 2: telaLogin(); break;
                    case 3: telaRecuperacaoSenha(); break;
                    case 0: System.out.println("Saindo..."); break;
                    default: System.out.println("Opcao invalida."); break;
                }
            } else {
                System.out.println("\n╔════════════════════════════════╗");
                System.out.println("║ Meus Dados (" + usuarioLogado.getNome() + ")");
                System.out.println("╚════════════════════════════════╝");
                System.out.println("Opcoes:");
                System.out.println("1 - Cursos");
                System.out.println("2 - Atualizar Meus Dados");
                System.out.println("3 - Excluir Minha Conta");
                System.out.println("0 - Logout (Sair da conta)");
                System.out.print("Escolha: ");

                try {
                    opcao = Integer.parseInt(console.nextLine().trim());
                } catch (NumberFormatException e) {
                    System.out.println("Entrada invalida. Digite um numero.");
                    continue;
                }

                switch (opcao) {
                    case 1:
                        // FIX: Usa a instância compartilhada de VisaoCurso
                        visaoCurso.menuCurso(this.usuarioLogado.getID());
                        break;
                    case 2: telaAtualizacao(); break;
                    case 3: telaExclusao(); break;
                    case 0: 
                        System.out.println("Logout realizado.");
                        this.usuarioLogado = null; 
                        opcao = -1; 
                        break;
                    default: System.out.println("Opcao invalida."); break;
                }
            }
        } while (opcao != 0);
    }

    public void telaCadastro() {
        System.out.print("Digite o nome: ");
        String nome = console.nextLine();

        System.out.print("Digite o email: ");
        String email = console.nextLine();

        System.out.print("Digite a senha: ");
        int senha = console.nextLine().hashCode();
        
        System.out.print("Digite a pergunta secreta: ");
        String perguntaSecreta = console.nextLine();

        System.out.print("Digite a resposta secreta: ");
        int respostaSecreta = console.nextLine().hashCode();

        try {
            boolean sucesso = controle.cadastrarUsuario(nome, email, senha, perguntaSecreta, respostaSecreta);
            if (sucesso) {
                System.out.println("Usuario cadastrado com sucesso!");
            } else {
                System.out.println("Erro: Este email ja esta cadastrado no sistema.");            
            }
        } catch (Exception e) {
            System.out.println("Ocorreu um erro no sistema: " + e.getMessage());
        }
    }

    public void telaLogin() {
        System.out.print("Digite o email: ");
        String email = console.nextLine();

        System.out.print("Digite a senha: ");
        int senha = console.nextLine().hashCode();

        try {
            this.usuarioLogado = controle.logarUsuario(email, senha);
            
            if (usuarioLogado != null) {
                System.out.println("Usuario logado com sucesso! Bem-vindo, " + usuarioLogado.getNome());
            } else {
                System.out.println("Erro: Email e/ou senha invalidos");
            }
        } catch (Exception e) {
            System.out.println("Ocorreu um erro no sistema: " + e.getMessage());
        }
    }

    public void telaAtualizacao() {
        System.out.print("Digite o nome: ");
        String nome = console.nextLine();

        System.out.print("Digite o email: ");
        String email = console.nextLine();

        System.out.print("Digite a senha: ");
        int senha = console.nextLine().hashCode();

        System.out.print("Digite a pergunta secreta: ");
        String perguntaSecreta = console.nextLine();

        System.out.print("Digite a resposta secreta: ");
        int respostaSecreta = console.nextLine().hashCode();

        Usuario usuarioEditado = new Usuario(nome, email, senha, perguntaSecreta, respostaSecreta);
        usuarioEditado.setID(usuarioLogado.getID());

        try {
            boolean sucesso = controle.atualizarUsuario(usuarioEditado);
            if (sucesso) {
                System.out.println("Usuario atualizado com sucesso!");
                this.usuarioLogado = usuarioEditado;
            } else {
                System.out.println("Erro: O novo email escolhido ja esta em uso por outro usuario.");
            }
        } catch (Exception e) {
            System.out.println("Ocorreu um erro no sistema: " + e.getMessage());
        }
    }

    public void telaRecuperacaoSenha() {
        System.out.print("Digite o seu email: ");
        String email = console.nextLine();
        
        try {
            String pergunta = controle.obterPerguntaSecreta(email);
            if (pergunta == null) {
                System.out.println("Erro: Email nao encontrado.");
                return;
            }

            System.out.println("Sua pergunta secreta e: " + pergunta);
            System.out.print("Digite a resposta: ");
            int resposta = console.nextLine().hashCode();

            System.out.print("Digite a nova senha: ");
            int novaSenha = console.nextLine().hashCode();
            
            boolean sucesso = controle.recuperarSenha(email, resposta, novaSenha);
            if (sucesso) {
                System.out.println("Senha alterada com sucesso! Voce ja pode fazer login.");
            } else {
                System.out.println("Erro: Resposta secreta incorreta.");
            }
        } catch (Exception e) {
            System.out.println("Ocorreu um erro: " + e.getMessage());
        }
    }

    public void telaExclusao() {
        System.out.print("Tem certeza que deseja excluir sua conta? (S/N): ");
        String confirmacao = console.nextLine();

        if (confirmacao.equalsIgnoreCase("S")) {
            try {
                boolean sucesso = controle.excluirUsuario(this.usuarioLogado.getID());
                if (sucesso) {
                    System.out.println("Conta excluida com sucesso!");
                    this.usuarioLogado = null;
                } else {
                    System.out.println("Erro: Voce possui cursos ativos e nao pode excluir a conta.");
                }
            } catch (Exception e) {
                System.out.println("Ocorreu um erro no sistema: " + e.getMessage());
            }
        } else {
            System.out.println("Exclusao cancelada.");
        }
    }
}
