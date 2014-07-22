package ixa.pipe.wikify;

import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import ixa.kaflib.KAFDocument;
import ixa.kaflib.WF;
import ixa.kaflib.Entity;
import ixa.kaflib.Term;
import ixa.kaflib.ExternalRef;

import org.dbpedia.spotlight.exceptions.AnnotationException;
import org.dbpedia.spotlight.model.DBpediaResource;
import org.dbpedia.spotlight.model.Text;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class Annotate {


    DBpediaSpotlightClient c;
    public Annotate(){
	c = new DBpediaSpotlightClient ();
    }
    
    public void wikificationToKAF (KAFDocument kaf, String host, String port) throws Exception {
	
	String text = "";

	int textOffset = 0;
	List<WF> wordForms = kaf.getWFs();
	for (int i = 0; i < wordForms.size(); i++) {
	    WF wordForm = wordForms.get(i);
	    if (textOffset != wordForm.getOffset()){
		while(textOffset < wordForm.getOffset()) {
		    text += " ";
		    textOffset += 1;
		}
	    }
	    text += wordForm.getForm();
	    textOffset += wordForm.getLength();
	}
   
   

	//   int pos = 0;
	//int max = entities.size();
	//int set = 0;
	//if (max < 100){
	//	set = max;
	//}
	//else{
	//	set = 100;
	//}
	//while (pos < max){
	// disambiguate entities, 100 each time. 
	Document response = annotate(text, host, port);

	XMLSpot2KAF(kaf,response);
	//	pos = set;
	//set+=100;
	//if (max < set){
	//    set = max;
	//}
	//}
	
    }
    

    private Document annotate(String text, String host, String port) throws AnnotationException {
	Document response = c.extract(new Text(text), host, port);
	return response;
    }
    
    
    private void XMLSpot2KAF(KAFDocument kaf, Document doc){

	String resource = "spotlight_v1";
	
	doc.getDocumentElement().normalize();
	NodeList nList = doc.getElementsByTagName("Resource");
	
	for (int temp = 0; temp < nList.getLength(); temp++) {
	    Node nNode = nList.item(temp);
	    
	    if (nNode.getNodeType() == Node.ELEMENT_NODE) {	
		Element eElement = (Element) nNode;
		ExternalRef externalRef = kaf.createExternalRef(resource,eElement.getAttribute("URI"));
		List<Term> spotTerms = getSpotTermsGivenOffset(kaf, new Integer(eElement.getAttribute("offset")), eElement.getAttribute("surfaceForm"));
		if(spotTerms.size() == 1){
		    spotTerms.get(0).addExternalRef(externalRef);
		}
		else{
		    String compoundLemma = "";
		    for (Term t : spotTerms){
			if(compoundLemma.length() != 0){
			    compoundLemma += " ";
			}
			compoundLemma += t.getLemma();
		    }
		    Term compoundTerm = kaf.newCompound(spotTerms, compoundLemma);
		    compoundTerm.addExternalRef(externalRef);
		}
	    }
	}
    }
    
    
    private List<Term> getSpotTermsGivenOffset(KAFDocument kaf, int offset, String surfaceForm){
	
	List<Term> spotTerms = new ArrayList<Term>();

	int sfLength = surfaceForm.length();
	List<Term> docTerms = kaf.getTerms();
	for (Term t : docTerms){
	    WF wf = t.getWFs().get(0);
	    if(wf.getOffset() == offset){
		spotTerms.add(t);
		if(wf.getLength() == sfLength) break;
		offset += wf.getLength() + 1;
		sfLength -= wf.getLength() + 1;
	    }
	}
	return spotTerms;
    }
      
}
