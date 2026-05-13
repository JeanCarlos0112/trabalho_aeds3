package entidades.cursos;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import aed3.InterfaceHashExtensivel;

public class ParCodigoId implements InterfaceHashExtensivel {

    private String codigo;
    private int idCurso;

    private static final int TAMANHO_CODIGO = 10;
    private static final short TAMANHO_BYTES = (short) (TAMANHO_CODIGO + Integer.BYTES);

    public ParCodigoId() {
        this.codigo = "          ";
        this.idCurso = -1;
    }

    public ParCodigoId(String codigo, int idCurso) {
        this.codigo = (codigo == null) ? "          " : codigo;
        this.idCurso = idCurso;
    }

    public String getCodigo() { return codigo; }
    public int getIdCurso() { return idCurso; }

    @Override
    public int hashCode() {
        int h = 0;
        for (char ch : codigo.toCharArray()) h = h * 31 + ch;
        return Math.abs(h);
    }

    @Override
    public short size() { return TAMANHO_BYTES; }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        byte[] codigoBytes = codigo.getBytes("UTF-8");
        byte[] padded = new byte[TAMANHO_CODIGO];
        for (int i = 0; i < TAMANHO_CODIGO; i++) {
            padded[i] = (i < codigoBytes.length) ? codigoBytes[i] : (byte) ' ';
        }
        dos.write(padded);
        dos.writeInt(idCurso);
        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] vb) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(vb);
        DataInputStream dis = new DataInputStream(bais);
        byte[] codigoBytes = new byte[TAMANHO_CODIGO];
        dis.readFully(codigoBytes);
        this.codigo = new String(codigoBytes, "UTF-8").trim();
        this.idCurso = dis.readInt();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ParCodigoId)) return false;
        return this.codigo.equals(((ParCodigoId) obj).codigo);
    }
}