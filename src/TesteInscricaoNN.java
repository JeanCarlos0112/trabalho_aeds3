import java.io.File;
import java.util.ArrayList;
import entidades.usuarios.*;
import entidades.cursos.*;
import entidades.inscricoes.*;
import entidades.inscricoes.ControleInscricao.CursoComInscricao;
import entidades.inscricoes.ControleInscricao.UsuarioComInscricao;

/**
 * Teste do Relacionamento N:N do TP2 (entidade CursoUsuario).
 *
 * Cobre:
 *  - CRUD de CursoUsuario via ArquivoCursoUsuario
 *  - Sincronizacao das duas Arvores B+ em create/delete
 *  - Consulta por curso (lista inscritos) e por usuario (lista cursos)
 *  - Regras de negocio do ControleInscricao:
 *      * curso inexistente
 *      * curso fora do estado 0
 *      * dono nao se inscreve no proprio curso
 *      * inscricao dupla
 *  - Integridade referencial:
 *      * cancelar curso cancela inscricoes em cascata
 *      * excluir conta de usuario cancela inscricoes dele em outros cursos
 *  - Cancelar inscricao individual (do aluno ou do dono)
 *  - Exportacao CSV com escape de virgulas e aspas
 *
 * Compilar e rodar:
 *   javac -d bin -sourcepath src src/TesteInscricaoNN.java
 *   java -cp bin TesteInscricaoNN
 */
public class TesteInscricaoNN {

    static int total = 0, ok = 0, falhou = 0;

    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("  TESTE - Relacionamento N:N (TP2)");
        System.out.println("===========================================\n");

