package entidades.cursos;

import java.io.RandomAccessFile;
import java.util.ArrayList;
import aed3.*;

/**
 *
 * Indices adicionais:
 *
 *  1) indiceUsuarioCurso (Arvore B+) - ParIdId(idUsuario, idCurso)
 *      Registra o relacionamento 1:N entre usuarios e cursos.
 *      Busca com ParIdId(idUsuario, -1) retorna TODOS os cursos
 *      do usuario, pois o compareTo do ParIdId trata id2 == -1 como coringa.
 *
 *  2) indiceNomeCurso (Arvore B+) - ParNomeId(nomeCurso, idCurso)
 *      Indice indireto por nome, necessario para listar cursos
 *      em ordem alfabetica no menu (conforme especificacao do TP1).
 *
 *  3) indiceCodigoCurso (Hash Extensivel) - ParCodigoId(codigo, idCurso)
 *      Indice indireto por codigo NanoID, usado na busca de cursos
 *      por codigo na tela "Minhas Inscricoes" (TP2).
 */
public class ArquivoCurso extends Arquivo<Curso> {

    ArvoreBMais<ParIdId> indiceUsuarioCurso;
    ArvoreBMais<ParNomeId> indiceNomeCurso;
    HashExtensivel<ParCodigoId> indiceCodigoCurso;
    IndiceInvertidoCurso indiceInvertido;

    public ArquivoCurso() throws Exception {
        super("cursos", Curso.class.getConstructor());

        indiceUsuarioCurso = new ArvoreBMais<>(
            ParIdId.class.getConstructor(),
            5,
            "./dados/cursos/indiceUsuarioCurso.btree.db"
        );

        indiceNomeCurso = new ArvoreBMais<>(
            ParNomeId.class.getConstructor(),
            5,
            "./dados/cursos/indiceNomeCurso.btree.db"
        );

        indiceCodigoCurso = new HashExtensivel<>(
            ParCodigoId.class.getConstructor(),
            4,
            "./dados/cursos/indiceCodigo.d.db",
            "./dados/cursos/indiceCodigo.c.db"
        );

        // TP3: indice invertido sobre os nomes dos cursos.
        // Usa a ListaInvertida do prof (pacote aed3) com dois arquivos:
        // o dicionario de termos e os blocos com (idCurso, TF).
        indiceInvertido = new IndiceInvertidoCurso(
            "./dados/cursos/indiceInvertido.dicionario.db",
            "./dados/cursos/indiceInvertido.blocos.db"
        );

        // Bootstrap: cursos cadastrados em versoes anteriores (pre-TP3)
        // nao tem entrada no indice invertido. Se o indice estiver vazio
        // mas o arquivo de dados ja tiver cursos, reindexa todos.
        // Caso tipico: primeiro `java Principal` apos a entrega do TP3
        // em um repositorio com cursos do TP2 ja gravados em disco.
        if (indiceInvertido.numeroEntidades() == 0) {
            ArrayList<Curso> existentes = readAllCursos();
            if (!existentes.isEmpty()) {
                for (Curso c : existentes) {
                    indiceInvertido.inserir(c.getID(), c.getNome());
                }
            }
        }
    }

    /**
     * Insere curso e atualiza os quatro indices (3 do TP1/TP2 + 1 invertido do TP3).
     */
    @Override
    public int create(Curso curso) throws Exception {
        int id = super.create(curso);

        indiceUsuarioCurso.create(new ParIdId(curso.getIdUsuario(), id));

        String nomeTruncado = truncaNome(curso.getNome());
        indiceNomeCurso.create(new ParNomeId(nomeTruncado, id));

        indiceCodigoCurso.create(new ParCodigoId(curso.getCodigo(), id));

        // TP3: indexa os termos do nome do curso na lista invertida.
        indiceInvertido.inserir(id, curso.getNome());

        return id;
    }

