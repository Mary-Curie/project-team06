package Consumer;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.io.IOException;
import java.io.Writer;
import java.io.FileWriter;
import java.io.File;

import json.gson.TrainingSet;

import json.gson.Question;
import json.gson.TestSet;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;

import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;

public class SDConsumer extends CasConsumer_ImplBase {

	public static final String PATH = "Output";
	public static final String Standard = "goldenstandard"; 
	public String filePath;
	public String standardPath;
	private Writer fileWriter = null;
	private List<Question> gold;
	
	public void initialize() throws ResourceInitializationException {
		filePath = (String) getConfigParameterValue(PATH);
	    
		if(filePath == null){
			throw new ResourceInitializationException(
				ResourceInitializationException.CONFIG_SETTING_ABSENT, 
				new Object[] {"output file initialization fail"}
			);	
		}
		
		try {
	        fileWriter = new FileWriter(new File(filePath));
	    
	      } catch (IOException e) {
	        e.printStackTrace();
	      }

		//get gloden standard file
		standardPath  = (String) getConfigParameterValue(Standard);	
		gold = TestSet.load(getClass().getResourceAsStream(standardPath)).stream().collect(toList());;
		gold.stream().filter(input->input.getBody() != null).forEach(input->input.setBody(input.getBody().trim().replaceAll("\\s+", " ")));	
		//evaluation = new Evaluation(gold);
	}

	public void processCas(CAS aCAS) throws ResourceProcessException {

		JCas jcas = null;
		try {
			jcas = aCAS.getJCas();
		} catch (Exception e) {
			e.printStackTrace();
		}

		FSIterator<TOP> it = jcas.getJFSIndexRepository().getAllIndexedFS(
				AtomicQueryConcept.type);
		while (it.hasNext()) {
			AtomicQueryConcept con = (AtomicQueryConcept) it.next();
			String text = con.getText();
			System.out.println(text);
			try {
				fileWriter.append(text + "\n");

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	     }
	      
		 it = jcas.getJFSIndexRepository().getAllIndexedFS(
		    		ConceptSearchResult.type);
	
		 for(int i = 0; i < gold.size(); i++){
			Question q = gold.get(i);
			System.out.println(q.getConcepts());
			System.out.println(q.getDocuments());
										
		 } 	 		
	}

}