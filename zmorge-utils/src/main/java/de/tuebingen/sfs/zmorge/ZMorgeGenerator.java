package de.tuebingen.sfs.zmorge;

import de.tuebingen.sfs.jfst.fst.CompactFST;
import de.tuebingen.sfs.zmorge.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class ZMorgeGenerator {

    private static final List<String> IGN = Arrays.asList("<#>", "<->", "<~>");

    private CompactFST zmorge;

    public ZMorgeGenerator(String zmorgeFile) {
        zmorge = CompactFST.readFromBinary(zmorgeFile, true);
    }

    public Set<String> inflect(String taggedLemma) {
        return zmorge.apply(taggedLemma, IGN);
    }

    public Set<String> prefixSearch(String posTaggedLemma) {
        return zmorge.prefixSearch(posTaggedLemma, IGN);
    }

    public Set<String> prefixSearch(String lemma, String pos) {
        return prefixSearch(lemma+"<+"+pos+">");
    }

    public List<String> inflectAll(String tags, Iterable<String> lemmas) {
        List<String> forms = new ArrayList<>();
        for (String lemma : lemmas)
            forms.addAll(inflect(lemma+tags));
        return forms;
    }

    public static void main(String[] args) {
        System.err.print("Loading FST... ");
        ZMorgeGenerator gen = new ZMorgeGenerator("/zmorge.jfst");
        System.err.println("Ready.");
        System.err.println("=== USAGE ===");
        System.err.println("Enter:");
        System.err.println("   - A word with its Zmorge tags to get the corresponding inflection,");
        System.err.println("     e.g. 'fühl<~>en<+V><2><Sg><Pres><Ind>' to obtain 'fühlst' and 'fuehlst'.");
        System.err.println("   - A lemma with its POS tag (space-separated) to get all of its Zmorge tag tails,");
        System.err.println("     e.g. 'fühlen V' to obtain 'fühl<~>en<+V><2><Sg><Pres><Ind>' etc.");
        System.err.println("   - A lonely tag tail with at least two lemmas to get their corresponding inflections,");
        System.err.println("     e.g. '<+V><2><Sg><Pres><Ind> suchen finden' to obtain 'suchst' and 'findest'");
        System.err.println("=============");
        Scanner in = new Scanner(System.in);
        while (in.hasNextLine()) {
            String line = in.nextLine();
            String[] fields = StringUtils.split(line, ' ');
            if (fields.length == 1) {
                Set<String> infls = gen.inflect(fields[0]);
                if (infls.isEmpty())
                    System.out.println("Form not found!");
                for (String infl : infls)
                    System.out.println(infl);
            }
            else if (fields.length == 2) {
                Set<String> forms = gen.prefixSearch(fields[0], fields[1]);
                if (forms.isEmpty())
                    System.out.println("Lemma not found!");
                for (String form : forms)
                    System.out.println(form);
            }
            else {
                String infl = fields[0];
                List<String> lemmas = Arrays.stream(fields).skip(1).collect(Collectors.toList());
                List<String> forms = gen.inflectAll(infl, lemmas);
                if (forms.isEmpty())
                    System.out.println("Form not found!");
                for (String form : forms)
                    System.out.println(form);
            }
        }
    }
}
