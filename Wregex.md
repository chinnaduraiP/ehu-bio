# Introduction #

## Original article ##

Prieto G, Fullaondo A, Rodriguez J A: **Prediction of nuclear export signals using weighted regular expressions (Wregex).**  _Bioinformatics_ (2014) 30 (9): 1220-1227.

DOI: 10.1093/bioinformatics/btu016

URL: http://bioinformatics.oxfordjournals.org/content/30/9/1220

## Summary ##

Wregex (weighted regular expression) offers a new approach for amino acid motif searching. Wregex combines a regular expression with an optional Position-Specific Scoring Matrix (PSSM). The regular expression is used to obtain a candidate list matching the desired conditions, and the PSSM is used for computing a score that can be used to select the most promising of those candidates.

# Online use #

For online use and the user manual, please go to the official Wregex website http://wregex.ehubio.es.

# Local installation #

You can download the compiled [wregex.war](https://drive.google.com/folderview?id=0B1U_FilyidMsMlg0cGxoRkt0VFE) file and copy/extract it to `webapps` in your Apache Tomcat 7 home directory, or you can checkout the source code from [the SVN repository](https://code.google.com/p/ehu-bio/source/checkout) and compile your customized version.

In any case you probably want to relax the default Wregex limits in [WEB-INF/web.xml](https://code.google.com/p/ehu-bio/source/browse/tags/Wregex-v1.0/Projects/java/Wregex/WebContent/WEB-INF/web.xml).

Wregex uses a predefined set of [databases](https://code.google.com/p/ehu-bio/source/browse/tags/Wregex-v1.0/Projects/java/Wregex/WebContent/resources/data/databases.xml) which **must** be present in the system. You can modify (add/delete) these databases by editing the [resources/data/databases.xml](https://code.google.com/p/ehu-bio/source/browse/tags/Wregex-v1.0/Projects/java/Wregex/WebContent/resources/data/databases.xml) file.