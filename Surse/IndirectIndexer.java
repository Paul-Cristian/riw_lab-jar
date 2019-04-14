import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.bson.Document;

import com.mongodb.BasicDBObject;


public class IndirectIndexer {

    public static Map<String, HashMap<String, Integer>> invertedIndex = new HashMap<String, HashMap<String, Integer>>();
    
    public static Map<String, HashMap<String, Integer>> getInvertedIndex(LinkedList<File> listOfFiles, HashMap<String, Map<String, Integer>> mapper) throws IOException
    {
        FileWriter writer = new FileWriter(new File("invertedIndex.txt"));
        MongoWriter dbWriter = new MongoWriter("IndirectIndex");
        
        for(String fileName : mapper.keySet()) {
            for(String word : mapper.get(fileName).keySet()) {
                if(invertedIndex.containsKey(word)) {
                    invertedIndex.get(word).put(fileName, mapper.get(fileName).get(word));
                }else {
                    HashMap<String, Integer> tempMap = new HashMap<String,Integer>();
                    tempMap.put(fileName, mapper.get(fileName).get(word));
                    invertedIndex.put(word, tempMap);
                }
            }
        }
        
        for(String word : invertedIndex.keySet()) {
            writer.write(word+ "  " + invertedIndex.get(word) + System.lineSeparator());
            
            for(String path : invertedIndex.get(word).keySet())
                dbWriter.listMongo.add(new BasicDBObject("d", path).append("c", invertedIndex.get(word).get(path)));
            
            dbWriter.collection.insertOne(new Document("term", word).append("docs", dbWriter.listMongo));
            dbWriter.listMongo.clear();
        }
        writer.close();
        dbWriter.client.close();
        return invertedIndex;
    }
}
