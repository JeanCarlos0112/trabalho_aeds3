package entidades.usuarios;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import aed3.InterfaceHashExtensivel;

/**
 * Par (email, id) para uso na Tabela Hash Extensível.
 * Permite localizar um usuário pelo email (usado no login).
 * 
 * O hashCode é calculado a partir do email para posicionamento
 * no diretório da hash extensível.
 * 
 * Tamanho fixo: 50 bytes (email) + 4 bytes (id) = 54 bytes
 */
public class ParEmailId implements InterfaceHashExtensivel {

    private String email;
    private int id;
    private final short TAMANHO = 54;

    public ParEmailId() {
        this.email = "";
        this.id = -1;
    }

    public ParEmailId(String email, int id) {
        this.email = email;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public int hashCode() {
        return Math.abs(this.email.hashCode());
    }

    @Override
    public short size() {
        return this.TAMANHO;
    }

    @Override
    public String toString() {
        return "(" + this.email + ";" + this.id + ")";
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        byte[] vb = new byte[50];
        byte[] vbEmail = this.email.getBytes("UTF-8");
        int i = 0;
        while (i < vbEmail.length && i < 50) {
            vb[i] = vbEmail[i];
            i++;
        }
        while (i < 50) {
            vb[i] = 0;
            i++;
        }
        dos.write(vb);
        dos.writeInt(this.id);
        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);
        byte[] vb = new byte[50];
        dis.read(vb);
        this.email = new String(vb, "UTF-8").trim();
        this.id = dis.readInt();
    }
}
