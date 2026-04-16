package entidades.cursos;

import java.util.Scanner;
import java.util.ArrayList;

public class VisaoCurso {
    private ControleCurso controle;
    private Scanner console;

    public VisaoCurso(ControleCurso controle, Scanner console) {
        this.controle = controle;
        this.console = console;
    }

    public void menuCurso(int idUsuarioLogado) {
        int opcao = -1;
        do {
            System.out.println("\n╔════════════════════╗");
            System.out.println("║    Menu Cursos     ║");
            System.out.println("╚════════════════════╝\n");
            System.out.println("Opcoes:");
            System.out.println("1 - Listar Meus Cursos");
            System.out.println("2 - Cadastrar Novo Curso");
            System.out.println("3 - Atualizar Curso");
            System.out.println("4 - Excluir Curso");
            System.out.println("0 - Voltar ao Menu Anterior");

            // FIX: try-catch no parseInt para evitar crash com entrada inválida
            try {
                opcao = Integer.parseInt(console.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Entrada invalida. Digite um numero.");
                continue;
            }

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
                case 0:
                    break;
                default:
                    System.out.println("Opcao invalida.");
                    break;
            }
        } while (opcao != 0);
    }

    public void telaListagem(int idUsuarioLogado) {
        try {
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

        System.out.print("Digite a descricao: ");
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
        int id;
        try {
            id = Integer.parseInt(console.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("ID invalido.");
            return;
        }

        try {
            Curso cursoExistente = controle.buscarCurso(id);

            if (cursoExistente != null && cursoExistente.getIdUsuario() == idUsuarioLogado) {
                System.out.print("Novo nome (" + cursoExistente.getNome() + "): ");
                String nome = console.nextLine();
                System.out.print("Nova descricao: ");
                String desc = console.nextLine();

                // FIX: Labels de estado corrigidos conforme README
                System.out.println("Novo estado:");
                System.out.println("  0 - Ativo (recebendo inscricoes)");
                System.out.println("  1 - Ativo (inscricoes encerradas)");
                System.out.println("  2 - Concluido");
                System.out.println("  3 - Cancelado");
                System.out.print("Escolha: ");
                
                int estado;
                try {
                    estado = Integer.parseInt(console.nextLine().trim());
                    if (estado < 0 || estado > 3) {
                        System.out.println("Estado invalido. Mantendo o anterior.");
                        estado = cursoExistente.getEstado();
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Entrada invalida. Mantendo estado anterior.");
                    estado = cursoExistente.getEstado();
                }

                Curso editado = new Curso(id, idUsuarioLogado, nome, desc,
                    cursoExistente.getDataInicio(), cursoExistente.getCodigo(), estado);

                if (controle.atualizarCurso(editado)) {
                    System.out.println("Curso atualizado com sucesso!");
                }
            } else {
                System.out.println("Erro: Curso nao encontrado ou acesso negado.");
            }
        } catch (Exception e) {
            System.out.println("Erro na atualizacao: " + e.getMessage());
        }
    }

    public void telaExclusao(int idUsuarioLogado) {
        System.out.print("Digite o ID do curso que deseja excluir: ");
        int id;
        try {
            id = Integer.parseInt(console.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("ID invalido.");
            return;
        }

        try {
            Curso c = controle.buscarCurso(id);
            if (c != null && c.getIdUsuario() == idUsuarioLogado) {
                if (controle.excluirCurso(id)) {
                    System.out.println("Curso removido com sucesso!");
                }
            } else {
                System.out.println("Erro: Curso nao encontrado ou sem permissao para excluir.");
            }
        } catch (Exception e) {
            System.out.println("Erro na exclusao: " + e.getMessage());
        }
    }
}
