package ixa.pipe.wikify;

import ixa.kaflib.KAFDocument;
import ixa.kaflib.WF;
import ixa.kaflib.Term;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.util.List;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class CLI {

    public static void main(String[] args) throws Exception {
    	
    	Namespace parsedArguments = null;

        // create Argument Parser
        ArgumentParser parser = ArgumentParsers.newArgumentParser(
            "ixa-pipe-wikify-1.1.0.jar").description(
            "ixa-pipe-wikify-1.1.0 is a multilingual Wikification module "
                + "developed by IXA NLP Group based on DBpedia Spotlight API.\n");

        // specify port
        parser
            .addArgument("-p", "--port")
            .choices("2020","2030")
            .required(true)
            .help(
		  "It is REQUIRED to choose a port number. Port numbers are assigned " +
		  "alphabetically by language code: en:2020, es:2030");

        parser.addArgument("-H", "--host").setDefault("http://localhost").help("Choose hostname in which dbpedia-spotlight rest " +
        		"server is being executed; this value defaults to 'http://localhost'");
        
        /*
         * Parse the command line arguments
         */

        // catch errors and print help
        try {
	    parsedArguments = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
	    parser.handleError(e);
	    System.out
		.println("Run java -jar target/ixa-pipe-wikify-1.1.0.jar -help for details");
	    System.exit(1);
        }

        /*
         * Load port and host parameters; host defaults to http://localhost
         */

        String port = parsedArguments.getString("port");
        String host = parsedArguments.getString("host");

	// Input
	BufferedReader stdInReader = null;
	// Output
	BufferedWriter w = null;

	stdInReader = new BufferedReader(new InputStreamReader(System.in,"UTF-8"));
	w = new BufferedWriter(new OutputStreamWriter(System.out,"UTF-8"));
	KAFDocument kaf = KAFDocument.createFromStream(stdInReader);
	
	String lang = kaf.getLang();
	KAFDocument.LinguisticProcessor lp = kaf.addLinguisticProcessor("markables", "ixa-pipe-wikify-" + lang, "1.1.0");
	lp.setBeginTimestamp();

	Annotate annotator = new Annotate();
	try{
	    List<WF> wordForms = kaf.getWFs();
	    List<Term> terms = kaf.getTerms();
	    if (!wordForms.isEmpty() && !terms.isEmpty()){
		annotator.wikificationToKAF(kaf, host, port);
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
