package entidades.cursos;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import aed3.InterfaceHashExtensivel;

/**
 * Par (codigo, idCurso) para uso na Tabela Hash Extensivel.
 *
 * Permite localizar um curso pelo codigo compartilhavel NanoID
 * (10 caracteres alfanumericos ASCII gerados pelo ControleCurso).
 *
 * Tamanho fixo: 10 bytes (codigo) + 4 bytes (id) = 14 bytes.
 *
 * O hashCode usa o codigo como chave, garantindo que cursos com
 * o mesmo NanoID caiam no mesmo bucket (colisao impossivel se o
 * NanoID for unico, o que e garantido pelo cadastro).
 */
public class ParCodigoId implements InterfaceHashExtensivel {

    private String codigo;
    private int id;

    private static final short TAMANHO_PAR = 14;
    private static final int TAMANHO_CODIGO = 10;

    public ParCodigoId() {
        this.codigo = "";
        this.id = -1;
    }

    public ParCodigoId(String codigo, int id) {
        this.codigo = codigo;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    @Override
    public int hashCode() {
        return Math.abs(this.codigo.hashCode());
    }

    @Override
    public short size() {
        return TAMANHO_PAR;
    }

    @Override
    public String toString() {
        return "(" + this.codigo + ";" + this.id + ")";
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        byte[] vb = new byte[TAMANHO_CODIGO];
        byte[] vbCodigo = this.codigo.getBytes("UTF-8");
        int i = 0;
        while (i < vbCodigo.length && i < TAMANHO_CODIGO) {
            vb[i] = vbCodigo[i];
            i++;
        }
        while (i < TAMANHO_CODIGO) {
            vb[i] = 0; // padding com zero para garantir tamanho fixo
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
        byte[] vb = new byte[TAMANHO_CODIGO];
        dis.read(vb);
        // trim() remove tanto espacos quanto bytes nulos (codigo <= 0x20)
        this.codigo = new String(vb, "UTF-8").trim();
        this.id = dis.readInt();
    }
}
