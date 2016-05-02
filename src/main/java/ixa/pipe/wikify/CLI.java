/*
 * Copyright (C) 2015 IXA Taldea, University of the Basque Country UPV/EHU

   This file is part of ixa-pipe-wikify.

   ixa-pipe-wikify is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   ixa-pipe-wikify is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with ixa-pipe-wikify.  If not, see <http://www.gnu.org/licenses/>.

*/


package ixa.pipe.wikify;

import ixa.kaflib.KAFDocument;
import ixa.kaflib.WF;
import ixa.kaflib.Term;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.nio.file.*;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class CLI {

    /**
     * Get dynamically the version of ixa-pipe-wikify by looking at the MANIFEST
     * file.
     */
    private final String version = CLI.class.getPackage().getImplementationVersion();
    private final String commit = CLI.class.getPackage().getSpecificationVersion();

    
    public CLI(){
    }


    public static void main(String[] args) throws Exception {
        CLI cmdLine = new CLI();
        cmdLine.parseCLI(args);
    }
    

    public final void parseCLI(final String[] args) throws Exception{

        Namespace parsedArguments = null;

        ArgumentParser parser = ArgumentParsers.newArgumentParser(
            "ixa-pipe-wikify-" + version + ".jar").description(
            "ixa-pipe-wikify-" + version + " is a multilingual Wikification module "
                + "developed by IXA NLP Group based on DBpedia Spotlight API.\n");

        parser
            .addArgument("-p", "--port")
            .choices("2010","2020","2030","2040","2050","2060")
            .required(true)
            .help(
                  "It is REQUIRED to choose a port number. Port numbers are assigned " +
                  "alphabetically by language code: de: 2010, en: 2020, es: 2030, fr: 2040, it: 2050, nl: 2060");
        parser
            .addArgument("-s", "--server")
            .required(false)
            .setDefault("http://localhost")
            .help("Choose hostname in which dbpedia-spotlight rest " +
                  "server is being executed. Default value: http://localhost");
        parser
            .addArgument("-cor", "--coreference")
            .choices(true,false)
            .type(Boolean.class)
            .required(false)
            .setDefault(false)
            .help("Coreference resolution. When is true, no other filter will be applied. " +
                  "Default value: false");
        parser
            .addArgument("-con", "--confidence")
            .type(Double.class)
            .required(false)
            .setDefault(0.0)
            .help("Confidence filter. Selects all entities that have a confidence " +
                  "greater than this double value. Default value: 0.0");
        parser
            .addArgument("-sup", "--support")
            .type(Integer.class)
            .required(false)
            .setDefault(0)
            .help("Support filter. Selects all entities that have a support greater than this integer value. " +
                  "Default value: 0");
        parser
            .addArgument("-i", "--index")
            .setDefault("none")
            .help("Path to the 'database' created by MapDB to find the corresponding English crosslingual link");
        parser
            .addArgument("-n", "--name")
            .setDefault("none")
            .help("Name of the HashMap in the index to be used; i.e. 'esEn' for English crosslingual links for Spanish\n");
        
        
        /*
         * Parse the command line arguments
         */

        // catch errors and print help
        try {
            parsedArguments = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.out.println("Run java -jar target/ixa-pipe-wikify-" + version + ".jar -help for details");
            System.exit(1);
        }

        String port = parsedArguments.getString("port");
        String host = parsedArguments.getString("server");
        boolean coreference = parsedArguments.getBoolean("coreference");
        double confidence = parsedArguments.getDouble("confidence");
        int support = parsedArguments.getInt("support");
        String index = parsedArguments.getString("index");
        String hashName = parsedArguments.getString("name");

        // Input
        BufferedReader stdInReader = null;
        // Output
        BufferedWriter w = null;

        stdInReader = new BufferedReader(new InputStreamReader(System.in,"UTF-8"));
        w = new BufferedWriter(new OutputStreamWriter(System.out,"UTF-8"));
        KAFDocument kaf = KAFDocument.createFromStream(stdInReader);

        String lang = kaf.getLang();
        
        KAFDocument.LinguisticProcessor lp = kaf.addLinguisticProcessor("markables", "ixa-pipe-wikify-" + lang, version + "-" + commit);
        lp.setBeginTimestamp();
        
        Annotate annotator = new Annotate(index, hashName, lang);
        try{
            List<WF> wordForms = kaf.getWFs();
            List<Term> terms = kaf.getTerms();
            if (!wordForms.isEmpty() && !terms.isEmpty()){
                annotator.wikificationToKAF(kaf, host, port, coreference, confidence, support);
            }
        }
        catch (Exception e){
            System.err.println("Wikification failed: ");
            e.printStackTrace();
        }
        finally {
            lp.setEndTimestamp();
            w.write(kaf.toString());
            w.close();
        }
    }

}
