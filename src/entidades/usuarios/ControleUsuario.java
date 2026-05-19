package entidades.usuarios;

import java.util.ArrayList;
import entidades.cursos.*;
import entidades.inscricoes.ArquivoCursoUsuario;

public class ControleUsuario {
    private ArquivoUsuario arqUsuario;
    private ArquivoCurso arqCurso;
    private ArquivoCursoUsuario arqInscricao;

    public ControleUsuario(ArquivoUsuario arqUsuario,
                           ArquivoCurso arqCurso,
                           ArquivoCursoUsuario arqInscricao) throws Exception {
        this.arqUsuario = arqUsuario;
        this.arqCurso = arqCurso;
        this.arqInscricao = arqInscricao;
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

        // Mantem a regra do TP1: bloqueia exclusao se houver curso ativo
        // (estado 0 ou 1). Cursos inativos (concluido / cancelado) sao
        // removidos em cascata mais abaixo.
        for (Curso c : cursosDoUsuario) {
            if (c.getEstado() == 0 || c.getEstado() == 1) {
                return false;
            }
        }

        // Integridade do N:N - duas direcoes em cascata:
        //   (1) inscricoes do USUARIO em cursos de OUTRAS pessoas
        //       precisam sumir junto com a conta dele.
        //   (2) inscricoes em cursos do PROPRIO usuario que serao
        //       deletados a seguir tambem precisam ser canceladas
        //       (cuida disso o controle: cada delete do curso cancela
        //       suas inscricoes em cascata - mas como aqui apagamos
        //       direto pelo arqCurso, fazemos manualmente).
        arqInscricao.deleteAllByUsuario(idUsuario);
        for (Curso c : cursosDoUsuario) {
            arqInscricao.deleteAllByCurso(c.getID());
            arqCurso.delete(c.getID());
        }

        return arqUsuario.delete(idUsuario);
    }
}
