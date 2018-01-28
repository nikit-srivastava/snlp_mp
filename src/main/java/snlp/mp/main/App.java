package snlp.mp.main;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import com.mashape.unirest.http.exceptions.UnirestException;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.simple.Sentence;

/**
 * Hello world!
 *
 */
public class App {
	
	public static void main(String[] args) throws IOException, UnirestException {
		
		/*Path path = Paths.get("C:\\Users\\Nikit\\Downloads\\test.tsv");
		List<String> ttLines = new ArrayList<String>();
		Files.lines(path).forEachOrdered(s -> printOneLine(s));*/
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\Nikit\\Downloads\\test.tsv"),"utf-8"));
		String s = br.readLine();
		String s2 = br.readLine();
		String data[] = s2.split("\t");
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, depparse, openie");
		props.setProperty("ner.useSUTime", "0");
		Sentence sent = new Sentence("Chris Brown (American entertainer) is Charlie Sheen's better half.",props);
		List<String> nerTags = sent.nerTags();  // [PERSON, O, O, O, O, O, O, O]
		String firstPOSTag = sent.posTag(0);   // NNP
		SemanticGraph sg = sent.dependencyGraph();
		Collection<RelationTriple> triples = sent.openieTriples();
		for(RelationTriple entry: triples) {
			// Print the triple
	        System.out.println(entry.confidence + "\t" +
	        		entry.subjectLemmaGloss() + "\t" +
	        		entry.relationLemmaGloss() + "\t" +
	        		entry.objectLemmaGloss());
		}
		//SemanticGraphEdge edge = sg.getEdge(sg.getNodeByWordPattern("Edward"), sg.getNodeByWordPattern("Victor"));
		System.out.println(sg);
	}
	
	public static void process(String id, String fact) {
		//Run NER on the fact
		//Get dependencyGraph of the fact
		//find subject relation and object by matching against entities
		//Find the relation between entities on DBPedia
		//Find all the relations of subject and object in case of failure
		//Check if synonymous and check for negation
		//synonym check to be done with wordnet and upon failure wordsapi
		//save the ID and result in the output file
	}
	
	
	
}
