package entidades.cursos;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Visão de Cursos — menu e telas de entrada/saída.
 *
 * Fluxo conforme PROPOSTA.md:
 *   - menuCurso: lista cursos do usuário em ORDEM ALFABÉTICA com
 *     número sequencial; digitar o número abre a tela de detalhe;
 *     (A) cria novo curso; (R) retorna.
 *   - telaDetalheCurso: exibe todos os dados e as opções A–E:
 *       (A) Gerenciar inscritos  [placeholder TP2]
 *       (B) Corrigir dados do curso
 *       (C) Encerrar inscrições  (estado 0 -> 1)
 *       (D) Concluir curso       (estado 0/1 -> 2)
 *       (E) Cancelar curso
 *       (R) Retornar
 */
public class VisaoCurso {
    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private ControleCurso controle;
    private Scanner console;

    public VisaoCurso(ControleCurso controle, Scanner console) {
        this.controle = controle;
        this.console = console;
    }

    /**
     * Menu do Curso
     * @param idUsuarioLogado - id do usuario usando o sistema no momento
     * @return void
     */
    public void menuCurso(int idUsuarioLogado) {
        while (true) {
            ArrayList<Curso> cursos;
            try {
                cursos = controle.listarCursosOrdenados(idUsuarioLogado);
            } catch (Exception e) {
                System.out.println("Erro ao listar cursos: " + e.getMessage());
                return;
            }

            System.out.println("\nG12 TP1 1.2");
            System.out.println("--------------");
            System.out.println("> Inicio > Meus Cursos\n");
            System.out.println("CURSOS");

            if (cursos.isEmpty()) {
                System.out.println("(Nenhum curso cadastrado.)");
            } else {
                for (int i = 0; i < cursos.size(); i++) {
                    Curso c = cursos.get(i);
                    System.out.println("(" + (i + 1) + ") " + c.getNome()
                        + " - " + c.getDataInicio().format(FMT_DATA));
                }
            }

            System.out.println();
            System.out.println("(A) Novo curso");
            System.out.println("(R) Retornar ao menu anterior");
            System.out.print("\nOpcao: ");

            String opcao = console.nextLine().trim();
            if (opcao.isEmpty()) continue;

            if (opcao.equalsIgnoreCase("A")) {
                telaNovoCurso(idUsuarioLogado);
            } else if (opcao.equalsIgnoreCase("R")) {
                return;
            } else {
                int num;
                try {
                    num = Integer.parseInt(opcao);
                } catch (NumberFormatException e) {
                    System.out.println("Opcao invalida.");
                    continue;
                }
                if (num >= 1 && num <= cursos.size()) {
                    telaDetalheCurso(cursos.get(num - 1).getID());
                } else {
                    System.out.println("Numero fora do intervalo.");
                }
            }
        }
    }

    /**
     * Menu do Novo Curso
     * @param idUsuarioLogado - id do usuario usando o sistema no momento
     * @return void
     */
    public void telaNovoCurso(int idUsuarioLogado) {
        System.out.println("\nG12 TP1 1.2");
        System.out.println("--------------");
        System.out.println("> Inicio > Meus Cursos > Novo Curso\n");

        System.out.print("Nome do curso: ");
        String nome = console.nextLine();

        System.out.print("Descricao (programa, dias, locais, ...): ");
        String descricao = console.nextLine();

        try {
            int idGerado = controle.cadastrarCurso(idUsuarioLogado, nome, descricao);
            System.out.println("\nCurso cadastrado com sucesso! (ID interno: " + idGerado + ")");
        } catch (Exception e) {
            System.out.println("Erro ao cadastrar: " + e.getMessage());
        }
    }
   
