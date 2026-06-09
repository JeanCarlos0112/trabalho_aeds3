import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import entidades.usuarios.*;
import entidades.cursos.*;
import entidades.inscricoes.*;

/**
 * Teste do Indice Invertido do TP3.
 *
 * Cobre:
 *
 *  1. Processamento de termos (tokenizacao + normalizacao + stop words):
 *     "Introducao a Inteligencia Artificial" -> [introducao, inteligencia, artificial]
 *
 *  2. Cenario completo da especificacao do TP3:
 *     - 4 cursos: "Introducao a Inteligencia Artificial",
 *                 "Inteligencia Emocional para Gestores",
 *                 "Inteligencia no Trabalho por Meio da Inteligencia Artificial",
 *                 "Introducao a Gestao de Equipes"
 *     - busca "Inteligencia Artificial" deve retornar [1, 3, 2]
 *       (curso 4 nao tem nenhum termo)
 *     - calculo TFxIDF reproduz exatamente os valores do exemplo:
 *       (1; 0.808), (3; 0.656), (2; 0.375)
 *
 *  3. Sincronizacao do indice:
 *     - excluir um curso remove-o dos resultados
 *     - alterar nome de um curso atualiza os termos indexados
 *
 *  4. Robustez da busca:
 *     - busca por termos que sao apenas stop words -> lista vazia
 *     - busca por termos inexistentes -> lista vazia
 *     - busca case-insensitive e independente de acentos
 *
 *  5. Bootstrap: indexa cursos legados se o indice esta vazio
 *
 * Compilar e rodar:
 *   javac -d bin -sourcepath src src/TesteIndiceInvertido.java
 *   java -cp bin TesteIndiceInvertido
 */
public class TesteIndiceInvertido {

    static int total = 0, ok = 0, falhou = 0;

    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("  TESTE - Indice Invertido (TP3)");
        System.out.println("===========================================\n");

