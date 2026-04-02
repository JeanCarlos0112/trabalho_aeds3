import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import entidades.usuarios.*;
import entidades.cursos.*;

/**
 * Teste da implementação do relacionamento 1:N (idUsuario, idCurso)
 * usando Árvore B+ — parte do JEAN.
 * 
 * Para compilar e rodar (a partir da pasta src/):
 *   javac -d ../bin aed3/*.java entidades/usuarios/*.java entidades/cursos/*.java TesteRelacionamento1N.java
 *   cd ../bin
 *   java TesteRelacionamento1N
 * 
 * Ou se preferirem tudo na mesma pasta:
 *   javac aed3/*.java entidades/usuarios/*.java entidades/cursos/*.java TesteRelacionamento1N.java
 *   java TesteRelacionamento1N
 */
public class TesteRelacionamento1N {

    static int totalTestes = 0;
    static int passou = 0;
    static int falhou = 0;

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  TESTE - Relacionamento 1:N com Arvore B+ (ParIdId)      ║");
        System.out.println("║  Responsavel: JEAN                                       ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");

        try {
            limparDados();

            ArquivoUsuario arqUsuario = new ArquivoUsuario();
            ArquivoCurso arqCurso = new ArquivoCurso();

            // ============================================================
            //  SETUP: Criar usuários de teste
            // ============================================================
            printSecao("SETUP - Criando usuários de teste");

            Usuario u1 = new Usuario("Alice", "alice@puc.br", "senha1".hashCode(), "Cor?", "azul".hashCode());
            Usuario u2 = new Usuario("Bob", "bob@puc.br", "senha2".hashCode(), "Pet?", "gato".hashCode());
            Usuario u3 = new Usuario("Carol", "carol@puc.br", "senha3".hashCode(), "Cidade?", "bh".hashCode());

            int idAlice = arqUsuario.create(u1);
            int idBob   = arqUsuario.create(u2);
            int idCarol = arqUsuario.create(u3);

            System.out.println("  Usuario Alice criado - ID: " + idAlice);
            System.out.println("  Usuario Bob   criado - ID: " + idBob);
            System.out.println("  Usuario Carol criado - ID: " + idCarol);

            // ============================================================
            //  TESTE 1: Criar cursos vinculados a usuários
            // ============================================================
            printSecao("TESTE 1 - Criar cursos vinculados (create mantem indice B+)");

            // Alice: 3 cursos
            Curso c1 = new Curso(idAlice, "Python basico",      "Curso de Python",     LocalDate.of(2026,5,20), "abc1234567", 0);
            Curso c2 = new Curso(idAlice, "Java avancado",      "Curso de Java",       LocalDate.of(2026,6,10), "def1234567", 0);
            Curso c3 = new Curso(idAlice, "Banco de Dados",     "Curso de BD",         LocalDate.of(2026,7,1),  "ghi1234567", 0);

            // Bob: 2 cursos
            Curso c4 = new Curso(idBob,   "Financas pessoais",  "Curso de financas",   LocalDate.of(2026,2,10), "jkl1234567", 0);
            Curso c5 = new Curso(idBob,   "Arduino basico",     "Curso de Arduino",    LocalDate.of(2026,4,15), "mno1234567", 0);

            // Carol: 1 curso
            Curso c6 = new Curso(idCarol, "Violao para todos",  "Curso de violao",     LocalDate.of(2026,8,5),  "pqr1234567", 0);

            int idC1 = arqCurso.create(c1);
            int idC2 = arqCurso.create(c2);
            int idC3 = arqCurso.create(c3);
            int idC4 = arqCurso.create(c4);
            int idC5 = arqCurso.create(c5);
            int idC6 = arqCurso.create(c6);

            System.out.println("  Cursos criados:");
            System.out.println("    Alice -> Python basico (ID:" + idC1 + "), Java avancado (ID:" + idC2 + "), Banco de Dados (ID:" + idC3 + ")");
            System.out.println("    Bob   -> Financas pessoais (ID:" + idC4 + "), Arduino basico (ID:" + idC5 + ")");
            System.out.println("    Carol -> Violao para todos (ID:" + idC6 + ")");

            // ============================================================
            //  TESTE 2: readAll — buscar todos os cursos de cada usuário
            // ============================================================
            printSecao("TESTE 2 - readAll(idUsuario) retorna cursos corretos");

            ArrayList<Curso> cursosAlice = arqCurso.readAll(idAlice);
            ArrayList<Curso> cursosBob   = arqCurso.readAll(idBob);
            ArrayList<Curso> cursosCarol = arqCurso.readAll(idCarol);

            System.out.println("  Cursos da Alice: " + listaNomes(cursosAlice));
            System.out.println("  Cursos do Bob:   " + listaNomes(cursosBob));
            System.out.println("  Cursos da Carol: " + listaNomes(cursosCarol));

            verificar("Alice tem 3 cursos", cursosAlice.size() == 3);
            verificar("Bob tem 2 cursos",   cursosBob.size() == 2);
            verificar("Carol tem 1 curso",  cursosCarol.size() == 1);

            verificar("Curso da Carol é 'Violao para todos'",
                cursosCarol.get(0).getNome().equals("Violao para todos"));

            // ============================================================
            //  TESTE 3: readAllOrdenadoPorNome — ordem alfabética
            // ============================================================
            printSecao("TESTE 3 - readAllOrdenadoPorNome retorna em ordem alfabética");

            ArrayList<Curso> aliceOrdenado = arqCurso.readAllOrdenadoPorNome(idAlice);
            System.out.println("  Cursos da Alice (ordem alfabética):");
            for (int i = 0; i < aliceOrdenado.size(); i++) {
                System.out.println("    " + (i + 1) + ". " + aliceOrdenado.get(i).getNome());
            }

            // Ordem esperada: Banco de Dados, Java avancado, Python basico
            verificar("Primeiro curso é 'Banco de Dados'",  aliceOrdenado.get(0).getNome().equals("Banco de Dados"));
            verificar("Segundo curso é 'Java avancado'",   aliceOrdenado.get(1).getNome().equals("Java avancado"));
            verificar("Terceiro curso é 'Python basico'",   aliceOrdenado.get(2).getNome().equals("Python basico"));

            // ============================================================
            //  TESTE 4: verificaUsuarioTemCursos
            // ============================================================
            printSecao("TESTE 4 - verificaUsuarioTemCursos");

            verificar("Alice TEM cursos",        arqCurso.verificaUsuarioTemCursos(idAlice) == true);
            verificar("Bob TEM cursos",          arqCurso.verificaUsuarioTemCursos(idBob) == true);
            verificar("Usuário 999 NAO tem",     arqCurso.verificaUsuarioTemCursos(999) == false);

            // ============================================================
            //  TESTE 5: delete — remover curso e verificar índice
            // ============================================================
            printSecao("TESTE 5 - delete remove curso do índice B+");

            System.out.println("  Removendo 'Java avancado' (ID:" + idC2 + ") da Alice...");
            boolean removido = arqCurso.delete(idC2);
            verificar("delete retornou true", removido);

            ArrayList<Curso> aliceAposDelete = arqCurso.readAll(idAlice);
            System.out.println("  Cursos da Alice agora: " + listaNomes(aliceAposDelete));
            verificar("Alice tem 2 cursos apos exclusao", aliceAposDelete.size() == 2);

            boolean achouJava = false;
            for (Curso c : aliceAposDelete)
                if (c.getNome().equals("Java avancado")) achouJava = true;
            verificar("'Java avancado' nao aparece mais", achouJava == false);

            // Verifica que os cursos do Bob não foram afetados
            ArrayList<Curso> bobAposDelete = arqCurso.readAll(idBob);
            verificar("Bob continua com 2 cursos (nao afetado)", bobAposDelete.size() == 2);

            // ============================================================
            //  TESTE 6: update — alterar nome e verificar índice de nomes
            // ============================================================
            printSecao("TESTE 6 - update atualiza indice de nomes");

            Curso pythonAtual = arqCurso.read(idC1);
            System.out.println("  Renomeando '" + pythonAtual.getNome() + "' -> 'Aprenda Python'...");
            Curso pythonNovo = new Curso(idC1, idAlice, "Aprenda Python", "Curso de Python",
                LocalDate.of(2026, 5, 20), "abc1234567", 0);
            boolean atualizado = arqCurso.update(pythonNovo);
            verificar("update retornou true", atualizado);

            ArrayList<Curso> aliceOrdenadoApos = arqCurso.readAllOrdenadoPorNome(idAlice);
            System.out.println("  Cursos da Alice (ordem alfabetica apos update):");
            for (int i = 0; i < aliceOrdenadoApos.size(); i++) {
                System.out.println("    " + (i + 1) + ". " + aliceOrdenadoApos.get(i).getNome());
            }
            // Ordem esperada: Aprenda Python, Banco de Dados
            verificar("1º agora é 'Aprenda Python'", aliceOrdenadoApos.get(0).getNome().equals("Aprenda Python"));
            verificar("2º agora é 'Banco de Dados'", aliceOrdenadoApos.get(1).getNome().equals("Banco de Dados"));

            // ============================================================
            //  TESTE 7: deletar TODOS os cursos de um usuário
            // ============================================================
            printSecao("TESTE 7 - Deletar todos os cursos de um usuario");

            System.out.println("  Removendo todos os cursos do Bob...");
            arqCurso.delete(idC4);
            arqCurso.delete(idC5);

            ArrayList<Curso> bobVazio = arqCurso.readAll(idBob);
            verificar("Bob tem 0 cursos", bobVazio.size() == 0);
            verificar("verificaUsuarioTemCursos(Bob) = false",
                arqCurso.verificaUsuarioTemCursos(idBob) == false);

            // Alice e Carol não foram afetadas
            verificar("Alice continua com 2 cursos", arqCurso.readAll(idAlice).size() == 2);
            verificar("Carol continua com 1 curso",  arqCurso.readAll(idCarol).size() == 1);

            // ============================================================
            //  TESTE 8: ParEmailId — busca de usuário por email
            // ============================================================
            printSecao("TESTE 8 - ParEmailId (indice de email funcional)");

            Usuario encontrado = arqUsuario.readEmail("alice@puc.br");
            verificar("readEmail('alice@puc.br') encontrou Alice",
                encontrado != null && encontrado.getNome().equals("Alice"));

            Usuario naoExiste = arqUsuario.readEmail("ninguem@puc.br");
            verificar("readEmail('ninguem@puc.br') retorna null", naoExiste == null);

            // ============================================================
            //  RESULTADO FINAL
            // ============================================================
            arqCurso.close();
            arqUsuario.close();
            limparDados();

            System.out.println("\n╔══════════════════════════════════════════════════════════╗");
            System.out.printf( "║  RESULTADO: %d/%d testes passaram", passou, totalTestes);
            int espacos = 28 - String.valueOf(passou).length() - String.valueOf(totalTestes).length();
            System.out.println(" ".repeat(espacos) + "║");
            if (falhou == 0) {
                System.out.println("║  (V) TUDO CERTO - Implementacao 1:N funcionando!         ║");
            } else {
                System.out.printf("║  (X) %d teste(s) falharam - verifique os erros acima", falhou);
                int esp2 = 38 - String.valueOf(falhou).length();
                System.out.println(" ".repeat(Math.max(esp2, 1)) + "║");
            }
            System.out.println("╚══════════════════════════════════════════════════════════╝");

        } catch (Exception e) {
            System.out.println("\n(X) ERRO FATAL: " + e.getMessage());
            e.printStackTrace();
            limparDados();
        }
    }

    // =================================================================
    //  Utilitários
    // =================================================================

    static void verificar(String descricao, boolean condicao) {
        totalTestes++;
        if (condicao) {
            passou++;
            System.out.println("    (V) " + descricao);
        } else {
            falhou++;
            System.out.println("    (X) FALHOU: " + descricao);
        }
    }

    static void printSecao(String titulo) {
        System.out.println("\n──────────────────────────────────────────────────────────");
        System.out.println("  " + titulo);
        System.out.println("──────────────────────────────────────────────────────────");
    }

    static String listaNomes(ArrayList<Curso> cursos) {
        if (cursos.isEmpty()) return "(vazio)";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cursos.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(cursos.get(i).getNome());
        }
        return sb.toString();
    }

    static void limparDados() {
        deletarPasta(new File("dados"));
    }

    static void deletarPasta(File pasta) {
        if (pasta.exists()) {
            File[] arquivos = pasta.listFiles();
            if (arquivos != null) {
                for (File f : arquivos) {
                    if (f.isDirectory()) deletarPasta(f);
                    else f.delete();
                }
            }
            pasta.delete();
        }
    }
}
