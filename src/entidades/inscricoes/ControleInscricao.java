package entidades.inscricoes;

import java.util.ArrayList;

import entidades.cursos.ArquivoCurso;
import entidades.cursos.Curso;
import entidades.usuarios.ArquivoUsuario;
import entidades.usuarios.Usuario;

/**
 * Controle das operacoes de inscricao do TP2.
 *
 * Orquestra o ArquivoCursoUsuario com os arquivos de Curso e Usuario
 * para validar regras de negocio antes de criar ou cancelar inscricoes,
 * e para resolver as referencias quando uma visao precisa de objetos
 * Curso ou Usuario a partir de inscricoes.
 */
public class ControleInscricao {

    private ArquivoCursoUsuario arqInscricao;
    private ArquivoCurso        arqCurso;
    private ArquivoUsuario      arqUsuario;

    public ControleInscricao(ArquivoCursoUsuario arqInscricao,
                             ArquivoCurso arqCurso,
                             ArquivoUsuario arqUsuario) {
        this.arqInscricao = arqInscricao;
        this.arqCurso     = arqCurso;
        this.arqUsuario   = arqUsuario;
    }

    /**
     * Codigos de retorno do metodo inscrever, para que a Visao possa
     * mostrar uma mensagem adequada ao usuario sem precisar tratar
     * exceptions de regras de negocio.
     */
    public static final int OK_INSCRITO         = 0;
    public static final int ERRO_CURSO_INEXISTENTE = 1;
    public static final int ERRO_CURSO_NAO_DISPONIVEL = 2;
    public static final int ERRO_DONO_INSCREVENDO_NO_PROPRIO = 3;
    public static final int ERRO_JA_INSCRITO     = 4;

    /**
     * Efetiva a inscricao do usuario logado no curso indicado, depois de
     * validar todas as regras de negocio.
     *
     * Regras validadas (em ordem):
     *   1) Curso existe
     *   2) Curso esta em estado 0 (recebendo inscricoes)
     *   3) O usuario nao e o dono do curso (proibe auto-inscricao)
     *   4) O usuario ja nao esta inscrito (proibe inscricao dupla)
     *
     * @return codigo de status (OK_INSCRITO ou ERRO_*)
     */
    public int inscrever(int idCurso, int idUsuario) throws Exception {
        Curso curso = arqCurso.read(idCurso);
        if (curso == null) return ERRO_CURSO_INEXISTENTE;
        if (curso.getEstado() != 0) return ERRO_CURSO_NAO_DISPONIVEL;
        if (curso.getIdUsuario() == idUsuario) return ERRO_DONO_INSCREVENDO_NO_PROPRIO;
        if (arqInscricao.existeInscricao(idCurso, idUsuario)) return ERRO_JA_INSCRITO;

        arqInscricao.create(new CursoUsuario(idCurso, idUsuario));
        return OK_INSCRITO;
    }

    /**
     * Cancela uma inscricao pelo ID da entidade CursoUsuario.
     * Usado pela Visao quando ja se tem o objeto CursoUsuario em mao.
     */
    public boolean cancelarInscricao(int idCursoUsuario) throws Exception {
        return arqInscricao.delete(idCursoUsuario);
    }

    /**
     * Cancela a inscricao do par (idCurso, idUsuario), localizando-a
     * pelo indice de curso antes de deletar.
     */
    public boolean cancelarInscricaoCursoUsuario(int idCurso, int idUsuario) throws Exception {
        int id = arqInscricao.buscarIdInscricao(idCurso, idUsuario);
        if (id == -1) return false;
        return arqInscricao.delete(id);
    }

    /**
     * Verifica se o usuario esta inscrito no curso.
     * Usado pela Visao de Curso para decidir entre mostrar
     * o botao "Fazer inscricao" ou "Voce ja esta inscrito".
     */
    public boolean estaInscrito(int idCurso, int idUsuario) throws Exception {
        return arqInscricao.existeInscricao(idCurso, idUsuario);
    }