    /**
     * Remove curso e limpa os quatro indices (3 do TP1/TP2 + 1 invertido do TP3).
     */
    @Override
    public boolean delete(int id) throws Exception {
        Curso curso = read(id);
        if (curso == null)
            return false;

        boolean removido = super.delete(id);
        if (removido) {
            indiceUsuarioCurso.delete(new ParIdId(curso.getIdUsuario(), id));

            String nomeTruncado = truncaNome(curso.getNome());
            indiceNomeCurso.delete(new ParNomeId(nomeTruncado, id));

            indiceCodigoCurso.delete(Math.abs(curso.getCodigo().hashCode()));

            // TP3: remove os termos do nome do indice invertido e
            // decrementa o contador de entidades (N) usado no IDF.
            indiceInvertido.remover(id, curso.getNome());
        }
        return removido;
    }

    /**
     * Atualiza curso e corrige os indices se necessario.
     *
     * O codigo NanoID nao muda em operacoes normais de update
     * (a Visao nao expoe alteracao do codigo), mas o indice de
     * codigo e mantido por seguranca caso isso mude no futuro.
     */
    @Override
    public boolean update(Curso novoCurso) throws Exception {
        Curso cursoAntigo = read(novoCurso.getID());
        if (cursoAntigo == null)
            return false;

        boolean atualizado = super.update(novoCurso);
        if (atualizado) {
            if (!cursoAntigo.getNome().equals(novoCurso.getNome())) {
                indiceNomeCurso.delete(
                    new ParNomeId(truncaNome(cursoAntigo.getNome()), novoCurso.getID()));
                indiceNomeCurso.create(
                    new ParNomeId(truncaNome(novoCurso.getNome()), novoCurso.getID()));

                // TP3: nome mudou -> re-indexar termos no invertido
                // (delete dos termos antigos + insert dos novos, sem mexer
                // no contador N porque eh o mesmo curso).
                indiceInvertido.atualizar(novoCurso.getID(),
                    cursoAntigo.getNome(), novoCurso.getNome());
            }

            if (cursoAntigo.getIdUsuario() != novoCurso.getIdUsuario()) {
                indiceUsuarioCurso.delete(
                    new ParIdId(cursoAntigo.getIdUsuario(), novoCurso.getID()));
                indiceUsuarioCurso.create(
                    new ParIdId(novoCurso.getIdUsuario(), novoCurso.getID()));
            }

            if (!cursoAntigo.getCodigo().equals(novoCurso.getCodigo())) {
                indiceCodigoCurso.delete(Math.abs(cursoAntigo.getCodigo().hashCode()));
                indiceCodigoCurso.create(
                    new ParCodigoId(novoCurso.getCodigo(), novoCurso.getID()));
            }
        }
        return atualizado;
    }

    /**
     * Operacao central do relacionamento 1:N.
     * Busca na B+ com ParIdId(idUsuario, -1) explora o fato de que
     * compareTo retorna 0 quando id2 == -1, trazendo todos os pares
     * que compartilham aquele idUsuario.
     */
    public ArrayList<Curso> readAll(int idUsuario) throws Exception {
        ArrayList<Curso> cursos = new ArrayList<>();

        ArrayList<ParIdId> pares = indiceUsuarioCurso.read(
            new ParIdId(idUsuario, -1)
        );

        for (ParIdId par : pares) {
            Curso c = read(par.getId2());
            if (c != null)
                cursos.add(c);
        }

        return cursos;
    }

    /**
     * Cursos do usuario em ordem alfabetica (usado no menu Meus Cursos do TP1).
     */
    public ArrayList<Curso> readAllOrdenadoPorNome(int idUsuario) throws Exception {
        ArrayList<Curso> cursos = readAll(idUsuario);

        cursos.sort((a, b) -> {
            String na = ParNomeId.transforma(a.getNome());
            String nb = ParNomeId.transforma(b.getNome());
            return na.compareTo(nb);
        });

        return cursos;
    }

    /**
     * Verifica se usuario tem cursos cadastrados.
     * Usado pelo ControleUsuario para bloquear exclusao de conta com cursos ativos.
     */
    public boolean verificaUsuarioTemCursos(int idUsuario) throws Exception {
        ArrayList<ParIdId> pares = indiceUsuarioCurso.read(
            new ParIdId(idUsuario, -1)
        );
        return !pares.isEmpty();
    }

