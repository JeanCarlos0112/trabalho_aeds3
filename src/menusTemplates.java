import java.time.LocalDate;
import java.util.*;

import entidades.cursos.ArquivoCurso;
import entidades.cursos.Curso;
import entidades.cursos.VisaoCurso;
import entidades.usuarios.ArquivoUsuario;
import entidades.usuarios.ControleUsuario;
import entidades.usuarios.Usuario;
import entidades.usuarios.VisaoUsuario;

public class menusTemplates {
    public static void createAdmin() {
        try {
            ArquivoUsuario arqUsuario = new ArquivoUsuario();
            ArquivoCurso arqCurso = new ArquivoCurso();

            Usuario u1 = new Usuario("Admin", "admin@gmail.com", "admin123".hashCode(), "Pergunta?", "resposta".hashCode());
            int idAlice = arqUsuario.create(u1);

            Curso c1 = new Curso(idAlice, "Python basico", "Curso de Python", LocalDate.of(2026, 5, 20), "abc1234567", 0);
            
            int idC1 = arqCurso.create(c1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // ===========================
    // TELAS DE MENUS
    // ===========================
    public static void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        header();
    }

    public static void header() {
        System.out.println("G12 TP1 1.1");
        System.out.println("--------------");
    }

    public static void firstMenu() {
        System.out.println("\n(A) Login");
        System.out.println("(B) Novo usuário");
        System.out.println();
        System.out.println("(S) Sair");
        System.out.println();
        System.out.print("Opção: ");
    }

    public static void loggedMenu() {
        System.out.println("> Início");
        System.out.println();
        System.out.println("(A) Meus dados");
        System.out.println("(B) Meus cursos");
        System.out.println("(C) Minhas inscrições");
        System.out.println();
        System.out.println("(S) Sair");
        System.out.println();
        System.out.print("Opção: ");
    }

    public static void cursoMenu() {
        System.out.println("> Início > Meus cursos");
        System.out.println();
        System.out.println("CURSOS");
        // Listagem de cursos deve ser em ordem alfabetica
        // ($valorTemporarioParaPrint) $nomeDoCurso $dataInclusão
        // (1) Finanças pessoais - 10/02/2026
        // (2) Javascript para iniciantes - 15/04/2026
        // (3) Descubra o Python - 20/05/2026
        System.out.println("(A) Novo curso");
        System.out.println("(R) Retornar ao menu anterior");
        System.out.println();
        System.out.print("Opção: ");
    }

    public static void cursoSelecionado() {
        System.out.println("> Início > Meus cursos > $nomeCurso");
        System.out.println();
        // Esta parte pode ser implementada em visaoCurso
        System.out.println("CÓDIGO........:");
        System.out.println("NOME..........:");
        System.out.println("DESCRIÇÃO.....:");
        System.out.println("DATA DE INÍCIO:");
        System.out.println();
        System.out.println("Este curso (não)/está aberto para inscrições!"); // Status do curso
        // ==========
        System.out.println("(A) Gerenciar inscritos no curso" + "\t// A ser implementada"); // Não implementada ainda
        System.out.println("(B) Corrigir dados do curso");
        System.out.println("(C) Encerrar inscrições");
        System.out.println("(D) Concluir curso");
        System.out.println("(E) Cancelar curso");
        System.out.println();
        System.out.println("(R) Retornar ao menu anterior");
        System.out.println();
        System.out.print("Opção: ");
    }

    // ===========================
    // MAIN
    // ===========================
    private static Usuario usuarioLogado = null;
    
    public static void main(String[] args) {
        createAdmin();
        Scanner input = new Scanner(System.in);
        char opcao = 'Z';
    
        clearScreen();
        do {
            if (usuarioLogado == null) {
                firstMenu();
            
                opcao = Character.toUpperCase(input.nextLine().charAt(0));
                switch (opcao) {
                    case 'A':
                        try {
                            VisaoUsuario visao = new VisaoUsuario();
                            visao.telaLogin(); // LOGIN
                        } catch (Exception e) {
                            System.out.println("Ocorreu um erro ao acessar cursos: " + e.getMessage());
                        }
                        break;
                    case 'B':
                        // CADASTRO
                        break;
                    case 'S':
                        // Exit
                        break;
                    default:
                        System.out.println("Opção inválida!");
                }
            } else {
                // loggedMenu();
                // cursoMenu();
                // cursoSelecionado();
            }


        } while (opcao != 'S');
        input.close();
    }
}