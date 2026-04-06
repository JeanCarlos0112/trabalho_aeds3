package entidades.usuarios;

import java.util.ArrayList;
import entidades.cursos.*;

public class ControleUsuario {
    private ArquivoUsuario arqUsuario;
    private ArquivoCurso arqCurso;

    public ControleUsuario() throws Exception {
        this.arqUsuario = new ArquivoUsuario();
        this.arqCurso = new ArquivoCurso();
    }

    public boolean cadastrarUsuario(String nome, String email, int hashSenha, String perguntaSecreta, int hashRespostaSecreta) throws Exception {
        // Verifica se o email escolhido já está em uso por outra pessoa
        if (arqUsuario.readEmail(email) != null) {
                return false; // Email já cadastrado
        }

        Usuario novoUsuario = new Usuario(nome, email, hashSenha, perguntaSecreta, hashRespostaSecreta);
        arqUsuario.create(novoUsuario);

        return true;
    }

    public Usuario logarUsuario(String email, int hashSenhaTentativa) throws Exception {
        Usuario login = arqUsuario.readEmail(email);
        if(login != null && login.getHashSenha() == hashSenhaTentativa) {
            return login; // Login bem-sucedido
        }

        return null; // Credenciais inválidas
    }

    public boolean atualizarUsuario(Usuario usuarioEditado) throws Exception {
        Usuario usuarioExistente = arqUsuario.read(usuarioEditado.getID());

        // Se ele mudou o email, checa se o novo email já tem dono
        if (!usuarioExistente.getEmail().equals(usuarioEditado.getEmail())) {
            if (arqUsuario.readEmail(usuarioEditado.getEmail()) != null) {
                return false; // O novo email escolhido já está em uso por outra pessoa
            }
        }
    
        return arqUsuario.update(usuarioEditado);
    }

    public boolean excluirUsuario(int idUsuario) throws Exception {
        // 1. Bloqueia se houver cursos ativos
        if (arqCurso.verificaUsuarioTemCursos(idUsuario)) {
            return false;   // A exclusão não é executada e retorna false 
        }

        // 2. Remove todos os cursos inativos associados  ao usuário
        ArrayList<Curso> cursosDoUsuario = arqCurso.readAll(idUsuario);
        for (Curso c : cursosDoUsuario) {
            arqCurso.delete(c.getID());
        }

        // 3. Remove o usuário
        return arqUsuario.delete(idUsuario);
    }
}
