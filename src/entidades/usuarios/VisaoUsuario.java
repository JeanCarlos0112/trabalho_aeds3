package entidades.usuarios;

import java.util.Scanner;

public class VisaoUsuario {
    private ControleUsuario controle;
    private Scanner console;

    public VisaoUsuario() throws Exception {
        this.controle = new ControleUsuario();
        this.console = new Scanner(System.in);
    }

    public void menuUsuario() {
        System.out.println("╔════════════════════╗");
        System.out.println("║    Menu Usuario    ║");
        System.out.println("╚════════════════════╝\n");
        System.out.println("Opcoes:");
        System.out.println("1 - Cadastrar");
        System.out.println("2 - Login");
        System.out.println("3 - Atualizar Usuario");
        System.out.println("4 - Excluir Usuario");
        switch (console.nextInt()) {
            case 1:
                telaCadastro();
                break;
            case 2:
                telaLogin();
                break;
            case 3:
                telaAtualização();
                break;
            case 4:
                telaExclusao();
                break;
            default:
                System.out.println("Opção inválida.");
                break;
        }
    }

    public void telaCadastro() {
        System.out.print("Digite o nome: ");
        String nome = console.nextLine();

        System.out.print("Digite o email: ");
        String email = console.nextLine();

        System.out.print("Digite o senha: ");
        int senha = console.nextLine().hashCode();
        
        System.out.print("Digite o pergunta secreta: ");
        String perguntaSecreta = console.nextLine();

        System.out.print("Digite a resposta secreta: ");
        int respostaSecreta = console.nextLine().hashCode();

        try {
            boolean sucesso = controle.cadastrarUsuario(nome,email,senha,perguntaSecreta,respostaSecreta);
            if (sucesso) {
                System.out.println("Usuário cadastrado com sucesso!");
            } else {
                System.out.println("Erro: Este email já está cadastrado no sistema.");            
            }
        } catch (Exception e) {
            System.out.println("Ocorreu um erro no sistema: " + e.getMessage());
        }
    }

    // primeira versão (vai ocorrer alteracoes)
    public void telaLogin() {
        System.out.print("Digite o email: ");
        String email = console.nextLine();

        System.out.print("Digite a senha: ");
        int senha = console.nextLine().hashCode();

        Usuario usuarioLogado = new Usuario();

        try {
            usuarioLogado = controle.logarUsuario(email, senha);
            
            if (usuarioLogado != null) {
                System.out.println("Usuário logado com sucesso! Bem-vindo, " + usuarioLogado.getNome());
                System.out.println("nome: " + usuarioLogado.getNome());
            } else {
                System.out.println("Erro: Email e/ou senha inválidos");
            }
        } catch (Exception e) {
            System.out.println("Ocorreu um erro no sistema: " + e.getMessage());
        }
    }

    // primeira versão (vai ocorrer alteracoes)
    public void telaAtualização() {
        System.out.print("Digite o nome: ");
        String nome = console.nextLine();

        System.out.print("Digite o email: ");
        String email = console.nextLine();

        System.out.print("Digite o senha: ");
        int senha = console.nextLine().hashCode();
        
        System.out.print("Digite o pergunta secreta: ");
        String perguntaSecreta = console.nextLine();

        System.out.print("Digite a resposta secreta: ");
        int respostaSecreta = console.nextLine().hashCode();

        // 1. Instanciar o objeto COM o ID do usuário que está logando
        Usuario usuarioEditado = new Usuario(nome, email, senha, perguntaSecreta, respostaSecreta);

        try {
            boolean sucesso = controle.atualizarUsuario(usuarioEditado);
            if (sucesso) {
                System.out.println("Usuário atualizado com sucesso!");
            } else {
                System.out.println("Erro: O novo email escolhido já está em uso por outro usuário.");
            }
        } catch (Exception e) {
            System.out.println("Ocorreu um erro no sistema: " + e.getMessage());
        }
    }

    public void telaExclusao() {
        System.out.print("Digite o ID do usuário que deseja excluir: ");
        int id = console.nextInt();

        try {
            boolean sucesso = controle.excluirUsuario(id);
            if (sucesso) {
                System.out.println("Usuário e seus cursos inativos foram excluídos com sucesso!");
            } else {
                System.out.println("Erro: O usuário possui cursos ativos e não pode ser excluído.");
            }
        } catch (Exception e) {
            System.out.println("Ocorreu um erro no sistema: " + e.getMessage());
        }
    }
}
