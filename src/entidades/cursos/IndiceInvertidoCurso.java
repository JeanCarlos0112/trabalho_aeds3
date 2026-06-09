package entidades.cursos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import aed3.ElementoLista;
import aed3.ListaInvertida;

/**
 * Indice invertido especifico para o atributo "nome" da entidade Curso.
 *
 * Encapsula a ListaInvertida do prof. e implementa:
 *
 *   (i)   sincronizacao na criacao/exclusao/atualizacao de Curso,
 *         calculando a frequencia (TF) de cada termo no nome,
 *         garantindo que indiceInvertido sempre reflita o estado
 *         atual dos cursos validos;
 *
 *   (ii)  busca por palavras-chave aplicando o modelo TFxIDF
 *         exatamente como descrito na especificacao do TP3:
 *           - extrai os termos da query (mesmo pipeline do nome)
 *           - para cada termo, le a lista invertida correspondente
 *           - calcula o IDF do termo: log10(N / df) + 1, onde
 *             N = numero total de cursos indexados e df = tamanho
 *             da lista do termo
 *           - multiplica TF por IDF para cada (idCurso, TF)
 *           - soma scores agrupando por idCurso (um curso pode ter
 *             score de varios termos da query)
 *           - ordena os idCursos por score decrescente, com criterio
 *             de desempate estavel pelo id ascendente.
 *
 * Os calculos de IDF sao feitos on-the-fly no momento da busca - a
 * lista invertida em si guarda apenas TF, conforme a spec.
 */
public class IndiceInvertidoCurso {

    private final ListaInvertida lista;

    /**
     * Numero de blocos por termo. 4 eh o default usado pelo prof. nos
     * exemplos. Como esta na construcao da ListaInvertida, e' definido
     * aqui para ficar visivel.
     */
    private static final int BLOCOS_POR_TERMO = 4;

    public IndiceInvertidoCurso(String pathDicionario, String pathBlocos) throws Exception {
        this.lista = new ListaInvertida(BLOCOS_POR_TERMO, pathDicionario, pathBlocos);
    }

    /**
     * Indexa um curso recem-criado: extrai os termos do nome, calcula
     * o TF de cada um e insere os pares (termo, ElementoLista(id, tf))
     * na lista invertida. Tambem incrementa o contador de entidades,
     * que e o N usado no calculo do IDF.
     *
     * Convencao de TF: ocorrencias do termo / total de termos validos
     * no nome (conforme exemplo da spec: para "Introducao a Inteligencia
     * Artificial", TF do "introducao" = 1/3 = 0.333).
     *
     * @param idCurso id do curso ja persistido em ArquivoCurso
     * @param nomeCurso nome cru do curso (sera processado pelo TermosUtil)
     */
    public void inserir(int idCurso, String nomeCurso) throws Exception {
        ArrayList<String> termos = TermosUtil.extrairTermos(nomeCurso);
        if (termos.isEmpty()) {
            // Mesmo cursos sem termos validos contam para N
            // (poderia gerar consequencias em IDF, mas mantemos
            // consistencia: N = total de cursos indexados).
            lista.incrementaEntidades();
            return;
        }

        // Conta ocorrencias por termo para calcular o TF
        HashMap<String, Integer> ocorrencias = new HashMap<>();
        for (String t : termos) {
            ocorrencias.merge(t, 1, Integer::sum);
        }

        int totalTermos = termos.size();
        // Insere uma entrada por termo unico no nome
        for (Map.Entry<String, Integer> entrada : ocorrencias.entrySet()) {
            float tf = entrada.getValue() / (float) totalTermos;
            lista.create(entrada.getKey(), new ElementoLista(idCurso, tf));
        }

        lista.incrementaEntidades();
    }

    /**
     * Remove um curso do indice. Percorre os termos do nome (assumindo
     * que o nome passado eh o nome ATUAL do curso, antes da exclusao) e
     * chama lista.delete para cada termo. Decrementa o contador de
     * entidades.
     *
     * @param idCurso id do curso a remover
     * @param nomeCurso nome do curso (usado para descobrir em quais
     *                  termos o id esta presente)
     */
    public void remover(int idCurso, String nomeCurso) throws Exception {
        ArrayList<String> termos = TermosUtil.extrairTermos(nomeCurso);

        // Para cada termo unico, deletar a entrada (id, *) da lista.
        // Usar um set para evitar chamadas duplicadas em termos repetidos
        // (o delete e' idempotente, mas o early-return em "termo nao
        // encontrado" indica falha, evitamos isso).
        java.util.HashSet<String> unicos = new java.util.HashSet<>(termos);
        for (String t : unicos) {
            lista.delete(t, idCurso);
        }

        lista.decrementaEntidades();
    }

