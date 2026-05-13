package entidades.cursos;

import java.util.ArrayList;
import aed3.*;

public class ArquivoCurso extends Arquivo<Curso> {

    ArvoreBMais<ParIdId> indiceUsuarioCurso;
    ArvoreBMais<ParNomeId> indiceNomeCurso;
    HashExtensivel<ParCodigoId> indiceCodigo;

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

        indiceCodigo = new HashExtensivel<>(
            ParCodigoId.class.getConstructor(),
            4,
            "./dados/cursos/indiceCodigo.hash.db",
            "./dados/cursos/indiceCodigoCestos.hash.db"
        );
    }

    @Override
    public int create(Curso curso) throws Exception {
        int id = super.create(curso);

        indiceUsuarioCurso.create(new ParIdId(curso.getIdUsuario(), id));

        String nomeTruncado = truncaNome(curso.getNome());
        indiceNomeCurso.create(new ParNomeId(nomeTruncado, id));

        indiceCodigo.create(new ParCodigoId(curso.getCodigo(), id));

        return id;
    }

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

            indiceCodigo.delete(Math.abs(curso.getCodigo().hashCode()));
        }
        return removido;
    }

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

            if (!cursoAntigo.getCodigo().equals(novoCurso.getCodigo())) {
                indiceCodigo.delete(Math.abs(cursoAntigo.getCodigo().hashCode()));
                indiceCodigo.create(new ParCodigoId(novoCurso.getCodigo(), novoCurso.getID()));
            }
        }
        return atualizado;
    }

    public Curso readCodigo(String codigo) throws Exception {
        ParCodigoId par = indiceCodigo.read(Math.abs(codigo.hashCode()));
        if (par == null)
            return null;
        return read(par.getIdCurso());
    }

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

    public ArrayList<Curso> readAllOrdenadoPorNome(int idUsuario) throws Exception {
        ArrayList<Curso> cursos = readAll(idUsuario);

        cursos.sort((a, b) -> {
            String na = ParNomeId.transforma(a.getNome());
            String nb = ParNomeId.transforma(b.getNome());
            return na.compareTo(nb);
        });

        return cursos;
    }

    public ArrayList<Curso> readTodosAtivos() throws Exception {
        ArrayList<Curso> ativos = new ArrayList<>();
        int id = 1;
        int vaziosConsecutivos = 0; // Travão de segurança

        // Se procurar 50 IDs seguidos e todos derem nulo, ele quebra o loop
        while (vaziosConsecutivos < 50) { 
            Curso c = read(id);
            
            if (c != null) {
                if (c.getEstado() == 0 || c.getEstado() == 1) {
                    ativos.add(c);
                }
                vaziosConsecutivos = 0; // Encontrou um curso válido, zera o contador
            } else {
                vaziosConsecutivos++; // Não encontrou, aumenta a contagem de vazios
            }
            id++;
        }
        
        return ativos;
    }

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
        indiceCodigo.close();
    }

    private String truncaNome(String nome) {
        if (nome == null) return "";
        byte[] bytes = nome.getBytes();
        if (bytes.length > 26)
            return new String(bytes, 0, 26);
        return nome;
    }
}