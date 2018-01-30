/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package snlp.mp.dict;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mashape.unirest.http.exceptions.UnirestException;

import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;
import snlp.mp.misc.SNLPUtil;

/**
 *
 * @author ngonga
 */
public class WordNetExpansionAdv {

    WordNetDatabase database;

    public WordNetExpansionAdv(String dictionary) {
        System.setProperty("wordnet.database.dir", dictionary);
        database = WordNetDatabase.getFileInstance();        
    }
    
    /** Expands a single keyword by retrieving all the elements of all its synsets
     *  UPDATE - Synonyms from DataMuse API are also fetched now
     * @param keyword Input token
     * @return All elements of all synsets of keyword
     * @throws UnirestException 
     */
    private Set<String> getSynset(String keyword) throws UnirestException
    {
        Set<String> result = new HashSet<String>();
        Synset[] synsets = database.getSynsets(keyword);
        for (int i = 0; i < synsets.length; i++) {
            String[] s = synsets[i].getWordForms();
            for (int j = 0; j < s.length; j++) {
                result.add(s[j]);
            }
        }
        //Get extra synonyms from DataMuse
        List<String> synList = SNLPUtil.getDMSyn(keyword);
        result.addAll(synList);
        return result;
    }
    
    /** Expand a string by chunking it into tokens and expanding each of the
     * tokens using WordNet
     * @param keywords Input string
     * @return  Set of tokens after Wordnet expansion
     * @throws UnirestException 
     */
    private Set<String> expand(String keywords) throws UnirestException
    {
        String[] split = keywords.split(" ");
        Set<String> result = new HashSet<String>();
        Set<String> buffer;
        for(int i=0; i<split.length; i++)
        {
            // no need to expand prepositions and the like
            if(split[i].length() > 2)
            {
                buffer = getSynset(split[i]);
                for(String s: buffer)
                    result.add(s);
            }
            else
            {
                result.add(split[i]);
            }
        }
        return result;
    }
    
    /** Computes the jaccard similarity of two strings after carrying out a WordNet
     * expansion of each of the tokens of the input string
     * @param s1 First input string
     * @param s2 Second input string
     * @return Similarity value between 0 and 1.
     * @throws UnirestException 
     */
    public double getExpandedJaccardSimilarity(String s1, String s2) throws UnirestException
    {
        /*Set<String> tokens1 = expand(s1);
        Set<String> tokens2 = expand(s2);*/
    	Set<String> tokens1 = getSynset(s1);
        Set<String> tokens2 = getSynset(s2);
        
        tokens1.addAll(expand(s1));
        tokens2.addAll(expand(s2));
        
        Set<String> intersection = new HashSet<>(tokens1);
        intersection.retainAll(tokens2);
        Set<String> union = new HashSet<>(tokens1);
        union.addAll(tokens2);
        
        return ((double)intersection.size())/((double)union.size());
    }
    
    /** Computes the jaccard similarity of two strings after carrying out a WordNet
     * expansion of each of the tokens of the input string
     * @param s1 First input string
     * @param s2 Second input string
     * @return Similarity value between 0 and 1.
     * @throws UnirestException 
     */
    public double getExpandedJaccardSimilarityAdv(String relation, List<String> relList) throws UnirestException
    {
        /*Set<String> tokens1 = expand(s1);
        Set<String> tokens2 = expand(s2);*/
    	Set<String> tokens1 = getSynset(relation);
        Set<String> tokens2 = new HashSet<>();
        for(String rel: relList)
        		tokens2.addAll(getSynset(rel));
        
        tokens1.addAll(expand(relation));
        for(String rel: relList)
        	tokens2.addAll(expand(rel));
        
        Set<String> intersection = new HashSet<>(tokens1);
        intersection.retainAll(tokens2);
        Set<String> union = new HashSet<>(tokens1);
        union.addAll(tokens2);
        
        return ((double)intersection.size())/((double)union.size());
    }
    
    public static void main(String args[]) throws UnirestException
    {
        WordNetExpansionAdv wne = new WordNetExpansionAdv("C:\\Users\\Nikit\\Downloads\\SPARQL2NL-master\\resources\\wordnet\\dict");
        String token = "art";
        System.out.println(wne.getSynset(token));
        String token2 = "painting";
        System.out.println(wne.getSynset(token2));
        System.out.println(wne.getExpandedJaccardSimilarity(token, token2));
        
    }
}