package de.tuebingen.sfs.zmorge;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import de.tuebingen.sfs.jfst.fst.CompactFST;
import de.tuebingen.sfs.zmorge.convert.UD2SMORConverter;
import de.tuebingen.sfs.zmorge.util.StringUtils;

public class ZmorgeSentenceLemmatizer {
    private static final Pattern RM_TAGS = Pattern.compile("<[^>]*>");

    private boolean udTags;
    private CompactFST zmorge;
    
    private static Map<String,String> frequentLemmaAmbiguityResolver;
    
    static {
    	frequentLemmaAmbiguityResolver = new TreeMap<String,String>();
    	frequentLemmaAmbiguityResolver.put("dass/daß", "dass");
    	frequentLemmaAmbiguityResolver.put("weißen/wissen", "wissen");
    	frequentLemmaAmbiguityResolver.put("möchten/mögen","mögen");
    	frequentLemmaAmbiguityResolver.put("gehören/hören","gehören");
    }

    public ZmorgeSentenceLemmatizer() {
        this("/zmorge.jfst");
    }

    public ZmorgeSentenceLemmatizer(boolean udTags) {
        this("/zmorge.jfst", udTags);
    }

    /**
     * Construct a new lemmatizer.
     * @param zmorgeFile The path to the Zmorge .jfst file (should be "/zmorge.jfst")
     */
    public ZmorgeSentenceLemmatizer(String zmorgeFile) {
        this(zmorgeFile, false);
    }

    public ZmorgeSentenceLemmatizer(String zmorgeFile, boolean udTags) {
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
    
    public static String deuTagMapping(String udTag) {
    	if (udTag.contentEquals("AUX")) return "VERB";
    	return udTag;
    }
    
    public static String normalizeLemma(String lemma, String tag) {
    	if (!tag.contentEquals("NOUN") && !tag.contentEquals("PROPN")) {
    		lemma = lemma.toLowerCase();
    	}
    	String disambiguated = frequentLemmaAmbiguityResolver.get(lemma);
    	if (disambiguated == null) disambiguated = lemma;
    	return disambiguated + "_" + tag;
    }

    public static void main(String[] args) {
    	
        if (args.length > 3 || args.length  == 0 || (args.length == 2 && !args[0].toLowerCase().equals("ud")))
            System.err.println("Usage: ZmorgeSentenceLemmatizer (ud) <tagged-input-sentences.txt>");
        else {
            boolean udTags = (args.length == 2 && args[0].toLowerCase().equals("ud"));
            String fileName = args[0];
            if (udTags) fileName = args[1];
            System.err.println(((udTags) ? "UD" : "SMOR") + " tagset selected.");
            System.err.print("Loading FST... ");
            ZmorgeLemmatizer lem = new ZmorgeLemmatizer("/zmorge.jfst", udTags);
            try {
	            Scanner in = new Scanner(new File(fileName));
	            while (in.hasNextLine()) {
	                String[] tokens = in.nextLine().split(" ");
	                String lemmatizedLine = "";
	                for (String token : tokens) {
		                int s = token.indexOf('_');
		                String wordForm = token.substring(0, s);
		                String tag = token.substring(s + 1);
		                Set<String> lemmas = new TreeSet<String>(Arrays.asList(lem.getLemmas(wordForm, deuTagMapping(tag))));
		                if (lemmas.size() == 0) {
		                    lemmatizedLine += normalizeLemma(wordForm, tag) + " ";
		                }
		                else {
		                	lemmatizedLine += normalizeLemma(StringUtils.join(lemmas, '/'), tag) + " ";
		                }
	                }
	                System.out.println(lemmatizedLine.substring(0,lemmatizedLine.length() - 1));
	            }
	            in.close();
            }
            catch (FileNotFoundException e) {
            	System.err.println("ERROR: input file \"" + fileName + "\" not found");
            }
        }
    }
}
