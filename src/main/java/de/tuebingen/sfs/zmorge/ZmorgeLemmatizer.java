package de.tuebingen.sfs.zmorge;

import de.tuebingen.sfs.jfst.fst.CompactFST;
import de.tuebingen.sfs.zmorge.convert.UD2SMORConverter;
import de.tuebingen.sfs.zmorge.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ZmorgeLemmatizer {

    private static final Pattern RM_TAGS = Pattern.compile("<[^>]*>");

    private boolean udTags;
    private CompactFST zmorge;

    public ZmorgeLemmatizer() {
        this("/zmorge.jfst");
    }

    public ZmorgeLemmatizer(boolean udTags) {
        this("/zmorge.jfst", udTags);
    }

    /**
     * Construct a new lemmatizer.
     * @param zmorgeFile The path to the Zmorge .jfst file (should be "/zmorge.jfst")
     */
    public ZmorgeLemmatizer(String zmorgeFile) {
        this(zmorgeFile, false);
    }

    public ZmorgeLemmatizer(String zmorgeFile, boolean udTags) {
        this.udTags = udTags;
        this.zmorge = CompactFST.readFromBinary(zmorgeFile);
    }

    public String[] getLemmasForAll(String[] wordsWithPOS) {
        return getLemmasForAll(Arrays.asList(wordsWithPOS));
    }

    public String[] getLemmasForAll(List<String> wordsWithPOS) {
        return wordsWithPOS.stream()
                .map(wordWithPOS -> StringUtils.split(wordWithPOS, '/'))
                .map(wordWithPOS -> StringUtils.join(wordWithPOS, '/')
                        + "/"
                        + StringUtils.join(getLemmas(wordWithPOS[0], wordWithPOS[1]), ','))
                .toArray(String[]::new);
    }

    /**
     * Get all possible lemmas for this word form.
     * @param word A word form
     * @return The word's lemmas
     */
    public String[] getLemmas(String word) {
        return getLemmasWithTags(word, null);
    }

    /**
     * Get all possible lemmas with this POS for this word form.
     * @param word A word form
     * @param pos A POS tag (Zmorge tagset)
     * @return The word's lemmas
     */
    public String[] getLemmas(String word, String pos) {
        if (pos != null && udTags)
            return Arrays.stream(UD2SMORConverter.convert(pos))
                    .flatMap(tag -> Arrays.stream(getLemmasWithTags(word, tag)))
                    .toArray(String[]::new);
        else
            return getLemmasWithTags(word, "<+"+pos+">");
    }

    private String[] getLemmasWithTags(String word, String tags) {
        return zmorge.apply(word)
                .stream()
                .filter(lemma -> (tags == null) || lemma.contains(tags))
                .map(lemma -> RM_TAGS.matcher(lemma).replaceAll(""))
                .distinct()
                .toArray(String[]::new);
    }

    public static void main(String[] args) {
        if (args.length > 1)
            System.err.println("Unknown arguments.");
        else {
            boolean udTags = (args.length == 1 && args[0].toLowerCase().equals("ud"));
            System.err.println(((udTags) ? "UD" : "SMOR") + " tagset selected.");
            System.err.print("Loading FST... ");
            ZmorgeLemmatizer lem = new ZmorgeLemmatizer("/zmorge.jfst", udTags);
            System.err.println("Ready. Enter words to lemmatize:");
            Scanner in = new Scanner(System.in);
            while (in.hasNextLine()) {
                String word = in.nextLine();
                int s = word.indexOf(' ');
                String[] lemmas = (s < 0) ? lem.getLemmas(word) : lem.getLemmas(word.substring(0, s), word.substring(s + 1));
                if (lemmas.length == 0) {
                    System.err.print("Unknown word form");
                    if (s >= 0)
                        System.err.print(" or POS");
                    System.err.println(".");
                }
                for (String lemma : lemmas)
                    System.out.println(lemma);
            }
            in.close();
        }
    }
}
