# Zmorge Utils

A German lemmatizer and inflected word form generator based on the [Zurich Morphological Analyzer for German](https://pub.cl.uzh.ch/users/sennrich/zmorge/) (Zmorge).

The Zmorge FST is provided as a JFST file (see submodule _jfst_). It was obtained by first converting the SFST binary for the Zmorge version `zmorge-20150315-smor_newlemma` into AT&T format using the Helsinki Finite State Toolkit (HFST) and then converting that AT&T file into a JFST binary using the JFST library.
