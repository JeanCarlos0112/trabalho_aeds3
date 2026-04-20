package entidades.usuarios;

import java.util.ArrayList;
import entidades.cursos.*;

public class ControleUsuario {
    private ArquivoUsuario arqUsuario;
    private ArquivoCurso arqCurso;

    public ControleUsuario(ArquivoUsuario arqUsuario, ArquivoCurso arqCurso) throws Exception {
        this.arqUsuario = arqUsuario;
        this.arqCurso = arqCurso;
    }

    public boolean cadastrarUsuario(String nome, String email, int hashSenha, String perguntaSecreta, int hashRespostaSecreta) throws Exception {
        if (arqUsuario.readEmail(email) != null) {
                return false;
        }

        Usuario novoUsuario = new Usuario(nome, email, hashSenha, perguntaSecreta, hashRespostaSecreta);
        arqUsuario.create(novoUsuario);

        return true;
    }

    public Usuario logarUsuario(String email, int hashSenhaTentativa) throws Exception {
        Usuario login = arqUsuario.readEmail(email);
        if(login != null && login.getHashSenha() == hashSenhaTentativa) {
            return login;
        }

        return null;
    }

    public boolean recuperarSenha(String email, int hashResposta, int novaSenhaHash) throws Exception {
        Usuario u = arqUsuario.readEmail(email);
        
        if (u != null && u.getHashRespostaSecreta() == hashResposta) {
            u.setHashSenha(novaSenhaHash);
            return arqUsuario.update(u);
        }
        return false;
    }

    public boolean atualizarUsuario(Usuario usuarioEditado) throws Exception {
        Usuario usuarioExistente = arqUsuario.read(usuarioEditado.getID());

        if (!usuarioExistente.getEmail().equals(usuarioEditado.getEmail())) {
            if (arqUsuario.readEmail(usuarioEditado.getEmail()) != null) {
                return false;
            }
        }
    
        return arqUsuario.update(usuarioEditado);
    }

    public String obterPerguntaSecreta(String email) throws Exception {
        Usuario u = arqUsuario.readEmail(email);
        return (u != null) ? u.getPerguntaSecreta() : null;
    } 

    public boolean excluirUsuario(int idUsuario) throws Exception {
        ArrayList<Curso> cursosDoUsuario = arqCurso.readAll(idUsuario);

        for (Curso c : cursosDoUsuario) {
            if (c.getEstado() == 0 || c.getEstado() == 1) {
                return false;
            }
        }

        for (Curso c : cursosDoUsuario) {
            arqCurso.delete(c.getID());
        }

        return arqUsuario.delete(idUsuario);
    }
}
