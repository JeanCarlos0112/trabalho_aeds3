package entidades.cursos;

import java.util.ArrayList;
import aed3.*;

/**
 * 
 * Índices adicionais (Árvore B+):
 * 
 *  1) indiceUsuarioCurso — ParIdId(idUsuario, idCurso)
 *      Registra o relacionamento 1:N entre usuários e cursos.
 *      Busca com ParIdId(idUsuario, -1) retorna TODOS os cursos
 *      do usuário, pois o compareTo do ParIdId trata id2 == -1 como coringa.
 * 
 *  2) indiceNomeCurso — ParNomeId(nomeCurso, idCurso)
 *      Índice indireto por nome, necessário para listar cursos
 *      em ordem alfabética no menu (conforme especificação).
 */
public class ArquivoCurso extends Arquivo<Curso> {

    ArvoreBMais<ParIdId> indiceUsuarioCurso;
    ArvoreBMais<ParNomeId> indiceNomeCurso;

    public ArquivoCurso() throws Exception {
        super("cursos", Curso.class.getConstructor());

        indiceUsuarioCurso = new ArvoreBMais<>(
            ParIdId.class.getConstructor(),
            5,
            "./dados/cursos/indiceUsuarioCurso.btree.db"
        );

        indiceNomeCurso = new ArvoreBMais<>(
            ParNomeId.class.getConstructor(),
            5,
            "./dados/cursos/indiceNomeCurso.btree.db"
        );
    }

    /**
     * Insere curso e atualiza os dois índices B+
     * @param curso - objeto curso a ser criado no db
     * @return int - id do curso criado no db
    
    */
    @Override
    public int create(Curso curso) throws Exception {
        int id = super.create(curso);

        indiceUsuarioCurso.create(new ParIdId(curso.getIdUsuario(), id));

        String nomeTruncado = truncaNome(curso.getNome());
        indiceNomeCurso.create(new ParNomeId(nomeTruncado, id));

        return id;
    }

    /**
     * Remove curso e limpa os dois índices B+
     * @param int - id do curso no db a ser deletado
     * @return retorna true para sucesso e false para falha 
     */
    @Override
    public boolean delete(int id) throws Exception {
        Curso curso = read(id);
        if (curso == null)
            return false;

        boolean removido = super.delete(id);
        if (removido) {
            indiceUsuarioCurso.delete(new ParIdId(curso.getIdUsuario(), id));

            String nomeTruncado = truncaNome(curso.getNome());
            indiceNomeCurso.delete(new ParNomeId(nomeTruncado, id));
        }
        return removido;
    }

    /**
     * Atualiza curso e corrige os índices se necessário
     * @param curso - objeto curso atualizado
     * @return retorna true para sucesso e false para falha 
     */
    @Override
    public boolean update(Curso novoCurso) throws Exception {
        Curso cursoAntigo = read(novoCurso.getID());
        if (cursoAntigo == null)
            return false;

        boolean atualizado = super.update(novoCurso);
        if (atualizado) {
            if (!cursoAntigo.getNome().equals(novoCurso.getNome())) {
                indiceNomeCurso.delete(
                    new ParNomeId(truncaNome(cursoAntigo.getNome()), novoCurso.getID()));
                indiceNomeCurso.create(
                    new ParNomeId(truncaNome(novoCurso.getNome()), novoCurso.getID()));
            }

            if (cursoAntigo.getIdUsuario() != novoCurso.getIdUsuario()) {
                indiceUsuarioCurso.delete(
                    new ParIdId(cursoAntigo.getIdUsuario(), novoCurso.getID()));
                indiceUsuarioCurso.create(
                    new ParIdId(novoCurso.getIdUsuario(), novoCurso.getID()));
            }
        }
        return atualizado;
    }

    /**
    * Essa é a operação central do relacionamento 1:N.
    *  A busca na B+ com ParIdId(idUsuario, -1) explora o fato
    *  de que compareTo retorna 0 quando id2 == -1, trazendo
    *  todos os pares que compartilham aquele idUsuario.
     * @param idUsuario - id do usuario a ser lido
     * @return retorna arraylist do tipo curso com TODOS os cursos do usuário
     * @throws Exception
     */
    public ArrayList<Curso> readAll(int idUsuario) throws Exception {
        ArrayList<Curso> cursos = new ArrayList<>();

        ArrayList<ParIdId> pares = indiceUsuarioCurso.read(
            new ParIdId(idUsuario, -1)
        );

        for (ParIdId par : pares) {
            Curso c = read(par.getId2());
            if (c != null)
                cursos.add(c);
        }

        return cursos;
    }

    /**
     *  Necessário para montar o menu conforme especificação.
     * @param idUsuario - id do usuario a ser lido.
     * @return retorna cursos do usuário em ordem alfabética.
     * @throws Exception
     */
    public ArrayList<Curso> readAllOrdenadoPorNome(int idUsuario) throws Exception {
        ArrayList<Curso> cursos = readAll(idUsuario);

        cursos.sort((a, b) -> {
            String na = ParNomeId.transforma(a.getNome());
            String nb = ParNomeId.transforma(b.getNome());
            return na.compareTo(nb);
        });

        return cursos;
    }

    /**
     * Verifica se usuario tem cursos
     * @param idUsuario - id do usuario a ser lido.
     * @return retorna true se usuario tem cursos, false caso não tenha
     * @throws Exception
     */
    public boolean verificaUsuarioTemCursos(int idUsuario) throws Exception {
        ArrayList<ParIdId> pares = indiceUsuarioCurso.read(
            new ParIdId(idUsuario, -1)
        );
        return !pares.isEmpty();
    }

    @Override
    public void close() throws Exception {
        super.close();
        indiceUsuarioCurso.close();
        indiceNomeCurso.close();
    }

    private String truncaNome(String nome) {
        if (nome == null) return "";
        byte[] bytes = nome.getBytes();
        if (bytes.length > 26)
            return new String(bytes, 0, 26);
        return nome;
    }
}
