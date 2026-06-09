package entidades.cursos;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Utilitario de processamento de termos para o indice invertido do TP3.
 *
 * Pipeline de extracao de termos (aplicado tanto na indexacao do nome do
 * curso quanto na query do usuario):
 *
 *   1. Tokenizacao: quebra a string em palavras separadas por espacos
 *      e pontuacao.
 *   2. Filtragem de stop words (artigos, preposicoes, conjuncoes,
 *      pronomes, numerais por extenso) em portugues, conforme a spec
 *      do TP3.
 *   3. Normalizacao: lowercase e remocao de acentos (NFD + remocao
 *      de marcas diacriticas) para que "Introducao" / "introdução"
 *      / "INTRODUCAO" caiam no mesmo termo "introducao".
 *
 * Garante simetria: o mesmo pipeline aplicado a "Introdução à Inteligência
 * Artificial" e a "INTELIGENCIA artificial" produz, respectivamente,
 * ["introducao", "inteligencia", "artificial"] e ["inteligencia", "artificial"],
 * de modo que a busca compare maca com maca.
 */
public class TermosUtil {

    /**
     * Lista de stop words em portugues conforme a especificacao do TP3
     * ("artigos, preposicoes, numerais e outras palavras irrelevantes para a busca").
     * Sao gravadas ja normalizadas (lowercase, sem acentos) para casar com a
     * forma normalizada usada na comparacao.
     */
    private static final Set<String> STOP_WORDS_PT;
    static {
        // Todas ja gravadas na forma normalizada (lowercase, sem acentos),
        // garantindo casamento com o resultado de normalizar() na comparacao.
        // O HashSet dedupica automaticamente termos que aparecem em mais
        // de uma categoria (ex: "se" eh conjuncao e pronome).
        String[] sw = {
            // Artigos
            "a", "o", "as", "os", "um", "uma", "uns", "umas",
            // Preposicoes e contracoes
            "de", "do", "da", "dos", "das",
            "em", "no", "na", "nos", "nas",
            "por", "pelo", "pela", "pelos", "pelas", "pra", "para",
            "ao", "aos",
            "com", "sem", "sob", "sobre", "ate", "entre", "contra", "desde", "perante",
            // Conjuncoes
            "e", "ou", "mas", "nem", "porem", "todavia", "contudo",
            "porque", "pois", "quando", "se", "como", "que", "embora",
            "enquanto", "logo",
            // Pronomes
            "eu", "tu", "ele", "ela", "vos", "eles", "elas",
            "meu", "minha", "seu", "sua", "nosso", "nossa",
            "este", "esta", "esse", "essa", "isto", "isso",
            "aquele", "aquela", "aquilo",
            // Formas verbais auxiliares mais frequentes
            "sao", "foi", "ser", "estar", "tem", "ter", "vai", "ir", "ha",
            // Numerais cardinais por extenso
            "dois", "tres", "quatro", "cinco", "seis", "sete", "oito",
            "nove", "dez", "onze", "doze", "vinte", "cem", "mil",
            // Marcadores irrelevantes para a busca
            "ja", "nao", "sim", "muito", "pouco", "mais", "menos",
            "todo", "toda", "todos", "todas", "tudo", "nada",
            "algum", "alguma", "alguns", "algumas", "qualquer"
        };
        HashSet<String> set = new HashSet<>(Arrays.asList(sw));
        STOP_WORDS_PT = Collections.unmodifiableSet(set);
    }

    /**
     * Extrai os termos validos de um texto (nome do curso ou query do usuario)
     * aplicando todo o pipeline: tokenizacao, normalizacao, filtragem de
     * stop words.
     *
     * @param texto qualquer string (pode conter acentos, pontuacao, maiusculas)
     * @return lista de termos normalizados, sem stop words, na ordem em que
     *         aparecem no texto (a ordem importa para o calculo do TF mas
     *         nao para o resultado em si)
     */
    public static ArrayList<String> extrairTermos(String texto) {
        ArrayList<String> termos = new ArrayList<>();
        if (texto == null) return termos;

        // 1) Tokenizacao: separa por qualquer sequencia de nao-letra/nao-digito.
        // Tabs, virgulas, hifens, parenteses, etc. viram separadores.
        String[] tokens = texto.split("[^\\p{L}\\p{Nd}]+");
        for (String tok : tokens) {
            if (tok.isEmpty()) continue;
            String normalizado = normalizar(tok);
            if (normalizado.isEmpty()) continue;
            // 2) Filtragem de stop words
            if (STOP_WORDS_PT.contains(normalizado)) continue;
            termos.add(normalizado);
        }
        return termos;
    }

    /**
     * Normaliza um termo: lowercase + remocao de acentos.
     *
     * Usa a Forma de Decomposicao Canonica NFD do Unicode, que separa cada
     * letra acentuada em (letra base) + (combining mark). Em seguida remove
     * todas as combining marks via regex, deixando apenas as letras base.
     *
     * Ex: "Introdução" -> "Introducao" (NFD) -> "introducao" (lowercase)
     */
    public static String normalizar(String s) {
        if (s == null) return "";
        String nfd = Normalizer.normalize(s, Normalizer.Form.NFD);
        String semAcentos = nfd.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return semAcentos.toLowerCase();
    }

    /**
     * Acessor read-only do conjunto de stop words, util para testes.
     */
    public static Set<String> getStopWords() {
        return STOP_WORDS_PT;
    }
}
