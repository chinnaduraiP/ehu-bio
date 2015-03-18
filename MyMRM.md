# Introduction #

Targeted proteomics requires the selection of proteotypic peptides for each of the proteins of interest to be analyzed. Despite the existence of proteotypic peptides predictors and databases containing information about proteins and peptides identified in discovery proteomics studies, the most straightforward approach to develop a targeted method is to use the information obtained by shotgun experiments performed in the own lab.

MyMRM is a simple software tool to aid proteomic laboratories designing targeted proteomics methods for their own equipment by using the data of their shotgun experiments.

# Usage #

Shotgun data can be loaded as mzIdentML files and the application updates a database with the occurrences of the different peptides and fragments (if present) observed and their scores.

Once this shotgun database has been fed, a complete fasta protein database and the accession of the protein of interest must be provided. The application selects the peptides unique to this protein (not present in other proteins of the database) with the largest number of observations and best score, and then selects the precursor ions and the transitions (when available) most probable to be detected.

For more information you can see the [HUPO 2014 Poster](https://drive.google.com/file/d/0B1U_FilyidMsamIzTDZFN0ZVa2s/view?usp=sharing).

# Installation #

MyMRM is a web application intended to run on a local PC/server of the lab, where all the information will be centralized in a MySQL database. The lab members can access/update this information from their own PC using a web browser.

## Requirements ##

Before MyMRM installation you need:
  * A MySQL server with and empty database for MyMRM.
  * A Java servlet 2.5 web container, such as Apache Tomcat 7.

## Configuration ##

You can download the compiled [MyMRM.war](https://drive.google.com/#folders/0B1U_FilyidMsRURaVk15bWJEbTA) file and extract it to webapps in your Apache Tomcat 7 home directory.

Once extracted, you must configure two files:
  * In [WEB-INF/classes/META-INF/persistence.xml](https://code.google.com/p/ehu-bio/source/browse/trunk/Projects/java/MyMRM/src/META-INF/persistence.xml) indicate the URL of the database, the user and password.
  * In [WEB-INF/web.xml](https://code.google.com/p/ehu-bio/source/browse/trunk/Projects/java/MyMRM/WebContent/WEB-INF/web.xml) indicate the path where fasta files and temporary mzid files will be uploaded.