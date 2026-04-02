package entidades.usuarios;

import aed3.*;

/**
 * CRUD de Usuários estendendo Arquivo genérico.
 * 
 * Índice adicional (Hash Extensível):
 *   indiceEmail — ParEmailId(email, id)
 *   Permite buscar usuário pelo email (login).
 * 
 * NOTA: Os overrides de delete e update foram acrescentados
 * para manter o índice de email sincronizado em todas as operações.
 * O create e readEmail já existiam na versão do Miro.
 */
public class ArquivoUsuario extends Arquivo<Usuario> {

    HashExtensivel<ParEmailId> indiceEmail;

    public ArquivoUsuario() throws Exception {
        super("usuarios", Usuario.class.getConstructor());
        indiceEmail = new HashExtensivel<>(
            ParEmailId.class.getConstructor(),
            4,
            "./dados/usuarios/indiceEmail.d.db",
            "./dados/usuarios/indiceEmail.c.db"
        );
    }

    @Override
    public int create(Usuario u) throws Exception {
        int id = super.create(u);
        indiceEmail.create(new ParEmailId(u.getEmail(), id));
        return id;
    }

    /**
     * Busca usuário pelo email (para login).
     */
    public Usuario readEmail(String email) throws Exception {
        ParEmailId pei = indiceEmail.read(Math.abs(email.hashCode()));
        return (pei == null) ? null : read(pei.getId());
    }

    @Override
    public boolean delete(int id) throws Exception {
        Usuario u = read(id);
        if (u == null)
            return false;

        boolean removido = super.delete(id);
        if (removido) {
            indiceEmail.delete(Math.abs(u.getEmail().hashCode()));
        }
        return removido;
    }

    @Override
    public boolean update(Usuario novoUsuario) throws Exception {
        Usuario antigo = read(novoUsuario.getID());
        if (antigo == null)
            return false;

        boolean atualizado = super.update(novoUsuario);
        if (atualizado && !antigo.getEmail().equals(novoUsuario.getEmail())) {
            // Email mudou — atualiza o índice
            indiceEmail.delete(Math.abs(antigo.getEmail().hashCode()));
            indiceEmail.create(new ParEmailId(novoUsuario.getEmail(), novoUsuario.getID()));
        }
        return atualizado;
    }

    @Override
    public void close() throws Exception {
        super.close();
        indiceEmail.close();
    }
}
