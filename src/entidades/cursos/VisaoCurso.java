package entidades.cursos;

import java.util.Scanner;
import java.util.ArrayList;

public class VisaoCurso {
    private ControleCurso controle;
    private Scanner console;

    public VisaoCurso() throws Exception {
        this.controle = new ControleCurso();
        this.console = new Scanner(System.in);
    }

    public void menuCurso(int idUsuarioLogado) {
        System.out.println("╔════════════════════╗");
        System.out.println("║    Menu Cursos     ║");
        System.out.println("╚════════════════════╝\n");
        System.out.println("Opcoes:");
        System.out.println("1 - Listar Meus Cursos");
        System.out.println("2 - Cadastrar Novo Curso");
        System.out.println("3 - Atualizar Curso");
        System.out.println("4 - Excluir Curso");
        
        // Uso de parseInt para evitar problemas com a quebra de linha do Scanner
        int opcao = Integer.parseInt(console.nextLine());

        switch (opcao) {
            case 1:
                telaListagem(idUsuarioLogado);
                break;
            case 2:
                telaCadastro(idUsuarioLogado);
                break;
            case 3:
                telaAtualizacao(idUsuarioLogado);
                break;
            case 4:
                telaExclusao(idUsuarioLogado);
                break;
            default:
                System.out.println("Opção inválida.");
                break;
        }
    }

    public void telaListagem(int idUsuarioLogado) {
        try {
            // Busca a lista ordenada alfabeticamente via índice secundário de nomes
            ArrayList<Curso> cursos = controle.listarCursosOrdenados(idUsuarioLogado);
            
            if (cursos.isEmpty()) {
                System.out.println("Nenhum curso cadastrado.");
            } else {
                System.out.println("\n--- LISTA DE CURSOS ---");
                for (int i = 0; i < cursos.size(); i++) {
                    Curso c = cursos.get(i);
                    System.out.println((i + 1) + ". [" + c.getCodigo() + "] " + c.getNome());
                    System.out.println("   ID: " + c.getID() + " | Status: " + c.getEstadoTexto());
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao listar cursos: " + e.getMessage());
        }
    }

    public void telaCadastro(int idUsuarioLogado) {
        System.out.print("Digite o nome do curso: ");
        String nome = console.nextLine();

        System.out.print("Digite a descrição: ");
        String descricao = console.nextLine();

        try {
            int idGerado = controle.cadastrarCurso(idUsuarioLogado, nome, descricao);
            System.out.println("Curso cadastrado com sucesso! ID: " + idGerado);
        } catch (Exception e) {
            System.out.println("Erro ao cadastrar: " + e.getMessage());
        }
    }

    public void telaAtualizacao(int idUsuarioLogado) {
        System.out.print("Digite o ID do curso que deseja atualizar: ");
        int id = Integer.parseInt(console.nextLine());

        try {
            Curso cursoExistente = controle.buscarCurso(id);

            // Valida se o curso existe e pertence ao usuário logado
            if (cursoExistente != null && cursoExistente.getIdUsuario() == idUsuarioLogado) {
                System.out.print("Novo nome (" + cursoExistente.getNome() + "): ");
                String nome = console.nextLine();
                System.out.print("Nova descrição: ");
                String desc = console.nextLine();
                System.out.print("Novo estado (0-Pendente, 1-Ativo, 2-Concluído): ");
                int estado = Integer.parseInt(console.nextLine());

                // Mantém o ID, FK, Data e o NanoID original
                Curso editado = new Curso(id, idUsuarioLogado, nome, desc, cursoExistente.getDataInicio(), cursoExistente.getCodigo(), estado);

                if (controle.atualizarCurso(editado)) {
                    System.out.println("Curso atualizado com sucesso!");
                }
            } else {
                System.out.println("Erro: Curso não encontrado ou acesso negado.");
            }
        } catch (Exception e) {
            System.out.println("Erro na atualização: " + e.getMessage());
        }
    }

    public void telaExclusao(int idUsuarioLogado) {
        System.out.print("Digite o ID do curso que deseja excluir: ");
        int id = Integer.parseInt(console.nextLine());

        try {
            Curso c = controle.buscarCurso(id);
            if (c != null && c.getIdUsuario() == idUsuarioLogado) {
                if (controle.excluirCurso(id)) {
                    System.out.println("Curso removido com sucesso!");
                }
            } else {
                System.out.println("Erro: Curso não encontrado ou sem permissão para excluir.");
            }
        } catch (Exception e) {
            System.out.println("Erro na exclusão: " + e.getMessage());
        }
    }
}