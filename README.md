# ixa-pipe-wikify

This repository contains the Wikification tool based on DBpedia
Spotlight. Providing that a DBpedia Spotlight Rest server for a given
language is running, the ixa-pipe-wikify module takes KAF or NAF (with
'wf' elements) as input and perform Wikification for your language of
choice.

Developed by IXA NLP Group (ixa.si.ehu.es) for the 7th Framework
OpeNER, NewsReader and QTLeap European projects.

### Contents

The contents of the repository are the following:

    + src/ source files of ixa-pipe-wikify
    + pom.xml 
    + pom-naf.xml
    + README.md: This README

## Installation Procedure

In a snapshot:

 1. Install dbpedia-spotlight
 2. Compile ixa-pipe-wikify module with mvn clean package
 3. Start dbpedia-spotlight server
 4. cat text.kaf | ixa-pipe-ned/target/ixa-pipe-wikify-1.0.jar -p $PORT_NUMBER

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
- dbpedia-spotlight-0.7.jar
- English model: en_2+2.tar.gz
- Spanish model: es.tar.gz	

Decompressed the language models 
- tar xvf $lang.tar.gz

Install dbpedia-spotlight
- go to the directory the dbpedia-spotlight-0.7.jar is located
- execute:
  mvn install:install-file -Dfile=dbpedia-spotlight-0.7.jar -DgroupId=ixa -DartifactId=dbpedia-spotlight -Dversion=0.7 -Dpackaging=jar -DgeneratePom=true
  This command will install dbpedia-spotlight jar as a local maven repository

Start the application
- java -jar dbpedia-spotlight-0.7.jar $lang http://localhost:$port/rest 


### 4. Download the ixa-pipe-wikify repository

    git clone git@github.com:ixa-ehu/ixa-pipe-wikify.git


### 5. Install ixa-pipe-wikify

Install the ixa-pipe-wikify module

To work with KAF files

    mvn clean package

This command will create a `ixa-pipe-wikify/target` directory containing the
ixa-pipe-wikify-1.0.jar binary with all dependencies included.

### 6. ixa-pipe-wikify USAGE

The ixa-pipe-wikify-1.0.jar requires a KAF or NAF document as standard input and
provides Wikification as standard output. It also requires the port number as argument.
The port numbers assigned to each language are the following:

    - en: 2120
    - es: 2130

**Once you have a DBpedia Spotlight Rest server running you can send queries to it via the ixa-pipe-wikify module as follows:

    cat text.kaf | java -jar ixa-pipe-wikify-1.0.jar -p $PORT_NUMBER

For more options running ixa-pipe-wikify

    java -jar ixa-pipe-wikify-1.0.jar -h

#### Contact information

    Rodrigo Agerri and Arantxa Otegi
    {rodrigo.agerri,arantza.otegi}@ehu.es
    IXA NLP Group
    University of the Basque Country (UPV/EHU)
    E-20018 Donostia-San Sebastián

