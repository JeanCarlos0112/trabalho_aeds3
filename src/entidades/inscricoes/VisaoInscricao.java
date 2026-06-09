package entidades.inscricoes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

import entidades.cursos.Curso;
import entidades.cursos.VisaoCurso;
import entidades.inscricoes.ControleInscricao.CursoComInscricao;
import entidades.inscricoes.ControleInscricao.UsuarioComInscricao;

/**
 * Visao das inscricoes do TP2.
 *
 * Telas:
 *  - menuMinhasInscricoes      ponto de entrada do menu "Minhas Inscricoes"
 *                              (do menu logado, opcao C). Lista as inscricoes
 *                              atuais do usuario no topo e oferece busca por
 *                              codigo, listagem de cursos disponiveis e
 *                              acesso ao detalhe de cada inscricao.
 *  - telaDetalheMinhaInscricao  detalhe de uma inscricao do ponto de vista
 *                              do aluno, com opcao de cancelar.
 *  - telaGerenciarInscritos    acessada de Meus Cursos > curso > (A); mostra
 *                              os inscritos numerados e permite exportar CSV
 *                              ou acessar o detalhe de cada inscrito.
 *  - telaDetalheInscrito        detalhe de um inscrito do ponto de vista do
 *                              dono do curso, com opcao de cancelar a inscricao
 *                              dele.
 *
 * As telas de busca por codigo e listagem paginada de cursos continuam
 * em VisaoCurso e sao delegadas a partir deste menu.
 */
public class VisaoInscricao {

    private static final DateTimeFormatter FMT_DATA =
        DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ControleInscricao controle;
    private final Scanner console;

    // VisaoCurso e injetada via setter para evitar dependencia circular
    // (VisaoCurso tambem precisa chamar VisaoInscricao para "Gerenciar
    // inscritos" do menu Meus Cursos).
    private VisaoCurso visaoCurso;

    public VisaoInscricao(ControleInscricao controle, Scanner console) {
        this.controle = controle;
        this.console = console;
    }

    public void setVisaoCurso(VisaoCurso visaoCurso) {
        this.visaoCurso = visaoCurso;
    }

    // ============================================================
    //  Menu Minhas Inscricoes (acessado de Logado > C)
    // ============================================================

    /**
     * Menu raiz "Minhas Inscricoes". Lista as inscricoes do usuario
     * no topo (numeradas) e oferece tres acoes de busca conforme
     * a especificacao do TP2.
     */
    public void menuMinhasInscricoes(int idUsuarioLogado) {
        while (true) {
            ArrayList<CursoComInscricao> inscricoes;
            try {
                inscricoes = controle.listarMinhasInscricoes(idUsuarioLogado);
            } catch (Exception e) {
                System.out.println("Erro ao listar suas inscricoes: " + e.getMessage());
                return;
            }

            System.out.println("\nG12 TP1 1.2");
            System.out.println("--------------");
            System.out.println("> Inicio > Minhas Inscricoes\n");

            System.out.println("INSCRICOES");
            if (inscricoes.isEmpty()) {
                System.out.println("(Voce ainda nao esta inscrito em nenhum curso.)");
            } else {
                for (int i = 0; i < inscricoes.size(); i++) {
                    CursoComInscricao ci = inscricoes.get(i);
                    Curso c = ci.curso;
                    String estadoTag = sufixoEstado(c.getEstado());
                    System.out.println("(" + (i + 1) + ") " + c.getNome()
                        + " - " + c.getDataInicio().format(FMT_DATA) + estadoTag);
                }
            }

            System.out.println();
            System.out.println("(A) Buscar curso por codigo");
            System.out.println("(B) Buscar curso por palavras-chave");
            System.out.println("(C) Listar todos os cursos");
            System.out.println();
            System.out.println("(R) Retornar ao menu anterior");
            System.out.print("\nOpcao: ");

            String op = console.nextLine().trim().toUpperCase();
            if (op.isEmpty()) continue;

            switch (op) {
                case "A":
                    if (visaoCurso != null)
                        visaoCurso.telaBuscaPorCodigoInscricao(idUsuarioLogado);
                    break;
                case "B":
                    if (visaoCurso != null)
                        visaoCurso.telaBuscaPorPalavrasInscricao(idUsuarioLogado);
                    break;
                case "C":
                    if (visaoCurso != null)
                        visaoCurso.telaListaCursosInscricao(idUsuarioLogado);
                    break;
                case "R":
                    return;
                default:
                    // Pode ser um numero apontando para uma inscricao da lista
                    int num;
                    try { num = Integer.parseInt(op); }
                    catch (NumberFormatException e) {
                        System.out.println("Opcao invalida.");
                        continue;
                    }
                    if (num >= 1 && num <= inscricoes.size()) {
                        telaDetalheMinhaInscricao(
                            inscricoes.get(num - 1).inscricao.getID(),
                            idUsuarioLogado);
                    } else {
                        System.out.println("Numero fora do intervalo.");
                    }
            }
        }
    }

