package ixa.pipe.wikify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ixa.kaflib.KAFDocument;
import ixa.kaflib.WF;
import ixa.kaflib.Term;
import ixa.kaflib.ExternalRef;
import ixa.kaflib.Mark;

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

	// TODO: DBpedia-spotlight buffer max size

	Document response = annotate(text, host, port);
	XMLSpot2KAF(kaf,response);
	
    }
    

    private Document annotate(String text, String host, String port) throws AnnotationException {
	Document response = c.extract(new Text(text), host, port);
	return response;
    }
    
    
    private void XMLSpot2KAF(KAFDocument kaf, Document spotDoc){
	String resourceExternalRef = "spotlight";
	String resourceMarkable = "DBpedia";
	
	spotDoc.getDocumentElement().normalize();
	NodeList nList = spotDoc.getElementsByTagName("Resource");
	
	for (int temp = 0; temp < nList.getLength(); temp++) {
	    Node nNode = nList.item(temp);
	    
	    if (nNode.getNodeType() == Node.ELEMENT_NODE) {	
		Element eElement = (Element) nNode;
		ExternalRef externalRef = kaf.createExternalRef(resourceExternalRef,eElement.getAttribute("URI"));
		externalRef.setConfidence(Float.valueOf(eElement.getAttribute("similarityScore")));
		List<Term> spotTerms = getSpotTermsGivenOffset(kaf, new Integer(eElement.getAttribute("offset")), eElement.getAttribute("surfaceForm"));
		boolean noun = false;
		String markableLemma = "";
		for (Term t : spotTerms) {
		    if(markableLemma.length() != 0){
			markableLemma += " ";
		    }
		    markableLemma += t.getLemma();
		    if((t.getPos().compareTo("N") == 0) || (t.getPos().compareTo("R") == 0)){
			noun = true;
		    }
		}
		if(noun){ // at least one term of the spot has to be a noun
		    List<WF> spotWFs = new ArrayList<WF>();
		    for(Term t : spotTerms){
			List<WF> wfs = t.getWFs();
			for(WF wf : wfs){
			    spotWFs.add(wf);
			}
		    }
		    Mark markable = kaf.newMark(resourceMarkable, kaf.newWFSpan(spotWFs));
		    markable.setLemma(markableLemma);
		    markable.addExternalRef(externalRef);
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
		if(wf.getLength() >= sfLength) break;
		offset += wf.getLength() + 1;
		sfLength -= wf.getLength() + 1;
	    }
	}
	return spotTerms;
    }
      
}