    /**
     * Retorna a lista de cursos em que o usuario esta inscrito, com a
     * inscricao correspondente em paralelo (mesmo indice nas duas listas).
     * Util para montar a listagem no topo do menu Minhas Inscricoes,
     * que precisa exibir nome do curso, data de inicio e o estado do
     * curso (encerrado, concluido, cancelado).
     */
    public ArrayList<CursoComInscricao> listarMinhasInscricoes(int idUsuario) throws Exception {
        ArrayList<CursoUsuario> inscricoes = arqInscricao.readByUsuario(idUsuario);
        ArrayList<CursoComInscricao> resultado = new ArrayList<>();

        for (CursoUsuario ins : inscricoes) {
            Curso c = arqCurso.read(ins.getIdCurso());
            if (c != null) {
                resultado.add(new CursoComInscricao(c, ins));
            }
        }

        // Ordena por data de inicio do curso, crescente
        resultado.sort((a, b) -> a.curso.getDataInicio().compareTo(b.curso.getDataInicio()));
        return resultado;
    }

    /**
     * Retorna a lista de inscritos em um curso (objetos Usuario) com a
     * inscricao correspondente em paralelo. Util para a tela
     * "Gerenciar inscritos no curso" do menu Meus Cursos.
     */
    public ArrayList<UsuarioComInscricao> listarInscritos(int idCurso) throws Exception {
        ArrayList<CursoUsuario> inscricoes = arqInscricao.readByCurso(idCurso);
        ArrayList<UsuarioComInscricao> resultado = new ArrayList<>();

        for (CursoUsuario ins : inscricoes) {
            Usuario u = arqUsuario.read(ins.getIdUsuario());
            if (u != null) {
                resultado.add(new UsuarioComInscricao(u, ins));
            }
        }

        resultado.sort((a, b) -> a.usuario.getNome().compareToIgnoreCase(b.usuario.getNome()));
        return resultado;
    }

    /**
     * Exporta os inscritos de um curso em formato CSV, contendo no
     * minimo nome e email conforme a especificacao do TP2.
     * Tambem inclui a data de inscricao para enriquecer a exportacao.
     */
    public String exportarCSV(int idCurso) throws Exception {
        ArrayList<UsuarioComInscricao> inscritos = listarInscritos(idCurso);
        StringBuilder sb = new StringBuilder();
        sb.append("Nome,Email,DataInscricao\n");
        for (UsuarioComInscricao ui : inscritos) {
            sb.append(escapeCSV(ui.usuario.getNome())).append(',')
              .append(escapeCSV(ui.usuario.getEmail())).append(',')
              .append(ui.inscricao.getDataInscricao().toString())
              .append('\n');
        }
        return sb.toString();
    }

    private String escapeCSV(String campo) {
        if (campo == null) return "";
        boolean precisaAspas = campo.contains(",") || campo.contains("\"") || campo.contains("\n");
        if (!precisaAspas) return campo;
        return "\"" + campo.replace("\"", "\"\"") + "\"";
    }

    /**
     * Acesso direto a contagem de inscritos sem materializar a lista,
     * util para mostrar contadores em telas.
     */
    public int contarInscritos(int idCurso) throws Exception {
        return arqInscricao.readByCurso(idCurso).size();
    }

    /**
     * Estruturas auxiliares para devolver Curso/Usuario emparelhado com
     * a inscricao correspondente, evitando que a Visao tenha que ficar
     * fazendo lookups para descobrir dataInscricao e idCursoUsuario.
     */
    public static class CursoComInscricao {
        public final Curso curso;
        public final CursoUsuario inscricao;
        public CursoComInscricao(Curso c, CursoUsuario i) { curso = c; inscricao = i; }
    }

    public static class UsuarioComInscricao {
        public final Usuario usuario;
        public final CursoUsuario inscricao;
        public UsuarioComInscricao(Usuario u, CursoUsuario i) { usuario = u; inscricao = i; }
    }
}