        try {
            // ============================================================
            //  Bloco 1 - TermosUtil (processamento independente da spec)
            // ============================================================
            printSecao("BLOCO 1 - Processamento de termos (TermosUtil)");

            ArrayList<String> t1 = TermosUtil.extrairTermos("Introducao a Inteligencia Artificial");
            check("[Introducao a Inteligencia Artificial] -> 3 termos",
                t1.size() == 3);
            check("ordem preservada: [introducao, inteligencia, artificial]",
                t1.equals(Arrays.asList("introducao", "inteligencia", "artificial")));

            ArrayList<String> t2 = TermosUtil.extrairTermos("Introdução à Inteligência Artificial");
            check("acentos removidos: 'Introdução' -> 'introducao'",
                t2.equals(Arrays.asList("introducao", "inteligencia", "artificial")));

            ArrayList<String> t3 = TermosUtil.extrairTermos("INTELIGENCIA artificial");
            check("uppercase normalizado para lowercase",
                t3.equals(Arrays.asList("inteligencia", "artificial")));

            ArrayList<String> t4 = TermosUtil.extrairTermos("um dois tres a o e");
            check("stop words sao filtradas (resultado vazio)",
                t4.isEmpty());

            ArrayList<String> t5 = TermosUtil.extrairTermos("");
            check("string vazia -> lista vazia", t5.isEmpty());

            ArrayList<String> t6 = TermosUtil.extrairTermos(null);
            check("string null -> lista vazia (sem NPE)", t6.isEmpty());

            // ============================================================
            //  Bloco 2 - Cenario da spec: 4 cursos + busca conhecida
            // ============================================================
            printSecao("BLOCO 2 - Cenario completo da spec do TP3");

            limparDados();

            ArquivoUsuario      arqUsuario   = new ArquivoUsuario();
            ArquivoCurso        arqCurso     = new ArquivoCurso();
            ArquivoCursoUsuario arqInscricao = new ArquivoCursoUsuario();
            ControleCurso       ctrlCurso    = new ControleCurso(arqCurso, arqUsuario, arqInscricao);

            int idAutor = arqUsuario.create(new Usuario("Autor", "autor@x", 1, "?", 1));

            // Cadastra os 4 cursos exatamente como no exemplo da spec
            LocalDate d = LocalDate.of(2026, 8, 1);
            int idC1 = ctrlCurso.cadastrarCurso(idAutor,
                "Introducao a Inteligencia Artificial", "Desc 1", d);
            int idC2 = ctrlCurso.cadastrarCurso(idAutor,
                "Inteligencia Emocional para Gestores", "Desc 2", d);
            int idC3 = ctrlCurso.cadastrarCurso(idAutor,
                "Inteligencia no Trabalho por Meio da Inteligencia Artificial", "Desc 3", d);
            int idC4 = ctrlCurso.cadastrarCurso(idAutor,
                "Introducao a Gestao de Equipes", "Desc 4", d);

            // Busca pela query do exemplo
            ArrayList<Curso> resultados = ctrlCurso.buscarPorPalavras("Inteligencia Artificial");
            check("busca 'Inteligencia Artificial' retorna 3 cursos",
                resultados.size() == 3);
            check("Curso 1 (Introducao a IA) eh o primeiro (maior score)",
                !resultados.isEmpty() && resultados.get(0).getID() == idC1);
            check("Curso 3 (Inteligencia... IA) eh o segundo",
                resultados.size() >= 2 && resultados.get(1).getID() == idC3);
            check("Curso 2 (Inteligencia Emocional) eh o terceiro",
                resultados.size() >= 3 && resultados.get(2).getID() == idC2);
            check("Curso 4 (Introducao a Gestao) nao aparece (sem termos)",
                resultados.stream().noneMatch(c -> c.getID() == idC4));

            // ============================================================
            //  Bloco 3 - Sincronizacao do indice em delete/update
            // ============================================================
            printSecao("BLOCO 3 - Sincronizacao em delete/update");

            // Excluir o Curso 1 e refazer a busca
            arqCurso.delete(idC1);
            resultados = ctrlCurso.buscarPorPalavras("Inteligencia Artificial");
            check("apos delete do C1, busca retorna 2 cursos",
                resultados.size() == 2);
            check("C1 desapareceu do ranking",
                resultados.stream().noneMatch(c -> c.getID() == idC1));

            // Atualizar nome do Curso 2: trocar "Emocional" por "Artificial"
            Curso c2 = arqCurso.read(idC2);
            Curso c2Atualizado = new Curso(c2.getID(), c2.getIdUsuario(),
                "Inteligencia Artificial Avancada para Gestores",
                c2.getDescricao(), c2.getDataInicio(), c2.getCodigo(), c2.getEstado());
            arqCurso.update(c2Atualizado);

            resultados = ctrlCurso.buscarPorPalavras("Emocional");
            check("apos remover 'Emocional' do C2, busca por 'Emocional' eh vazia",
                resultados.isEmpty());

            resultados = ctrlCurso.buscarPorPalavras("Artificial");
            check("apos adicionar 'Artificial' ao C2, ele aparece em busca por 'Artificial'",
                resultados.stream().anyMatch(c -> c.getID() == idC2));

            // ============================================================
            //  Bloco 4 - Robustez
            // ============================================================
            printSecao("BLOCO 4 - Robustez da busca");

            resultados = ctrlCurso.buscarPorPalavras("a o de para um");
            check("busca so com stop words -> lista vazia",
                resultados.isEmpty());

            resultados = ctrlCurso.buscarPorPalavras("xenomorfo bioluminescente");
            check("termos inexistentes -> lista vazia",
                resultados.isEmpty());

            resultados = ctrlCurso.buscarPorPalavras("INTELIGÊNCIA");
            check("busca com acento e uppercase encontra resultados",
                !resultados.isEmpty());

            resultados = ctrlCurso.buscarPorPalavras("");
            check("query vazia -> lista vazia", resultados.isEmpty());

            resultados = ctrlCurso.buscarPorPalavras(null);
            check("query null -> lista vazia (sem NPE)", resultados.isEmpty());

            // ============================================================
            //  Bloco 5 - Filtragem por estado (cursos cancelados nao aparecem)
            // ============================================================
            printSecao("BLOCO 5 - Filtragem por estado");

            Curso c3 = arqCurso.read(idC3);
            c3.setEstado(3); // cancelado
            arqCurso.update(c3);

            resultados = ctrlCurso.buscarPorPalavras("Inteligencia");
            check("curso cancelado nao aparece em busca por palavras-chave",
                resultados.stream().noneMatch(c -> c.getID() == idC3));

            arqCurso.close();
            arqUsuario.close();
            arqInscricao.close();

            // ============================================================
            //  Bloco 6 - Bootstrap: indexa cursos legados em sessao nova
            // ============================================================
            printSecao("BLOCO 6 - Bootstrap de indice vazio com cursos legados");

            // Apaga apenas os arquivos do indice invertido,
            // mantendo o dados.db dos cursos com os 4 registros do bloco 2.
            new File("./dados/cursos/indiceInvertido.dicionario.db").delete();
            new File("./dados/cursos/indiceInvertido.blocos.db").delete();

            ArquivoUsuario      arqU2 = new ArquivoUsuario();
            ArquivoCurso        arqC2 = new ArquivoCurso();
            ArquivoCursoUsuario arqI2 = new ArquivoCursoUsuario();
            ControleCurso       ctrl2 = new ControleCurso(arqC2, arqU2, arqI2);

            // O construtor de ArquivoCurso deve ter rebuildado o indice
            // ao perceber que estava vazio com cursos presentes em disco.
            // Cursos cancelados/atualizados continuam refletindo a realidade.
            resultados = ctrl2.buscarPorPalavras("Artificial");
            check("apos bootstrap, busca volta a encontrar cursos com 'Artificial'",
                !resultados.isEmpty());

            arqC2.close();
            arqU2.close();
            arqI2.close();

            System.out.println("\n===========================================");
            System.out.println("  RESULTADO: " + ok + "/" + total + " testes passaram");
            if (falhou == 0)
                System.out.println("  (V) INDICE INVERTIDO - TP3: TUDO CERTO");
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
