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
import ixa.kaflib.Term;
import ixa.kaflib.WF;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.regex.Pattern;

public class CLI {

    /**
     * Get dynamically the version of ixa-pipe-wikify by looking at the MANIFEST
     * file.
     */
    private final String version = CLI.class.getPackage().getImplementationVersion();
    private final String commit = CLI.class.getPackage().getSpecificationVersion();

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern JARPATH_PATTERN_BEGIN = Pattern.compile("file:");
    private static final Pattern JARPATH_PATTERN_END = Pattern.compile("[^/]+jar!.+");

    private String crosslinkMappingIndexFile;
    private final String crosslinkMappingHashName = "esEn";


    public CLI(){
    }


    public static void main(String[] args) throws Exception {
	CLI cmdLine = new CLI();
	cmdLine.parseCLI(args);
    }


    public final void parseCLI(final String[] args) throws Exception{
    	
    	Namespace parsedArguments = null;

        // create Argument Parser
        ArgumentParser parser = ArgumentParsers.newArgumentParser(
            "ixa-pipe-wikify-1.3.0.jar").description(
            "ixa-pipe-wikify-1.3.0 is a multilingual Wikification module "
                + "developed by IXA NLP Group based on DBpedia Spotlight API.\n");

        // specify port
        parser
            .addArgument("-p", "--port")
            //.choices("2010","2020","2030","2040","2050","2060")
            .required(true)
            .help(
		  "It is REQUIRED to choose a port number. Port numbers are assigned " +
		  "alphabetically by language code: de: 2010, en: 2020, es: 2030, fr: 2040, it: 2050, nl: 2060");
        parser
	    .addArgument("-s", "--server")
	    .required(false)
	    .setDefault("http://localhost")
	    .help("Choose hostname in which dbpedia-spotlight rest " +
        		"server is being executed; this value defaults to 'http://localhost'");
        
	parser
	    .addArgument("-i", "--index")
	    .setDefault("none")
	    .help("Path to the 'database' created by MapDB to find the corresponding English crosslingual link");
	parser
	    .addArgument("-n", "--name")
	    .setDefault("none")
	    .help("Name of the HashMap in the index to be used; i.e. 'esEn' for English crosslingual links for Spanish\n");

	parser
		.addArgument("-c", "--confidence")
		.type(Double.class)
		.setDefault(0.0)
		.help("Confidence level for spotlight service [0-1].\n");

	parser
		.addArgument("-su", "--support")
		.type(Integer.class)
		.setDefault(0)
		.help("Indicates how prominent is this entity, i.e. number of inlinks in Wikipedia.\n");

	parser
		.addArgument("-co", "--coreference")
		.type(Boolean.class)
		.setDefault(false)
		.help("Coreference resolution (true/false).\n");

        /*
         * Parse the command line arguments
         */

        // catch errors and print help
        try {
	    parsedArguments = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
	    parser.handleError(e);
	    System.out
		.println("Run java -jar target/ixa-pipe-wikify-1.3.0.jar -help for details");
	    System.exit(1);
        }

        String port = parsedArguments.getString("port");
        String host = parsedArguments.getString("server");
	String index = parsedArguments.getString("index");
	String hashName = parsedArguments.getString("name");
		double confidence = parsedArguments.getDouble("confidence");
		int support = parsedArguments.getInt("support");
		boolean coreference = parsedArguments.getBoolean("coreference");

	/*
	if(cross){
	    String jarpath = this.getClass().getResource("").getPath();
	    Matcher matcher = JARPATH_PATTERN_BEGIN.matcher(jarpath);
	    jarpath = matcher.replaceAll("");
	    matcher = JARPATH_PATTERN_END.matcher(jarpath);
	    jarpath = matcher.replaceAll("");
	    crosslinkMappingIndexFile = jarpath + "/resources/wikipedia-db";
	    if (!Files.isRegularFile(Paths.get(crosslinkMappingIndexFile))) {
		System.err.println("As you are using -c/--cross  parameter, wikipedia-db file not found. wikipedia-db* files must exist under 'resources/' folder.");
		throw new Exception();
	    }
	}
	*/
	    
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
		annotator.wikificationToKAF(kaf, host, port, confidence, support, coreference);
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
