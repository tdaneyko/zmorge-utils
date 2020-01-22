package de.tuebingen.sfs.zmorge.convert;

public class UD2SMORConverter {

    private static final String[] UD = new String[] {"ADJ", "ADP", "ADV", "CCONJ", "DET", "INTJ", "NOUN", "NUM", "PART",
            "PRON", "PROPN", "PUNCT", "SCONJ", "SYM", "VERB", "X"};
    private static final String[][] SMOR = new String[][]{
            {"<+ADJ>", "<+CARD><Attr>", "<+ORD>"},
            {"<+POSTP>", "<+PREP>", "<+PREPART>"},
            {"<+ADV>", "<+PROADV>", "<+WADV>"},
            {"<+CONJ><Compar>", "<+CONJ><Coord>"},
            {"<+ART>", "<+DEM><Attr>", "<+INDEF><Attr>", "<+POSS><Attr>", "<+REL><Attr>", "<+WPRO><Attr>"},
            {"<+INTJ>"},
            {"<+CARD><Subst>", "<+NN>"},
            {"<+CARD>"},
            {"<+PTCL>", "<+VPART>"},
            {"<+PPRO>", "<+DEM><Subst>", "<+INDEF><Subst>", "<+POSS><Subst>", "<+REL><Subst>", "<+WPRO><Subst>"},
            {"<+NPROP>"},
            {"<+PUNCT>"},
            {"<+CONJ><Sub>"},
            {"<+SYMBOL>"},
            {"<+V>"},
            {"<+TRUNC>"}
    };

    public static String[] convert(String pos) {
        for (int i = 0; i < UD.length; i++) {
            if (pos.equals(UD[i]))
                return SMOR[i];
        }
        return new String[]{};
    }

}
