package entidades.cursos;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;

import entidades.inscricoes.ArquivoCursoUsuario;
import entidades.usuarios.ArquivoUsuario;
import entidades.usuarios.Usuario;

public class ControleCurso {
    private ArquivoCurso arqCurso;
    private ArquivoUsuario arqUsuario;
    private ArquivoCursoUsuario arqInscricao;

    // SecureRandom para gerar NanoIDs com qualidade criptografica
    // (recomendado pelo padrao NanoID, vs java.util.Random).
    private static final SecureRandom RNG = new SecureRandom();
    private static final String ALFABETO_NANOID =
        "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public ControleCurso(ArquivoCurso arqCurso,
                         ArquivoUsuario arqUsuario,
                         ArquivoCursoUsuario arqInscricao) throws Exception {
        this.arqCurso = arqCurso;
        this.arqUsuario = arqUsuario;
        this.arqInscricao = arqInscricao;
    }

    /**
     * Cadastra novo curso, gerando NanoID unico e vinculando ao usuario logado.
     * O ParCodigoId/hash assume que o NanoID e unico, entao garantimos isso
     * regerando caso colida com algum existente (caso extremamente raro).
     */
    public int cadastrarCurso(int idUsuario, String nome, String descricao) throws Exception {
        String nanoid;
        int tentativas = 0;
        do {
            nanoid = gerarNanoId();
            tentativas++;
            if (tentativas > 10) {
                throw new Exception("Falha ao gerar codigo NanoID unico.");
            }
        } while (arqCurso.readByCodigo(nanoid) != null);

        Curso novoCurso = new Curso(idUsuario, nome, descricao, LocalDate.now(), nanoid, 0);
        return arqCurso.create(novoCurso);
    }

    private String gerarNanoId() {
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(ALFABETO_NANOID.charAt(RNG.nextInt(ALFABETO_NANOID.length())));
        }
        return sb.toString();
    }

    public ArrayList<Curso> listarCursosOrdenados(int idUsuario) throws Exception {
        return arqCurso.readAllOrdenadoPorNome(idUsuario);
    }

    public boolean atualizarCurso(Curso cursoEditado) throws Exception {
        return arqCurso.update(cursoEditado);
    }

    public boolean excluirCurso(int idCurso) throws Exception {
        // Cascata: cancela todas as inscricoes naquele curso antes de
        // remover o registro do curso, mantendo a integridade referencial
        // do N:N (nao deixa "inscricoes orfas" apontando para um curso
        // que nao existe mais).
        arqInscricao.deleteAllByCurso(idCurso);
        return arqCurso.delete(idCurso);
    }

    public Curso buscarCurso(int idCurso) throws Exception {
        return arqCurso.read(idCurso);
    }

    // ============================================================
    //  TP2 - Busca de cursos (menu Minhas Inscricoes)
    // ============================================================

    /**
     * Busca um curso pelo codigo compartilhavel NanoID.
     * Usado pela opcao "(A) Buscar curso por codigo" do menu Minhas Inscricoes.
     *
     * @param codigo - codigo NanoID de 10 caracteres informado pelo usuario
     * @return o Curso correspondente, ou null se nao encontrar
     */
    public Curso buscarPorCodigo(String codigo) throws Exception {
        if (codigo == null) return null;
        return arqCurso.readByCodigo(codigo.trim());
    }

    /**
     * Lista todos os cursos disponiveis para inscricao, ordenados por data de inicio.
     * Filtra para mostrar apenas cursos no estado 0 (recebendo inscricoes), ja que
     * a tela "Lista de cursos" e usada para descobrir cursos onde se inscrever.
     * Cursos encerrados, concluidos ou cancelados nao aparecem nessa lista
     * (mas continuam acessiveis via busca por codigo).
     *
     * Usado pela opcao "(C) Listar todos os cursos" do menu Minhas Inscricoes.
     *
     * @return lista de cursos com estado == 0, ordenados por data de inicio crescente
     */
    public ArrayList<Curso> listarTodosCursosDisponiveis() throws Exception {
        ArrayList<Curso> todos = arqCurso.readAllCursos();

        ArrayList<Curso> disponiveis = new ArrayList<>();
        for (Curso c : todos) {
            if (c.getEstado() == 0) {
                disponiveis.add(c);
            }
        }

        disponiveis.sort((a, b) -> a.getDataInicio().compareTo(b.getDataInicio()));
        return disponiveis;
    }

    /**
     * Resolve o autor de um curso a partir do idUsuario.
     * Usado pela tela de detalhe de curso (busca/inscricao) para mostrar
     * o nome do dono do curso no campo AUTOR.
     *
     * @param idUsuario - id do usuario dono do curso
     * @return o Usuario autor, ou null se ja foi excluido
     */
    public Usuario buscarAutor(int idUsuario) throws Exception {
        return arqUsuario.read(idUsuario);
    }
}
