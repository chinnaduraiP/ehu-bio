# Introduction #

## Original article ##

Prieto G, Aloria K, Osinalde N, Fullaondo A, Arizmendi JM, Matthiesen R: **PAnalyzer: A software tool for protein inference in shotgun proteomics.** _BMC bioinformatics_ 2012, **13**:288.

DOI: 10.1186/1471-2105-13-288

URL: http://www.biomedcentral.com/1471-2105/13/288

## Description ##

PAnalyzer uses as input protein identification files in mzIdentML (v1.0.0, v1.1.0 and v1.2.0) or Waters PLGS formats and re-arranges the proteins into evidence categories according to their shared peptides.

Protein evidence categories considered are: conclusive, non-conclusive, ambiguous group and indistinguishable proteins.

## Multiple replicate runs ##

Multirun analysis is also supported and peptides are filtered if they are not present in a minimum number of replicate runs specified by the user. If all the peptides of a protein are filtered, the protein is also filtered.

## Output ##

Results are saved to a CSV file (useful for processing using a spreadsheet) and an HTML report is also generated for allowing an easy browse.

If a single mzIdentML file is used as input, a mzIdentML output is also generated.

If you want to convert a mzIdentML v1.1.0 input to a mzIdentML v1.2.0 output, just use a text editor to change the version attribute in the input mzid file and let PAnalyzer do the rest.

# Installation #

1. Download the latest stable version for your operating system
  * GNU/Linux: [PAnalyzer-v1.1-Linux.tar.gz](https://drive.google.com/file/d/0B1U_FilyidMsSFJncU9YUmdPVU0/edit?usp=sharing)
  * Mac OS X [PAnalyzer-v1.1-MacOSX.zip](https://drive.google.com/file/d/0B1U_FilyidMsempIaEZyTHRBcVE/edit?usp=sharing)
  * Windows: [PAnalyzer-v1.1-Windows.zip](https://drive.google.com/file/d/0B1U_FilyidMsOERNaTdkSUF1U0U/edit?usp=sharing)

Alternatively you can also download any other version (older ones and betas) from [Google Drive](https://drive.google.com/folderview?id=0B1U_FilyidMsTGFfck9rYzhXa0k&usp=sharing).

2. Extract all the files in the zip to any location

3. Install the free software Mono .NET framework with GTK# support (required for all platforms). You can download the framework for GNU/Linux, Mac OS X, Windows, etc. from here:

> http://www.go-mono.com/mono-downloads/download.html

> If you use Ubuntu you can simply install the package "mono-runtime" instead.

Probably you also want some initial protein samples:

> http://ehu-bio.googlecode.com/files/Protein-Samples.zip

# Execution #

Specific launchers are provided for the different platforms:

  * GNU/Linux: just click on _PAnalyzer.exe_ or execute _PAnalyzer.sh_ from a console, which will automatically install the mono runtime if not detected

  * Mac OS X: just click on the _PAnalyzer_ application

  * Windows: just click on _PAnalyzer.bat_ (Mono .NET runtime) or _PAnalyzer.exe_ (Microsoft .NET runtime)

# User instructions #

**Open** one or more protein identification files. Multiple files can be selected by holding the _Ctrl_ key while clicking on them.

Wait until PAnalyzer loads and validates the data and then click **Execute** to enter the analysis parameters dialog (only shown if applicable).

Now you can browse the peptide and re-arranged protein list from the user interface, but probably you prefer using the HTML output report. To generate it just click **Save** and select the destination directory and file name.