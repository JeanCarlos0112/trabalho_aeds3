package entidades.usuarios;

import java.util.Scanner;

public class VisaoUsuario {
    private ControleUsuario controle;
    private Scanner console;

    public VisaoUsuario() throws Exception {
        this.controle = new ControleUsuario();
        this.console = new Scanner(System.in);
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
                System.out.println("Erro: O usuário possui cursos ativos e não pode ser excluído.");
            }
        } catch (Exception e) {
            System.out.println("Ocorreu um erro no sistema: " + e.getMessage());
        }
    }

    // primeira versão (vai ocorrer alteracoes)
    public void telaLogin() {
        System.out.print("Digite o email: ");
        String email = console.nextLine();

        System.out.print("Digite o senha: ");
        int senha = console.nextLine().hashCode();

        try {
            boolean sucesso = (controle.logarUsuario(email,senha) != null) ? true : false;
            if (sucesso) {
                System.out.println("Usuário logado");
            } else {
                System.out.println("Erro: Email e/ou senha invalidos");
            }
        } catch (Exception e) {
            System.out.println("Ocorreu um erro no sistema: " + e.getMessage());
        }
    }

    // primeira versão (vai ocorrer alteracoes)
    public void telaAtualização() {
    /*  Usuario usuarioEditado;
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
            boolean sucesso = controle.atualizarUsuario(usuarioEditado);
            if (sucesso) {
                System.out.println("Usuário cadastrado com sucesso!");
            } else {
                System.out.println("Erro: O usuário possui cursos ativos e não pode ser excluído.");
            }
        } catch (Exception e) {
            System.out.println("Ocorreu um erro no sistema: " + e.getMessage());
        }*/
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