    private String sufixoEstado(int estado) {
        switch (estado) {
            case 1: return " (INSCRICOES ENCERRADAS)";
            case 2: return " (CURSO CONCLUIDO)";
            case 3: return " (CURSO CANCELADO)";
            default: return "";
        }
    }

    /**
     * Detalhe de uma inscricao do aluno, com opcao de cancelar.
     */
    private void telaDetalheMinhaInscricao(int idCursoUsuario, int idUsuarioLogado) {
        while (true) {
            CursoComInscricao ci = localizarInscricaoDoUsuario(idCursoUsuario, idUsuarioLogado);
            if (ci == null) {
                System.out.println("Inscricao nao encontrada (pode ter sido cancelada).");
                return;
            }

            Curso c = ci.curso;
            String autorNome = visaoCurso != null
                ? visaoCurso.nomeAutor(c.getIdUsuario())
                : "(ID " + c.getIdUsuario() + ")";

            System.out.println("\nG12 TP1 1.2");
            System.out.println("--------------");
            System.out.println("> Inicio > Minhas Inscricoes > " + c.getNome() + "\n");

            System.out.println("CODIGO........: " + c.getCodigo());
            System.out.println("CURSO.........: " + c.getNome());
            System.out.println("AUTOR.........: " + autorNome);
            System.out.println("DESCRICAO.....: " + c.getDescricao());
            System.out.println("DATA DE INICIO: " + c.getDataInicio().format(FMT_DATA));
            System.out.println("INSCRITO EM...: " + ci.inscricao.getDataInscricao().format(FMT_DATA));
            String estadoTag = sufixoEstado(c.getEstado());
            if (!estadoTag.isEmpty())
                System.out.println("STATUS........:" + estadoTag);

            System.out.println();
            System.out.println("(A) Cancelar minha inscricao no curso");
            System.out.println("(R) Retornar ao menu anterior");
            System.out.print("\nOpcao: ");

            String op = console.nextLine().trim().toUpperCase();
            switch (op) {
                case "A":
                    System.out.print("Confirma o cancelamento da inscricao? (S/N): ");
                    String conf = console.nextLine().trim();
                    if (conf.equalsIgnoreCase("S")) {
                        try {
                            if (controle.cancelarInscricao(idCursoUsuario)) {
                                System.out.println("\nInscricao cancelada com sucesso.");
                                return;
                            } else {
                                System.out.println("Falha ao cancelar.");
                            }
                        } catch (Exception e) {
                            System.out.println("Erro ao cancelar: " + e.getMessage());
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
        }
    }

    /**
     * Localiza a inscricao na lista atualizada de inscricoes do usuario,
     * garantindo que ainda pertence ao usuario logado (defesa contra
     * estados inconsistentes).
     */
    private CursoComInscricao localizarInscricaoDoUsuario(int idCursoUsuario,
                                                           int idUsuarioLogado) {
        try {
            ArrayList<CursoComInscricao> atual = controle.listarMinhasInscricoes(idUsuarioLogado);
            for (CursoComInscricao ci : atual) {
                if (ci.inscricao.getID() == idCursoUsuario) return ci;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    // ============================================================
    //  Tela "Gerenciar Inscritos" (acessada de Meus Cursos > curso > A)
    // ============================================================

    /**
     * Lista os inscritos de um curso (visao do dono).
     * Cada inscrito vira uma linha numerada com nome + data de inscricao.
     * Oferece (A) Exportar lista em CSV e selecao por numero para
     * acessar o detalhe do inscrito.
     */
    public void telaGerenciarInscritos(Curso curso) {
        while (true) {
            ArrayList<UsuarioComInscricao> inscritos;
            try {
                inscritos = controle.listarInscritos(curso.getID());
            } catch (Exception e) {
                System.out.println("Erro ao listar inscritos: " + e.getMessage());
                return;
            }

            System.out.println("\nG12 TP1 1.2");
            System.out.println("--------------");
            System.out.println("> Inicio > Meus Cursos > " + curso.getNome() + " > Inscricoes\n");

            if (inscritos.isEmpty()) {
                System.out.println("(Nenhum inscrito neste curso ainda.)");
            } else {
                for (int i = 0; i < inscritos.size(); i++) {
                    UsuarioComInscricao ui = inscritos.get(i);
                    System.out.println("(" + (i + 1) + ") " + ui.usuario.getNome()
                        + " (" + ui.inscricao.getDataInscricao().format(FMT_DATA) + ")");
                }
            }

            System.out.println();
            if (!inscritos.isEmpty()) System.out.println("(A) Exportar lista");
            System.out.println("(R) Retornar ao menu anterior");
            System.out.print("\nOpcao: ");

            String op = console.nextLine().trim().toUpperCase();
            if (op.isEmpty()) continue;

            if (op.equals("A")) {
                if (!inscritos.isEmpty()) exportarLista(curso);
                else System.out.println("Nada para exportar.");
            } else if (op.equals("R")) {
                return;
            } else {
                int num;
                try { num = Integer.parseInt(op); }
                catch (NumberFormatException e) {
                    System.out.println("Opcao invalida.");
                    continue;
                }
                if (num >= 1 && num <= inscritos.size()) {
                    telaDetalheInscrito(curso, inscritos.get(num - 1));
                } else {
                    System.out.println("Numero fora do intervalo.");
                }
            }
        }
    }

    /**
     * Detalhe de um inscrito (visao do dono do curso), com opcao
     * de cancelar a inscricao daquele aluno especifico.
     */
    private void telaDetalheInscrito(Curso curso, UsuarioComInscricao ui) {
        while (true) {
            System.out.println("\nG12 TP1 1.2");
            System.out.println("--------------");
            System.out.println("> Inicio > Meus Cursos > " + curso.getNome()
                + " > Inscricoes > " + ui.usuario.getNome() + "\n");

            System.out.println("NOME..........: " + ui.usuario.getNome());
            System.out.println("EMAIL.........: " + ui.usuario.getEmail());
            System.out.println("INSCRITO EM...: " + ui.inscricao.getDataInscricao().format(FMT_DATA));

            System.out.println();
            System.out.println("(A) Cancelar a inscricao deste aluno");
            System.out.println("(R) Retornar ao menu anterior");
            System.out.print("\nOpcao: ");

            String op = console.nextLine().trim().toUpperCase();
            switch (op) {
                case "A":
                    System.out.print("Confirma o cancelamento da inscricao de "
                        + ui.usuario.getNome() + "? (S/N): ");
                    String conf = console.nextLine().trim();
                    if (conf.equalsIgnoreCase("S")) {
                        try {
                            if (controle.cancelarInscricao(ui.inscricao.getID())) {
                                System.out.println("\nInscricao cancelada com sucesso.");
                                return;
                            } else {
                                System.out.println("Falha ao cancelar.");
                            }
                        } catch (Exception e) {
                            System.out.println("Erro ao cancelar: " + e.getMessage());
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
        }
    }

    /**
     * Exporta a lista de inscritos do curso em CSV para
     * ./exportacoes/inscritos_<codigo>.csv. Cria a pasta se nao existir.
     */
    private void exportarLista(Curso curso) {
        try {
            String csv = controle.exportarCSV(curso.getID());
            File pasta = new File("./exportacoes");
            if (!pasta.exists()) pasta.mkdir();
            File arquivo = new File(pasta, "inscritos_" + curso.getCodigo() + ".csv");
            try (FileWriter w = new FileWriter(arquivo)) {
                w.write(csv);
            }
            System.out.println("\nLista exportada para: " + arquivo.getPath());
        } catch (IOException e) {
            System.out.println("Erro ao escrever arquivo: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Erro ao exportar: " + e.getMessage());
        }
    }
}