    /**
     * Menu de detalhe do Curso
     * @param idUsuarioLogado - id do usuario usando o sistema no momento
     * @return void
     */
    public void telaDetalheCurso(int idCurso) {
        while (true) {
            Curso c;
            try {
                c = controle.buscarCurso(idCurso);
            } catch (Exception e) {
                System.out.println("Erro ao ler curso: " + e.getMessage());
                return;
            }
            if (c == null) {
                System.out.println("Curso nao encontrado (pode ter sido removido).");
                return;
            }

            System.out.println("\nG12 TP1 1.2");
            System.out.println("--------------");
            System.out.println("> Inicio > Meus Cursos > " + c.getNome() + "\n");

            System.out.println("CODIGO........: " + c.getCodigo());
            System.out.println("NOME..........: " + c.getNome());
            System.out.println("DESCRICAO.....: " + c.getDescricao());
            System.out.println("DATA DE INICIO: " + c.getDataInicio().format(FMT_DATA));
            System.out.println("ESTADO........: " + c.getEstadoTexto());

            System.out.println();
            // Mensagem contextual por estado
            switch (c.getEstado()) {
                case 0: System.out.println("Este curso esta aberto para inscricoes!"); break;
                case 1: System.out.println("Este curso ja nao aceita novas inscricoes."); break;
                case 2: System.out.println("Este curso ja foi concluido."); break;
                case 3: System.out.println("Este curso foi cancelado."); break;
            }
            System.out.println();

            System.out.println("(A) Gerenciar inscritos no curso");
            System.out.println("(B) Corrigir dados do curso");
            if (c.getEstado() == 0) {
                System.out.println("(C) Encerrar inscricoes");
            }
            if (c.getEstado() == 0 || c.getEstado() == 1) {
                System.out.println("(D) Concluir curso");
            }
            if (c.getEstado() != 3 && c.getEstado() != 2) {
                System.out.println("(E) Cancelar curso");
            }
            System.out.println("(R) Retornar ao menu anterior");
            System.out.print("\nOpcao: ");

            String opcao = console.nextLine().trim().toUpperCase();
            try {
                switch (opcao) {
                    case "A":
                        System.out.println("\n(Funcionalidade de gerenciamento de inscritos sera implementada no TP2.)");
                        break;
                    case "B":
                        telaCorrecaoDados(c);
                        break;
                    case "C":
                        if (c.getEstado() == 0) {
                            c.setEstado(1);
                            if (controle.atualizarCurso(c))
                                System.out.println("\nInscricoes encerradas com sucesso.");
                        } else {
                            System.out.println("Operacao nao permitida para o estado atual.");
                        }
                        break;
                    case "D":
                        if (c.getEstado() == 0 || c.getEstado() == 1) {
                            c.setEstado(2);
                            if (controle.atualizarCurso(c))
                                System.out.println("\nCurso marcado como concluido.");
                        } else {
                            System.out.println("Operacao nao permitida para o estado atual.");
                        }
                        break;
                    case "E":
                        if (c.getEstado() == 2 || c.getEstado() == 3) {
                            System.out.println("Curso ja encerrado; nao pode ser cancelado novamente.");
                            break;
                        }
                        System.out.print("Confirma o cancelamento do curso? (S/N): ");
                        String conf = console.nextLine().trim();
                        if (conf.equalsIgnoreCase("S")) {
                            if (controle.excluirCurso(c.getID())) {
                                System.out.println("\nCurso cancelado e removido do sistema (nenhum inscrito).");
                                return;
                            } else {
                                System.out.println("Falha ao cancelar o curso.");
                            }
                        } else {
                            System.out.println("Cancelamento abortado.");
                        }
                        break;
                    case "R":
                        return;
                    default:
                        System.out.println("Opcao invalida.");
                }
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }
    }

    /**
     * Menu de Correção do Curso
     * @param idUsuarioLogado - id do usuario usando o sistema no momento
     * @return void
     */
    private void telaCorrecaoDados(Curso c) {
        System.out.println("\nG12 TP1 1.2");
        System.out.println("--------------");
        System.out.println("> Inicio > Meus Cursos > " + c.getNome() + " > Corrigir Dados\n");
        System.out.println("(Deixe em branco para manter o valor atual.)\n");

        System.out.print("Nome (" + c.getNome() + "): ");
        String nome = console.nextLine();
        if (nome.trim().isEmpty()) nome = c.getNome();

        System.out.print("Descricao atual: " + c.getDescricao() + "\n");
        System.out.print("Nova descricao: ");
        String desc = console.nextLine();
        if (desc.trim().isEmpty()) desc = c.getDescricao();

        System.out.print("Data de inicio (" + c.getDataInicio().format(FMT_DATA) + ") [dd/MM/yyyy]: ");
        String dataStr = console.nextLine().trim();
        java.time.LocalDate data = c.getDataInicio();
        if (!dataStr.isEmpty()) {
            try {
                data = java.time.LocalDate.parse(dataStr, FMT_DATA);
            } catch (Exception e) {
                System.out.println("Data invalida. Mantendo a anterior.");
            }
        }

        Curso editado = new Curso(c.getID(), c.getIdUsuario(), nome, desc, data,
                                  c.getCodigo(), c.getEstado());
        try {
            if (controle.atualizarCurso(editado))
                System.out.println("\nDados do curso atualizados.");
            else
                System.out.println("\nFalha ao atualizar.");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}
