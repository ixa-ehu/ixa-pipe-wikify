# ixa-pipe-ned

This repository contains the Named Entity Disambiguation tool based on DBpedia Spotlight.
Providing that a DBpedia Spotlight Rest server for a given language is running, the ixa-pipe-ned module will take
KAF or NAF as input (containing <entities> elements) and perform Named Entity Disambiguation
for your language of choice.

Developed by IXA NLP Group (ixa.si.ehu.es) for the 7th Framework OpeNER and NewsReader European projects.

### Contents

The contents of the repository are the following:

    + src/ source files of ixa-pipe-ned
    + pom.xml 
    + pom-naf.xml
    + README.md: This README

## Installation Procedure

In a snapshot:

 1. Install dbpedia-spotlight
 2. Compile ixa-pipe-ned module with mvn clean package
 3. Start dbpedia-spotlight server
 4. cat ner.kaf | ixa-pipe-ned/target/ixa-pipe-ned-1.0.jar -p $PORT_NUMBER

If you already have installed in your machine JDK7 and MAVEN 3, please go to step 3
directly. Otherwise, follow the detailed steps:

### 1. Install JDK 1.7

If you do not install JDK 1.7 in a default location, you will probably need to configure the PATH in .bashrc or .bash_profile:

    export JAVA_HOME=/yourpath/local/java17
    export PATH=${JAVA_HOME}/bin:${PATH}


If you use tcsh you will need to specify it in your .login as follows:

    setenv JAVA_HOME /usr/java/java17
    setenv PATH ${JAVA_HOME}/bin:${PATH}


If you re-login into your shell and run the command

    java -version


You should now see that your jdk is 1.7

### 2. Install MAVEN 3

Download MAVEN 3 from

    wget http://ftp.udc.es/apache/maven/maven-3/3.0.4/binaries/apache-maven-3.0.4-bin.tar.gz

Now you need to configure the PATH. For Bash Shell:

    export MAVEN_HOME=/home/ragerri/local/apache-maven-3.0.4
    export PATH=${MAVEN_HOME}/bin:${PATH}

For tcsh shell:

    setenv MAVEN3_HOME ~/local/apache-maven-3.0.4
    setenv PATH ${MAVEN3}/bin:{PATH}

If you re-login into your shell and run the command

    mvn -version


You should see reference to the MAVEN version you have just installed plus the JDK 7 that is using.

### 3. Download statistical backend - dbpedia spotlight

Downloand from http://spotlight.sztaki.hu/downloads/
- dbpedia-spotlight.jar
- German model: de.tar.gz
- English model: en.tar.gz or en_small.tar.gz
- Spanish model: es.tar.gz	
- French model: fr.tar.gz
- Italian model: it.tar.gz
- Dutch model: nl.tar.gz

Decompressed the language models 
- tar xvf $lang.tar.gz

Install dbpedia-spotlight
- go to the directory the dbpedia-spotlight.jar is located
- execute:
  mvn install:install-file -Dfile=dbpedia-spotlight.jar -DgroupId=ixa -DartifactId=dbpedia-spotlight -Dversion=0.6 -Dpackaging=jar -DgeneratePom=true
  This command will install dbpedia-spotlight jar as a local maven repository

Start the application
- java -jar dbpedia-spotlight.jar $lang http://localhost:$port/rest 


### 4. Download the ixa-pipe-ned repository

    git clone git@github.com:ixa-ehu/ixa-pipe-ned.git


### 5. Install ixa-pipe-ned

Install the ixa-pipe-ned module

To work with KAF files

    mvn clean package

This command will create a `ixa-pipe-ned/target` directory containing the
ixa-pipe-ned-1.0.jar binary with all dependencies included.

### 6. ixa-pipe-ned USAGE

The ixa-pipe-ned-1.0.jar requires a KAF or NAF document containing <entities> elements as standard input and
provides Named Entity Disambiguation as standard output. It also requires the port number as argument.
The port numbers assigned to each language are the following:

    - de: 2010
    - en: 2020
    - es: 2030
    - fr: 2040
    - it: 2050
    - nl: 2060

**Once you have a DBpedia Spotlight Rest server running you can send queries to it via the ixa-pipe-ned module as follows:

    cat ner.kaf | java -jar ixa-pipe-ned-1.0.jar -p $PORT_NUMBER

For more options running ixa-pipe-ned

    java -jar ixa-pipe-ned-1.0.jar -h

#### Contact information

    Rodrigo Agerri and Itziar Aldabe
    {rodrigo.agerri,itziar.aldabe}@ehu.es
    IXA NLP Group
    University of the Basque Country (UPV/EHU)
    E-20018 Donostia-San Sebastián

