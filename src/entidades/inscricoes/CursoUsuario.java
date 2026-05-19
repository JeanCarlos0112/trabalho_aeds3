package entidades.inscricoes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.time.LocalDate;
import aed3.InterfaceEntidade;

/**
 * Entidade de associacao do relacionamento N:N entre Curso e Usuario.
 *
 * Cada registro representa UMA inscricao de UM usuario em UM curso.
 *
 * Atributos:
 *   - idCursoUsuario: identificador proprio (chave primaria)
 *   - idCurso:        chave estrangeira para Curso
 *   - idUsuario:      chave estrangeira para Usuario
 *   - dataInscricao:  data em que a inscricao foi efetivada
 *
 * Indexada por ArquivoCursoUsuario em duas Arvores B+ (uma por
 * idCurso, outra por idUsuario), permitindo navegacao a partir
 * de qualquer um dos dois lados do relacionamento.
 */
public class CursoUsuario implements InterfaceEntidade {

    private int idCursoUsuario;
    private int idCurso;
    private int idUsuario;
    private LocalDate dataInscricao;

    public CursoUsuario() {
        this(-1, -1, -1, LocalDate.now());
    }

    public CursoUsuario(int idCurso, int idUsuario) {
        this(-1, idCurso, idUsuario, LocalDate.now());
    }

    public CursoUsuario(int idCurso, int idUsuario, LocalDate dataInscricao) {
        this(-1, idCurso, idUsuario, dataInscricao);
    }

    public CursoUsuario(int idCursoUsuario, int idCurso, int idUsuario, LocalDate dataInscricao) {
        this.idCursoUsuario = idCursoUsuario;
        this.idCurso = idCurso;
        this.idUsuario = idUsuario;
        this.dataInscricao = dataInscricao;
    }

    @Override public int getID() { return idCursoUsuario; }
    public int getIdCurso() { return idCurso; }
    public int getIdUsuario() { return idUsuario; }
    public LocalDate getDataInscricao() { return dataInscricao; }

    @Override public void setID(int id) { this.idCursoUsuario = id; }

    @Override
    public byte[] toByteArray() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(idCursoUsuario);
        dos.writeInt(idCurso);
        dos.writeInt(idUsuario);
        dos.writeLong(dataInscricao.toEpochDay());
        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] vb) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(vb);
        DataInputStream dis = new DataInputStream(bais);
        idCursoUsuario = dis.readInt();
        idCurso = dis.readInt();
        idUsuario = dis.readInt();
        dataInscricao = LocalDate.ofEpochDay(dis.readLong());
    }

    @Override
    public String toString() {
        return "CursoUsuario[id=" + idCursoUsuario
            + ", idCurso=" + idCurso
            + ", idUsuario=" + idUsuario
            + ", dataInscricao=" + dataInscricao + "]";
    }
}
