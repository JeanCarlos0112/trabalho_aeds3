package entidades.cursos;

import java.util.ArrayList;
import java.util.Random;
import java.time.LocalDate;

public class ControleCurso {

    private static final int CURSOS_POR_PAGINA = 10;

    private ArquivoCurso arqCurso;

    public ControleCurso(ArquivoCurso arqCurso) throws Exception {
        this.arqCurso = arqCurso;
    }

    public int cadastrarCurso(int idUsuario, String nome, String descricao) throws Exception {
        String alfabeto = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder nanoid = new StringBuilder();
        Random rnd = new Random();

        for (int i = 0; i < 10; i++) {
            nanoid.append(alfabeto.charAt(rnd.nextInt(alfabeto.length())));
        }

        Curso novoCurso = new Curso(idUsuario, nome, descricao, LocalDate.now(), nanoid.toString(), 0);
        return arqCurso.create(novoCurso);
    }

    public ArrayList<Curso> listarCursosOrdenados(int idUsuario) throws Exception {
        return arqCurso.readAllOrdenadoPorNome(idUsuario);
    }

    public boolean atualizarCurso(Curso cursoEditado) throws Exception {
        return arqCurso.update(cursoEditado);
    }

    public boolean excluirCurso(int idCurso) throws Exception {
        return arqCurso.delete(idCurso);
    }

    public Curso buscarCurso(int idCurso) throws Exception {
        return arqCurso.read(idCurso);
    }

    public Curso buscarCursoPorCodigo(String codigo) throws Exception {
        return arqCurso.readCodigo(codigo);
    }

    public ArrayList<Curso> listarCursosDisponiveisPaginado(int idUsuarioLogado, int pagina) throws Exception {
        ArrayList<Curso> todos = arqCurso.readTodosAtivos();

        ArrayList<Curso> filtrados = new ArrayList<>();
        for (Curso c : todos) {
            if (c.getIdUsuario() != idUsuarioLogado) {
                filtrados.add(c);
            }
        }

        quickSort(filtrados, 0, filtrados.size() - 1);

        int inicio = pagina * CURSOS_POR_PAGINA;
        int fim = Math.min(inicio + CURSOS_POR_PAGINA, filtrados.size());

        if (inicio >= filtrados.size()) {
            return new ArrayList<>();
        }

        return new ArrayList<>(filtrados.subList(inicio, fim));
    }

    public int totalPaginas(int idUsuarioLogado) throws Exception {
        ArrayList<Curso> todos = arqCurso.readTodosAtivos();

        int count = 0;
        for (Curso c : todos) {
            if (c.getIdUsuario() != idUsuarioLogado) {
                count++;
            }
        }

        return (int) Math.ceil((double) count / CURSOS_POR_PAGINA);
    }

    private void quickSort(ArrayList<Curso> lista, int esq, int dir) {
        if (esq >= dir) return;

        int pivo = particionar(lista, esq, dir);
        quickSort(lista, esq, pivo - 1);
        quickSort(lista, pivo + 1, dir);
    }

    private int particionar(ArrayList<Curso> lista, int esq, int dir) {
        LocalDate pivotData = lista.get(dir).getDataInicio();
        int i = esq - 1;

        for (int j = esq; j < dir; j++) {
            if (!lista.get(j).getDataInicio().isAfter(pivotData)) {
                i++;
                Curso temp = lista.get(i);
                lista.set(i, lista.get(j));
                lista.set(j, temp);
            }
        }

        Curso temp = lista.get(i + 1);
        lista.set(i + 1, lista.get(dir));
        lista.set(dir, temp);

        return i + 1;
    }
}