        try {
            limparDados();

            ArquivoUsuario      arqUsuario   = new ArquivoUsuario();
            ArquivoCurso        arqCurso     = new ArquivoCurso();
            ArquivoCursoUsuario arqInscricao = new ArquivoCursoUsuario();

            ControleCurso     ctrlCurso     = new ControleCurso(arqCurso, arqUsuario, arqInscricao);
            ControleUsuario   ctrlUsuario   = new ControleUsuario(arqUsuario, arqCurso, arqInscricao);
            ControleInscricao ctrlInscricao = new ControleInscricao(arqInscricao, arqCurso, arqUsuario);

            // SETUP - 3 usuarios e 3 cursos
            printSecao("SETUP - usuarios e cursos");
            int idAlice = arqUsuario.create(new Usuario("Alice",  "alice@x", 1, "?", 1));
            int idBob   = arqUsuario.create(new Usuario("Bob",    "bob@x",   1, "?", 1));
            int idCarol = arqUsuario.create(new Usuario("Carol",  "carol@x", 1, "?", 1));
            int idCursoAlice = ctrlCurso.cadastrarCurso(idAlice, "Python", "Curso Python");
            int idCursoBob   = ctrlCurso.cadastrarCurso(idBob,   "Java",   "Curso Java");
            int idCursoCarol = ctrlCurso.cadastrarCurso(idCarol, "Rust",   "Curso Rust");
            System.out.println("  Alice=" + idAlice + " curso=" + idCursoAlice);
            System.out.println("  Bob="   + idBob   + " curso=" + idCursoBob);
            System.out.println("  Carol=" + idCarol + " curso=" + idCursoCarol);

            // TESTE 1 - inscricao basica funciona
            printSecao("TESTE 1 - Inscricao basica");
            int statusOk = ctrlInscricao.inscrever(idCursoAlice, idBob);
            check("Bob inscreve no curso da Alice -> OK_INSCRITO",
                statusOk == ControleInscricao.OK_INSCRITO);
            check("estaInscrito(cursoAlice, Bob) = true",
                ctrlInscricao.estaInscrito(idCursoAlice, idBob));
            check("contarInscritos(cursoAlice) = 1",
                ctrlInscricao.contarInscritos(idCursoAlice) == 1);

            // TESTE 2 - regras de negocio
            printSecao("TESTE 2 - Regras de negocio na inscricao");
            check("dono no proprio curso -> ERRO_DONO",
                ctrlInscricao.inscrever(idCursoAlice, idAlice)
                    == ControleInscricao.ERRO_DONO_INSCREVENDO_NO_PROPRIO);
            check("inscricao duplicada -> ERRO_JA_INSCRITO",
                ctrlInscricao.inscrever(idCursoAlice, idBob)
                    == ControleInscricao.ERRO_JA_INSCRITO);
            check("curso inexistente -> ERRO_CURSO_INEXISTENTE",
                ctrlInscricao.inscrever(99999, idBob)
                    == ControleInscricao.ERRO_CURSO_INEXISTENTE);

            // encerra inscricoes do curso da Alice -> estado 1
            Curso cursoAlice = arqCurso.read(idCursoAlice);
            cursoAlice.setEstado(1);
            arqCurso.update(cursoAlice);
            check("curso fora do estado 0 -> ERRO_CURSO_NAO_DISPONIVEL",
                ctrlInscricao.inscrever(idCursoAlice, idCarol)
                    == ControleInscricao.ERRO_CURSO_NAO_DISPONIVEL);
            // restaura estado 0 para o resto dos testes
            cursoAlice.setEstado(0);
            arqCurso.update(cursoAlice);

            // TESTE 3 - multiplas inscricoes
            printSecao("TESTE 3 - Multiplas inscricoes (N:N de verdade)");
            ctrlInscricao.inscrever(idCursoAlice, idCarol);  // Carol -> Alice
            ctrlInscricao.inscrever(idCursoBob,   idAlice);  // Alice -> Bob
            ctrlInscricao.inscrever(idCursoBob,   idCarol);  // Carol -> Bob

            check("cursoAlice tem 2 inscritos (Bob + Carol)",
                ctrlInscricao.contarInscritos(idCursoAlice) == 2);
            check("cursoBob tem 2 inscritos (Alice + Carol)",
                ctrlInscricao.contarInscritos(idCursoBob) == 2);
            check("cursoCarol tem 0 inscritos",
                ctrlInscricao.contarInscritos(idCursoCarol) == 0);

            ArrayList<CursoComInscricao> cursosCarol = ctrlInscricao.listarMinhasInscricoes(idCarol);
            check("Carol esta em 2 cursos (Alice + Bob)",
                cursosCarol.size() == 2);

            ArrayList<UsuarioComInscricao> inscritosAlice = ctrlInscricao.listarInscritos(idCursoAlice);
            check("Inscritos no curso da Alice estao em ordem alfabetica",
                inscritosAlice.size() == 2
                && inscritosAlice.get(0).usuario.getNome().equals("Bob")
                && inscritosAlice.get(1).usuario.getNome().equals("Carol"));

            // TESTE 4 - cancelamento individual
            printSecao("TESTE 4 - Cancelar inscricao individual");
            boolean cancelado = ctrlInscricao.cancelarInscricaoCursoUsuario(idCursoAlice, idBob);
            check("cancelar Bob do curso da Alice retorna true", cancelado);
            check("Bob nao esta mais inscrito",
                !ctrlInscricao.estaInscrito(idCursoAlice, idBob));
            check("curso da Alice agora tem 1 inscrito (so Carol)",
                ctrlInscricao.contarInscritos(idCursoAlice) == 1);
            check("Bob ainda esta inscrito no curso do Bob? NAO (so se inscreve em terceiros)",
                !ctrlInscricao.estaInscrito(idCursoBob, idBob));

            // TESTE 5 - cascata ao cancelar curso
            printSecao("TESTE 5 - Cascata: cancelar curso remove inscricoes");
            int totalAntes = ctrlInscricao.contarInscritos(idCursoBob);
            check("cursoBob tem " + totalAntes + " inscritos antes do cancelamento",
                totalAntes > 0);
            ctrlCurso.excluirCurso(idCursoBob);
            check("apos cancelar cursoBob, suas inscricoes desapareceram",
                arqInscricao.readByCurso(idCursoBob).isEmpty());
            check("Alice nao tem mais a inscricao no cursoBob",
                !ctrlInscricao.estaInscrito(idCursoBob, idAlice));

            // TESTE 6 - cascata ao excluir conta
            printSecao("TESTE 6 - Cascata: excluir conta cancela inscricoes do usuario");
            // Carol estava inscrita no curso da Alice. Carol tambem TEM um
            // curso ativo (Rust). Exclusao bloqueia.
            check("Carol tem curso ativo -> exclusao bloqueada",
                !ctrlUsuario.excluirUsuario(idCarol));
            // Cancela o curso da Carol primeiro (vira estado 3)
            Curso cursoCarol = arqCurso.read(idCursoCarol);
            cursoCarol.setEstado(3);
            arqCurso.update(cursoCarol);
            // Agora pode excluir
            check("Carol sem cursos ativos -> exclusao permitida",
                ctrlUsuario.excluirUsuario(idCarol));
            check("apos excluir Carol, ela sumiu das inscricoes do curso da Alice",
                arqInscricao.readByUsuario(idCarol).isEmpty());
            check("curso da Alice agora tem 0 inscritos (Carol cascateou)",
                ctrlInscricao.contarInscritos(idCursoAlice) == 0);

            // TESTE 7 - exportacao CSV
            printSecao("TESTE 7 - Exportacao CSV");
            int idDanCom = arqUsuario.create(new Usuario("Dan, Junior", "dan@\"x\"", 1, "?", 1));
            int idErica  = arqUsuario.create(new Usuario("Erica",       "erica@x",  1, "?", 1));
            ctrlInscricao.inscrever(idCursoAlice, idDanCom);
            ctrlInscricao.inscrever(idCursoAlice, idErica);

            String csv = ctrlInscricao.exportarCSV(idCursoAlice);
            check("CSV comeca com header correto",
                csv.startsWith("Nome,Email,DataInscricao\n"));
            check("CSV escapa virgula no nome ('Dan, Junior' -> com aspas)",
                csv.contains("\"Dan, Junior\""));
            check("CSV escapa aspas no email (dan@\"x\" -> dan@\"\"x\"\")",
                csv.contains("\"dan@\"\"x\"\"\""));
            check("CSV contem ambos inscritos",
                csv.contains("Erica") && csv.contains("Dan, Junior"));

            // TESTE 8 - resincronizacao de indices apos cascata
            printSecao("TESTE 8 - Indices B+ consistentes apos operacoes em cascata");
            // Procura por curso (que foi deletado) - deve retornar vazio
            check("indice por curso deletado vazio (apos cascata do teste 5)",
                arqInscricao.readByCurso(idCursoBob).isEmpty());
            // E Carol foi deletada - usuario tambem vazio
            check("indice por usuario deletado vazio (apos cascata do teste 6)",
                arqInscricao.readByUsuario(idCarol).isEmpty());

            arqInscricao.close();
            arqCurso.close();
            arqUsuario.close();

            System.out.println("\n===========================================");
            System.out.println("  RESULTADO: " + ok + "/" + total + " testes passaram");
            if (falhou == 0)
                System.out.println("  (V) RELACIONAMENTO N:N - TP2: TUDO CERTO");
            else
                System.out.println("  (X) " + falhou + " testes falharam");
            System.out.println("===========================================");

        } catch (Exception e) {
            System.out.println("ERRO FATAL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static void printSecao(String s) {
        System.out.println("\n-------------------------------------------");
        System.out.println("  " + s);
        System.out.println("-------------------------------------------");
    }

    static void check(String descricao, boolean condicao) {
        total++;
        if (condicao) { ok++; System.out.println("    (V) " + descricao); }
        else          { falhou++; System.out.println("    (X) " + descricao); }
    }

    static void limparDados() {
        File d = new File("./dados");
        if (d.exists()) apagaRecursivo(d);
    }

    static void apagaRecursivo(File f) {
        if (f.isDirectory()) {
            File[] filhos = f.listFiles();
            if (filhos != null) for (File c : filhos) apagaRecursivo(c);
        }
        f.delete();
    }
}
