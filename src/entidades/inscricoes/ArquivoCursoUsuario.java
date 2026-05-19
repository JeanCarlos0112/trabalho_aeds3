package entidades.inscricoes;

import java.util.ArrayList;
import aed3.*;

/**
 * CRUD da entidade de associacao CursoUsuario, com dois indices indiretos
 * em Arvore B+ para suportar consultas dos dois lados do relacionamento N:N:
 *
 *  1) indiceCursoInscricao (Arvore B+) - ParIdId(idCurso, idCursoUsuario)
 *      Permite recuperar todas as inscricoes de um determinado curso.
 *      Busca com ParIdId(idCurso, -1) usa o coringa do compareTo do prof
 *      e retorna todas as inscricoes daquele curso.
 *
 *  2) indiceUsuarioInscricao (Arvore B+) - ParIdId(idUsuario, idCursoUsuario)
 *      Permite recuperar todas as inscricoes de um determinado usuario.
 *      Mesmo mecanismo de coringa: ParIdId(idUsuario, -1) retorna todas
 *      as inscricoes daquele usuario.
 *
 * A escolha de duas Arvores B+ (em vez de uma so) e exatamente o que a
 * especificacao do TP2 pede: o N:N precisa ser consultavel a partir de
 * qualquer um dos dois lados, e uma estrutura B+ so admite UM criterio
 * de ordenacao por vez.
 */
public class ArquivoCursoUsuario extends Arquivo<CursoUsuario> {

    ArvoreBMais<ParIdId> indiceCursoInscricao;
    ArvoreBMais<ParIdId> indiceUsuarioInscricao;

    public ArquivoCursoUsuario() throws Exception {
        super("inscricoes", CursoUsuario.class.getConstructor());

        indiceCursoInscricao = new ArvoreBMais<>(
            ParIdId.class.getConstructor(),
            5,
            "./dados/inscricoes/indiceCursoInscricao.btree.db"
        );

        indiceUsuarioInscricao = new ArvoreBMais<>(
            ParIdId.class.getConstructor(),
            5,
            "./dados/inscricoes/indiceUsuarioInscricao.btree.db"
        );
    }

    /**
     * Insere inscricao e atualiza ambos os indices B+.
     */
    @Override
    public int create(CursoUsuario inscricao) throws Exception {
        int id = super.create(inscricao);

        indiceCursoInscricao.create(
            new ParIdId(inscricao.getIdCurso(), id));
        indiceUsuarioInscricao.create(
            new ParIdId(inscricao.getIdUsuario(), id));

        return id;
    }

    /**
     * Remove inscricao e limpa ambos os indices B+.
     */
    @Override
    public boolean delete(int id) throws Exception {
        CursoUsuario inscricao = read(id);
        if (inscricao == null)
            return false;

        boolean removida = super.delete(id);
        if (removida) {
            indiceCursoInscricao.delete(
                new ParIdId(inscricao.getIdCurso(), id));
            indiceUsuarioInscricao.delete(
                new ParIdId(inscricao.getIdUsuario(), id));
        }
        return removida;
    }

    /**
     * Atualiza inscricao e corrige os indices se idCurso ou idUsuario mudaram.
     * Em pratica, os FKs de uma inscricao nao mudam (e como "trocar" a
     * inscricao para outra dupla curso-usuario), mas o override mantem o
     * contrato de sincronizacao caso o uso evolua.
     */
    @Override
    public boolean update(CursoUsuario nova) throws Exception {
        CursoUsuario antiga = read(nova.getID());
        if (antiga == null)
            return false;

        boolean atualizada = super.update(nova);
        if (atualizada) {
            if (antiga.getIdCurso() != nova.getIdCurso()) {
                indiceCursoInscricao.delete(
                    new ParIdId(antiga.getIdCurso(), nova.getID()));
                indiceCursoInscricao.create(
                    new ParIdId(nova.getIdCurso(), nova.getID()));
            }
            if (antiga.getIdUsuario() != nova.getIdUsuario()) {
                indiceUsuarioInscricao.delete(
                    new ParIdId(antiga.getIdUsuario(), nova.getID()));
                indiceUsuarioInscricao.create(
                    new ParIdId(nova.getIdUsuario(), nova.getID()));
            }
        }
        return atualizada;
    }

