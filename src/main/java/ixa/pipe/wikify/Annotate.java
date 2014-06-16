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

  public void disambiguateNEsToKAF (KAFDocument kaf, String host, String port) throws Exception {

      // String lang = kaf.getLang();
      // KAFDocument.LinguisticProcessor lp = kaf.addLinguisticProcessor("entities", "ixa-pipe-ned-" + lang, "1.0");
      // lp.setBeginTimestamp();

    String text = KAF2XMLText(kaf);    
    List<Entity> entities = kaf.getEntities();
    int pos = 0;
    int max = entities.size();
    int set = 0;
    if (max < 100){
	set = max;
    }
    else{
	set = 100;
    }
    while (pos < max){
	// disambiguate entities, 100 each time. 
	String entityAnnotation = surfaceForm(entities,pos,set);
	String annotation = spotAnnotation(text,entityAnnotation);
	Document response = annotate(annotation, host, port);
	XMLSpot2KAF(kaf,response);
	pos = set;
	set+=100;
	if (max < set){
	    set = max;
	}
    }
    /*
    String annotation = KAF2XMLSpot(kaf);
    Document response = annotate(annotation, host, port);
    XMLSpot2KAF(kaf,response);
    */
  }

    private String KAF2XMLText(KAFDocument kaf){
	String text = "";
	List<List<WF>> sentences = kaf.getSentences();
	for (List<WF> sentence : sentences) {
	    for (int i = 0; i < sentence.size(); i++) {
		if (!text.isEmpty()) {
		    text += " ";
		}
		String tok = sentence.get(i).getForm();
		
		//quot  "
		//amp   &
		//apos  '
		//lt    <
		//gt    >

		tok = tok.replaceAll( "&([^;]+(?!(?:\\w|;)))", "&amp;$1" );

		
		if (tok.contains("\"")){
		    tok = tok.replaceAll("\"","'");
		}
		
		if (tok.matches("&")) { 
		    tok = tok.replaceAll("&","amper");
		}
		
		
		if (tok.matches("<")){
		    tok = tok.replaceAll("<","&lt;");
		}
		
		if (tok.matches(">")){
		    tok = tok.replaceAll("<","&gt;");
		}
		

		if (!tok.matches("http.*")){
		    text += tok;
		}
	    }
	}
	return text;
    }


    private String surfaceForm(List<Entity> entities, int start, int end){
        int offset = -1;
	String entStr = "";
	String forms = "";
	
	List<Entity> entitySet = entities.subList(start,end);
	for (Entity entity : entitySet){
	    List<List<Term>> references = entity.getReferences();
	    for (List<Term> ref : references){
		offset = -1;
		// TODO: Careful! We are only using the last value of this variable in the forms variable below!!
		entStr = "";
		for (Term t: ref){
		    List<WF> words = t.getWFs();
		    for (int i = 0; i < words.size(); i++){
			if (!entStr.isEmpty()){
			    entStr += " ";
			}
			WF word = words.get(i);
			entStr += word.getForm();
			if (offset == -1){
			    if (word.hasOffset()){
				offset = word.getOffset();
			    }
			    else{
				System.out.println("There is not offset for word id " + word.getId());
			    }
			}
		    }
		}
		
	    }
	    // Each reference is a spot to disambiguate
	    entStr = entStr.replaceAll( "&([^;]+(?!(?:\\w|;)))", "&amp;$1" );

	    if (entStr.contains("\"")){
		entStr = entStr.replaceAll("\"","'");
	    }

	    /*
	    if (entStr.matches(".*&(?![A-Za-z]+;|#[0-9]+;).*")) { 
		entStr = entStr.replaceAll("&","amper");
	    }
	    */
	    /*	    	    
	    if (entStr.matches("&")) { 
		entStr = entStr.replaceAll("&","amper");
		}*/
	    
	    if (entStr.contains("<")){
		entStr = entStr.replaceAll("<","&lt;");
	    }
	    
	    if (entStr.contains(">")){
		entStr = entStr.replaceAll("<","&gt;");
	    }
		

	    forms += "<surfaceForm name=\"" + entStr + "\" offset=\"" + offset + "\"/>\n";
	}
	return forms;
    }

    private String spotAnnotation(String text,String forms){
	String annotation = "<annotation text=\"" + text + "\">\n" + forms + "</annotation>";
	return annotation;
    }

  private String KAF2XMLSpot(KAFDocument kaf){
    /*
	  <annotation text="Brazilian oil giant Petrobras and U.S. oilfield service company Halliburton have signed a technological cooperation agreement, Petrobras announced Monday. The two companies agreed on three projects: studies on contamination of fluids in oil wells, laboratory simulation of well production, and research on solidification of salt and carbon dioxide formations, said Petrobras. Twelve other projects are still under negotiation.">
	  <surfaceForm name="oil" offset="10"/>
	  <surfaceForm name="company" offset="56"/>
	  <surfaceForm name="Halliburton" offset="64"/>
	  <surfaceForm name="oil" offset="237"/>
	  <surfaceForm name="other" offset="383"/>
	  </annotation>
     */

    String text = "";
    List<List<WF>> sentences = kaf.getSentences();
    for (List<WF> sentence : sentences) {
      for (int i = 0; i < sentence.size(); i++) {
        if (!text.isEmpty()) {
          text += " ";
        }
        String tok = sentence.get(i).getForm();

	//quot  "
	//amp   &
	//apos  '
	//lt    <
	//gt    >
        if (tok.contains("\"")){
          tok = tok.replaceAll("\"","'");
        }
        if (tok.matches("&")) { 
        	tok = tok.replaceAll("&","amper");
        }
	if (tok.matches("<")){
	    tok = tok.replaceAll("<","&lt;");
	}
	if (tok.matches(">")){
	    tok = tok.replaceAll("<","&gt;");
	}
        text += tok;
      }
    }

    int offset = -1;
    String entStr = "";
    String forms = "";
    List<Entity> entities = kaf.getEntities();
    for (Entity entity : entities){
      List<List<Term>> references = entity.getReferences();
      for (List<Term> ref : references){
        offset = -1;
        // TODO: Careful! We are only using the last value of this variable in the forms variable below!!
        entStr = "";
        for (Term t: ref){
          List<WF> words = t.getWFs();
          for (int i = 0; i < words.size(); i++){
            if (!entStr.isEmpty()){
              entStr += " ";
            }
            WF word = words.get(i);
            entStr += word.getForm();
            if (offset == -1){
              if (word.hasOffset()){
                offset = word.getOffset();
              }
              else{
                System.out.println("There is not offset for word id " + word.getId());
              }
            }
          }
        }

      }
      // Each reference is a spot to disambiguate
      if (entStr.contains("\"")){
          entStr = entStr.replaceAll("\"","'");
      }
      if (entStr.matches(".*&(?![A-Za-z]+;|#[0-9]+;).*")) { 
	  entStr = entStr.replaceAll("&","amper");
      }
      forms += "<surfaceForm name=\"" + entStr + "\" offset=\"" + offset + "\"/>\n";
    }

    String annotation = "<annotation text=\"" + text + "\">\n" + forms + "</annotation>";
    return annotation;
  }

  private Document annotate(String annotation, String host, String port) throws AnnotationException {
    Document response = c.extract(new Text(annotation), host, port);
    return response;
  }

  private void XMLSpot2KAF(KAFDocument kaf, Document doc){
    // Store the References into a hash. Key: offset
    HashMap<Integer,String> refs = new HashMap<Integer,String>();


    doc.getDocumentElement().normalize();
    NodeList nList = doc.getElementsByTagName("Resource");

    for (int temp = 0; temp < nList.getLength(); temp++) {

      Node nNode = nList.item(temp);

      if (nNode.getNodeType() == Node.ELEMENT_NODE) {

        Element eElement = (Element) nNode;
        refs.put(new Integer(eElement.getAttribute("offset")),eElement.getAttribute("URI"));

      }
    }


    String resource = "spotlight_v1";
    List<Entity> entities = kaf.getEntities();
    for (Entity entity : entities){
      // Get the offset of the entity
      int offset = getEntityOffset(entity);
      if (refs.containsKey(offset)){
        String reference = refs.get(offset);
        // Create ExternalRef
        ExternalRef externalRef = kaf.createExternalRef(resource,reference);
        // addExternalRef to Entity
        entity.addExternalRef(externalRef);
      }
    }
  }

  private int getEntityOffset(Entity ent){
    int offset = -1;
    List<List<Term>> references = ent.getReferences();
    for (List<Term> ref : references){
      for (Term t: ref){
        List<WF> words = t.getWFs();
        for (int i = 0; i < words.size(); i++){
          WF word = words.get(i);
          if (word.hasOffset()){
            return word.getOffset();
          }
        }
      }
    }
    return offset;
  }
}
