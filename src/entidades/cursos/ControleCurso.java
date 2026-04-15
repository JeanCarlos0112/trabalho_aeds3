package entidades.cursos;

import java.util.ArrayList;
import java.util.Random;
import java.time.LocalDate;

public class ControleCurso {
    private ArquivoCurso arqCurso;

    public ControleCurso() throws Exception {
        this.arqCurso = new ArquivoCurso();
    }

    public int cadastrarCurso(int idUsuario, String nome, String descricao) throws Exception {
        // Geração do NanoID de 10 caracteres (alfanumérico)
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

    public void fechar() throws Exception {
        arqCurso.close();
    }
}