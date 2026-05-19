import java.io.File;
import java.util.ArrayList;
import entidades.usuarios.*;
import entidades.cursos.*;

/**
 * Teste da Busca de Cursos do TP2 (menu Minhas Inscricoes).
 *
 * Cobre:
 *  - cadastro normal de cursos com NanoID gerado
 *  - busca por codigo NanoID (positiva, negativa, codigo inexistente)
 *  - listagem completa filtrada por estado 0 e ordenada por data
 *  - resolucao do autor via ControleCurso.buscarAutor
 *  - sincronizacao do indice de codigo apos delete
 *
 * Compilar e rodar:
 *   javac -d bin -sourcepath src src/TesteBuscaCursos.java
 *   java -cp bin TesteBuscaCursos
 */
public class TesteBuscaCursos {

    static int total = 0, ok = 0, falhou = 0;

    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("  TESTE - Busca de Cursos (TP2)");
        System.out.println("===========================================\n");

        try {
            limparDados();

            ArquivoUsuario arqUsuario = new ArquivoUsuario();
            ArquivoCurso   arqCurso   = new ArquivoCurso();
            ControleCurso  ctrlCurso  = new ControleCurso(arqCurso, arqUsuario);

            // SETUP - usuarios
            printSecao("SETUP - Usuarios");
            int idAlice = arqUsuario.create(new Usuario("Alice",  "alice@puc.br",
                "s1".hashCode(), "?", "r".hashCode()));
            int idBob   = arqUsuario.create(new Usuario("Bob",    "bob@puc.br",
                "s2".hashCode(), "?", "r".hashCode()));
            System.out.println("  Alice id=" + idAlice + ", Bob id=" + idBob);

            // SETUP - cursos via ControleCurso (gera NanoID real)
            printSecao("SETUP - Cursos via ControleCurso");
            int idC1 = ctrlCurso.cadastrarCurso(idAlice, "Python basico",
                "Introducao ao Python.");
            int idC2 = ctrlCurso.cadastrarCurso(idAlice, "Java avancado",
                "Conceitos avancados em Java.");
            int idC3 = ctrlCurso.cadastrarCurso(idBob,   "DevOps essencial",
                "Pipelines e infra como codigo.");
            Curso c1 = arqCurso.read(idC1);
            Curso c2 = arqCurso.read(idC2);
            Curso c3 = arqCurso.read(idC3);
            System.out.println("  " + c1.getNome() + " - codigo " + c1.getCodigo());
            System.out.println("  " + c2.getNome() + " - codigo " + c2.getCodigo());
            System.out.println("  " + c3.getNome() + " - codigo " + c3.getCodigo());

            // TESTE 1 - codigo NanoID tem 10 caracteres
            printSecao("TESTE 1 - NanoID com 10 caracteres");
            check("c1.codigo.length == 10", c1.getCodigo().length() == 10);
            check("c2.codigo.length == 10", c2.getCodigo().length() == 10);
            check("c3.codigo.length == 10", c3.getCodigo().length() == 10);
            check("codigos sao distintos",
                !c1.getCodigo().equals(c2.getCodigo()) &&
                !c1.getCodigo().equals(c3.getCodigo()) &&
                !c2.getCodigo().equals(c3.getCodigo()));

            // TESTE 2 - busca por codigo retorna o curso certo
            printSecao("TESTE 2 - Busca por codigo NanoID");
            Curso achado1 = ctrlCurso.buscarPorCodigo(c1.getCodigo());
            check("buscar c1 por codigo nao retorna null", achado1 != null);
            check("buscar c1 por codigo retorna ID correto",
                achado1 != null && achado1.getID() == idC1);
            Curso achado3 = ctrlCurso.buscarPorCodigo(c3.getCodigo());
            check("buscar c3 por codigo retorna ID correto",
                achado3 != null && achado3.getID() == idC3);

            // TESTE 3 - codigo inexistente retorna null
            printSecao("TESTE 3 - Codigo inexistente");
            check("codigo aleatorio retorna null",
                ctrlCurso.buscarPorCodigo("ZZZZZZZZZZ") == null);
            check("codigo vazio retorna null",
                ctrlCurso.buscarPorCodigo("") == null);
            check("codigo null retorna null",
                ctrlCurso.buscarPorCodigo(null) == null);

            // TESTE 4 - listar todos retorna so estado 0
            printSecao("TESTE 4 - Listar todos os cursos disponiveis");
            ArrayList<Curso> disponiveis = ctrlCurso.listarTodosCursosDisponiveis();
            check("3 cursos disponiveis (todos estado 0)", disponiveis.size() == 3);

            // encerrar inscricoes de c2 -> estado 1, sai da listagem
            c2.setEstado(1);
            arqCurso.update(c2);
            disponiveis = ctrlCurso.listarTodosCursosDisponiveis();
            check("apos encerrar c2, ficam 2 disponiveis", disponiveis.size() == 2);
            check("c2 nao esta na lista",
                disponiveis.stream().noneMatch(x -> x.getID() == idC2));

            // mas a busca por codigo ainda acha c2 (estado 1 nao some)
            Curso c2achado = ctrlCurso.buscarPorCodigo(c2.getCodigo());
            check("busca por codigo ainda encontra c2 (estado 1)",
                c2achado != null && c2achado.getID() == idC2);

            // TESTE 5 - ordenacao por data de inicio
            printSecao("TESTE 5 - Ordenacao por data de inicio");
            // todos os 3 cursos foram criados com LocalDate.now(), mesma data.
            // criamos um curso "futuro" e um "passado" para validar
            int idFuturo = ctrlCurso.cadastrarCurso(idAlice, "Curso Futuro",
                "Comeca depois.");
            Curso futuro = arqCurso.read(idFuturo);
            // empurra a data uns 30 dias pra frente
            Curso futuroAtualizado = new Curso(
                futuro.getID(), futuro.getIdUsuario(), futuro.getNome(),
                futuro.getDescricao(), futuro.getDataInicio().plusDays(30),
                futuro.getCodigo(), futuro.getEstado());
            arqCurso.update(futuroAtualizado);

            int idPassado = ctrlCurso.cadastrarCurso(idAlice, "Curso Passado",
                "Comeca antes.");
            Curso passado = arqCurso.read(idPassado);
            Curso passadoAtualizado = new Curso(
                passado.getID(), passado.getIdUsuario(), passado.getNome(),
                passado.getDescricao(), passado.getDataInicio().minusDays(30),
                passado.getCodigo(), passado.getEstado());
            arqCurso.update(passadoAtualizado);

            disponiveis = ctrlCurso.listarTodosCursosDisponiveis();
            check("primeiro da lista eh 'Curso Passado'",
                disponiveis.get(0).getNome().equals("Curso Passado"));
            check("ultimo da lista eh 'Curso Futuro'",
                disponiveis.get(disponiveis.size() - 1).getNome().equals("Curso Futuro"));

            // TESTE 6 - resolucao do autor
            printSecao("TESTE 6 - Resolucao do autor");
            Usuario autor = ctrlCurso.buscarAutor(idAlice);
            check("buscarAutor(Alice) retorna Alice",
                autor != null && autor.getNome().equals("Alice"));
            Usuario autorInexistente = ctrlCurso.buscarAutor(99999);
            check("buscarAutor de id inexistente retorna null",
                autorInexistente == null);

            // TESTE 7 - sincronizacao do indice de codigo apos delete
            printSecao("TESTE 7 - Indice de codigo sincronizado apos delete");
            String codigoC1 = c1.getCodigo();
            arqCurso.delete(idC1);
            check("apos delete, busca por codigo retorna null",
                ctrlCurso.buscarPorCodigo(codigoC1) == null);

            // mas outras buscas continuam funcionando
            Curso aindaAcha = ctrlCurso.buscarPorCodigo(c3.getCodigo());
            check("outros codigos seguem buscaveis apos delete",
                aindaAcha != null && aindaAcha.getID() == idC3);

            arqCurso.close();
            arqUsuario.close();

            System.out.println("\n===========================================");
            System.out.println("  RESULTADO: " + ok + "/" + total + " testes passaram");
            if (falhou == 0)
                System.out.println("  (V) BUSCA DE CURSOS - TP2: TUDO CERTO");
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
        if (condicao) {
            ok++;
            System.out.println("    (V) " + descricao);
        } else {
            falhou++;
            System.out.println("    (X) " + descricao);
        }
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
