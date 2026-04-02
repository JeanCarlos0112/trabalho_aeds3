package entidades.usuarios;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import aed3.InterfaceEntidade;

public class Usuario implements InterfaceEntidade {
    private int idUsuario;
    private String nome;
    private String email;
    private int hashSenha;
    private String perguntaSecreta;
    private int hashRespostaSecreta;

    public Usuario() { this(-1, "", "", 0, "", 0); }

    public Usuario(String n, String e, int hs, String p, int hr) {
        this(-1, n, e, hs, p, hr);
    }

    public Usuario(int i, String n, String e, int hs, String p, int hr) {
        idUsuario = i; nome = n; email = e;
        hashSenha = hs; perguntaSecreta = p; hashRespostaSecreta = hr;
    }

    @Override public int getID() { return idUsuario; }
    @Override public void setID(int id) { this.idUsuario = id; }
    public String getEmail() { return email; }
    public String getNome() { return nome; }
    public int getHashSenha() { return hashSenha; }

    @Override
    public byte[] toByteArray() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(idUsuario);
        dos.writeUTF(nome);
        dos.writeUTF(email);
        dos.writeInt(hashSenha);
        dos.writeUTF(perguntaSecreta);
        dos.writeInt(hashRespostaSecreta);
        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] vb) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(vb);
        DataInputStream dis = new DataInputStream(bais);
        idUsuario = dis.readInt();
        nome = dis.readUTF();
        email = dis.readUTF();
        hashSenha = dis.readInt();
        perguntaSecreta = dis.readUTF();
        hashRespostaSecreta = dis.readInt();
    }
}