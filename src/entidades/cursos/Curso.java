package entidades.cursos;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.time.LocalDate;
import aed3.InterfaceEntidade;

public class Curso implements InterfaceEntidade {
    private int idCurso;
    private int idUsuario;
    private String nome;
    private String descricao;
    private LocalDate dataInicio;
    private String codigo;
    private int estado;

    public Curso() { this(-1, -1, "", "", LocalDate.now(), "", 0); }

    public Curso(int idU, String n, String d, LocalDate dt, String c, int e) {
        this(-1, idU, n, d, dt, c, e);
    }

    public Curso(int idC, int idU, String n, String d, LocalDate dt, String c, int e) {
        idCurso = idC; idUsuario = idU; nome = n;
        descricao = d; dataInicio = dt; codigo = c; estado = e;
    }

    @Override public int getID() { return idCurso; }
    @Override public void setID(int id) { this.idCurso = id; }
    public int getIdUsuario() { return idUsuario; }
    public String getNome() { return nome; }
    public void setCodigo(String c) { this.codigo = c; }

    @Override
    public byte[] toByteArray() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(idCurso);
        dos.writeInt(idUsuario);
        dos.writeUTF(nome);
        dos.writeUTF(descricao);
        dos.writeLong(dataInicio.toEpochDay());
        dos.writeUTF(codigo);
        dos.writeInt(estado);
        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] vb) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(vb);
        DataInputStream dis = new DataInputStream(bais);
        idCurso = dis.readInt();
        idUsuario = dis.readInt();
        nome = dis.readUTF();
        descricao = dis.readUTF();
        dataInicio = LocalDate.ofEpochDay(dis.readLong());
        codigo = dis.readUTF();
        estado = dis.readInt();
    }
}