    /**
     * Atualiza o indice apos uma edicao de nome. Estrategia simples:
     * remove com o nome antigo e insere com o novo, sem mexer no contador
     * de entidades (eh o mesmo curso, nao um curso novo).
     *
     * Se o nome nao mudou, nada e' feito - o chamador (ArquivoCurso.update)
     * decide se passa por aqui.
     */
    public void atualizar(int idCurso, String nomeAntigo, String nomeNovo) throws Exception {
        ArrayList<String> termosAntigos = TermosUtil.extrairTermos(nomeAntigo);
        ArrayList<String> termosNovos   = TermosUtil.extrairTermos(nomeNovo);

        java.util.HashSet<String> unicosAntigos = new java.util.HashSet<>(termosAntigos);
        for (String t : unicosAntigos) {
            lista.delete(t, idCurso);
        }

        if (termosNovos.isEmpty()) return;

        HashMap<String, Integer> ocorrencias = new HashMap<>();
        for (String t : termosNovos) ocorrencias.merge(t, 1, Integer::sum);

        int totalTermos = termosNovos.size();
        for (Map.Entry<String, Integer> entrada : ocorrencias.entrySet()) {
            float tf = entrada.getValue() / (float) totalTermos;
            lista.create(entrada.getKey(), new ElementoLista(idCurso, tf));
        }
    }

    /**
     * Realiza a busca por palavras-chave aplicando o modelo TFxIDF da
     * especificacao do TP3. Retorna idCursos ordenados por score
     * decrescente.
     *
     * @param query texto livre informado pelo usuario (pode conter
     *              maiusculas, acentos, pontuacao - tudo eh normalizado
     *              pelo TermosUtil antes da busca)
     * @return lista de ResultadoBusca ordenada por score decrescente.
     *         Lista vazia se a query nao tem termos validos (ex: so
     *         stop words) ou se nenhum curso indexado contem qualquer
     *         dos termos.
     */
    public ArrayList<ResultadoBusca> buscar(String query) throws Exception {
        ArrayList<String> termosQuery = TermosUtil.extrairTermos(query);
        if (termosQuery.isEmpty()) return new ArrayList<>();

        int totalEntidades = lista.numeroEntidades();
        if (totalEntidades == 0) return new ArrayList<>();

        // Acumula score por idCurso usando LinkedHashMap para preservar
        // ordem de insercao em casos de empate.
        LinkedHashMap<Integer, Float> scores = new LinkedHashMap<>();

        // Para cada termo unico da query (evita aplicar duas vezes o IDF
        // quando o usuario digita "inteligencia inteligencia"):
        java.util.HashSet<String> termosUnicos = new java.util.HashSet<>(termosQuery);
        for (String termo : termosUnicos) {
            ElementoLista[] entradas = lista.read(termo);
            if (entradas == null || entradas.length == 0) continue;

            int df = entradas.length; // numero de documentos com esse termo
            // IDF = log10(N / df) + 1, conforme a especificacao do TP3.
            // O "+1" e' aplicado para manter os pesos acima de 1, conforme
            // descrito no enunciado.
            float idf = (float) (Math.log10((double) totalEntidades / df) + 1.0);

            for (ElementoLista e : entradas) {
                float contribuicao = e.getFrequencia() * idf;
                scores.merge(e.getId(), contribuicao, Float::sum);
            }
        }

        if (scores.isEmpty()) return new ArrayList<>();

        ArrayList<ResultadoBusca> resultado = new ArrayList<>(scores.size());
        for (Map.Entry<Integer, Float> e : scores.entrySet()) {
            resultado.add(new ResultadoBusca(e.getKey(), e.getValue()));
        }

        // Ordenacao: score decrescente; empate -> idCurso ascendente
        // (estabilidade do resultado entre execucoes).
        Collections.sort(resultado, (a, b) -> {
            int cmpScore = Float.compare(b.score, a.score);
            if (cmpScore != 0) return cmpScore;
            return Integer.compare(a.idCurso, b.idCurso);
        });
        return resultado;
    }

    /**
     * Retorna o N atual (total de cursos indexados). Util para o bootstrap
     * em ArquivoCurso decidir se eh preciso reindexar cursos legados.
     */
    public int numeroEntidades() throws Exception {
        return lista.numeroEntidades();
    }

    public void close() throws Exception {
        lista.close();
    }

    /**
     * DTO simples para devolver os resultados da busca ja pareados com
     * o score TFxIDF. A View materializa em objetos Curso depois.
     */
    public static class ResultadoBusca {
        public final int idCurso;
        public final float score;
        public ResultadoBusca(int idCurso, float score) {
            this.idCurso = idCurso;
            this.score = score;
        }
        @Override
        public String toString() {
            return "(" + idCurso + "; " + score + ")";
        }
    }
}