    /**
     * Retorna todas as inscricoes daquele curso (sob o ponto de vista
     * do dono do curso: quem se inscreveu nele).
     */
    public ArrayList<CursoUsuario> readByCurso(int idCurso) throws Exception {
        ArrayList<CursoUsuario> inscricoes = new ArrayList<>();

        ArrayList<ParIdId> pares = indiceCursoInscricao.read(
            new ParIdId(idCurso, -1)
        );

        for (ParIdId par : pares) {
            CursoUsuario inscricao = read(par.getId2());
            if (inscricao != null)
                inscricoes.add(inscricao);
        }

        return inscricoes;
    }

    /**
     * Retorna todas as inscricoes do usuario (sob o ponto de vista
     * do aluno: em quais cursos ele esta inscrito).
     */
    public ArrayList<CursoUsuario> readByUsuario(int idUsuario) throws Exception {
        ArrayList<CursoUsuario> inscricoes = new ArrayList<>();

        ArrayList<ParIdId> pares = indiceUsuarioInscricao.read(
            new ParIdId(idUsuario, -1)
        );

        for (ParIdId par : pares) {
            CursoUsuario inscricao = read(par.getId2());
            if (inscricao != null)
                inscricoes.add(inscricao);
        }

        return inscricoes;
    }

    /**
     * Verifica se ja existe uma inscricao do usuario X no curso Y.
     * Usado pelo ControleInscricao para nao permitir inscricao dupla.
     */
    public boolean existeInscricao(int idCurso, int idUsuario) throws Exception {
        ArrayList<ParIdId> pares = indiceCursoInscricao.read(
            new ParIdId(idCurso, -1)
        );
        for (ParIdId par : pares) {
            CursoUsuario inscricao = read(par.getId2());
            if (inscricao != null && inscricao.getIdUsuario() == idUsuario)
                return true;
        }
        return false;
    }

    /**
     * Localiza a inscricao especifica de um usuario em um curso e retorna seu ID.
     * Retorna -1 se nao existir.
     */
    public int buscarIdInscricao(int idCurso, int idUsuario) throws Exception {
        ArrayList<ParIdId> pares = indiceCursoInscricao.read(
            new ParIdId(idCurso, -1)
        );
        for (ParIdId par : pares) {
            CursoUsuario inscricao = read(par.getId2());
            if (inscricao != null && inscricao.getIdUsuario() == idUsuario)
                return inscricao.getID();
        }
        return -1;
    }

    /**
     * Remove em cascata todas as inscricoes de um curso.
     * Usado quando o curso e cancelado/excluido para manter
     * a integridade referencial.
     *
     * @return numero de inscricoes removidas
     */
    public int deleteAllByCurso(int idCurso) throws Exception {
        ArrayList<CursoUsuario> inscricoes = readByCurso(idCurso);
        int removidas = 0;
        for (CursoUsuario i : inscricoes) {
            if (delete(i.getID()))
                removidas++;
        }
        return removidas;
    }

    /**
     * Remove em cascata todas as inscricoes de um usuario.
     * Usado quando o usuario exclui sua conta para manter
     * a integridade referencial.
     *
     * @return numero de inscricoes removidas
     */
    public int deleteAllByUsuario(int idUsuario) throws Exception {
        ArrayList<CursoUsuario> inscricoes = readByUsuario(idUsuario);
        int removidas = 0;
        for (CursoUsuario i : inscricoes) {
            if (delete(i.getID()))
                removidas++;
        }
        return removidas;
    }

    @Override
    public void close() throws Exception {
        super.close();
        indiceCursoInscricao.close();
        indiceUsuarioInscricao.close();
    }
}