    /**
     * Busca um curso pelo codigo NanoID compartilhavel.
     * Usado na tela de busca por codigo do menu Minhas Inscricoes (TP2).
     *
     * @param codigo - codigo NanoID de 10 caracteres
     * @return o Curso correspondente ou null se nao existir
     */
    public Curso readByCodigo(String codigo) throws Exception {
        if (codigo == null || codigo.isEmpty())
            return null;

        ParCodigoId par = indiceCodigoCurso.read(Math.abs(codigo.hashCode()));
        if (par == null)
            return null;

        // Confirmacao por igualdade exata: protege contra colisoes
        // teoricas de hashCode. O codigo recuperado deve casar com o buscado.
        if (!par.getCodigo().equals(codigo))
            return null;

        return read(par.getId());
    }

    /**
     * Le TODOS os cursos do arquivo de dados, varrendo o dados.db
     * sequencialmente e ignorando registros com lapide '*' (excluidos).
     *
     * A base Arquivo do prof nao expoe iteracao e a referencia ao
     * RandomAccessFile e package-private em aed3. Para nao alterar
     * o codigo do professor, abre-se aqui um leitor proprio em modo
     * read-only sobre o mesmo arquivo dados.db. E seguro em ambiente
     * single-thread (que e o caso do nosso programa).
     *
     * Layout do registro (definido por Arquivo.create):
     *   byte lapide (' ' valido, '*' excluido)
     *   short tamanho do registro
     *   byte[] dados
     *
     * Cabecalho do arquivo (12 bytes): int ultimoID + long cabecaListaVazios.
     *
     * @return lista com todos os cursos validos
     */
    public ArrayList<Curso> readAllCursos() throws Exception {
        ArrayList<Curso> cursos = new ArrayList<>();
        RandomAccessFile leitor = new RandomAccessFile(
            "./dados/cursos/dados.db", "r");

        try {
            if (leitor.length() < TAM_CABECALHO)
                return cursos;

            leitor.seek(TAM_CABECALHO); // pula o cabecalho

            while (leitor.getFilePointer() < leitor.length()) {
                byte lapide = leitor.readByte();
                short tam = leitor.readShort();

                if (tam <= 0) break; // dados corrompidos

                byte[] buffer = new byte[tam];
                int lido = leitor.read(buffer);
                if (lido < tam) break;

                if (lapide == ' ') {
                    Curso c = new Curso();
                    c.fromByteArray(buffer);
                    cursos.add(c);
                }
            }
        } finally {
            leitor.close();
        }

        return cursos;
    }

    /**
     * Busca cursos por palavras-chave do nome usando o indice invertido
     * (TFxIDF). Retorna os Curso ja materializados, na ordem decrescente
     * de score; o calculo do score eh feito pelo IndiceInvertidoCurso.
     *
     * @param query texto livre informado pelo usuario
     * @return cursos ordenados por relevancia. Lista vazia se nenhuma
     *         palavra valida foi reconhecida na query ou se nenhum curso
     *         contem qualquer dos termos.
     */
    public ArrayList<Curso> readByPalavras(String query) throws Exception {
        ArrayList<IndiceInvertidoCurso.ResultadoBusca> ranking = indiceInvertido.buscar(query);
        ArrayList<Curso> cursos = new ArrayList<>(ranking.size());
        for (IndiceInvertidoCurso.ResultadoBusca r : ranking) {
            Curso c = read(r.idCurso);
            if (c != null) cursos.add(c);
        }
        return cursos;
    }

    @Override
    public void close() throws Exception {
        super.close();
        indiceUsuarioCurso.close();
        indiceNomeCurso.close();
        indiceCodigoCurso.close();
        indiceInvertido.close();
    }

    private String truncaNome(String nome) {
        if (nome == null) return "";
        byte[] bytes = nome.getBytes();
        if (bytes.length > 26)
            return new String(bytes, 0, 26);
        return nome;
    }
}
