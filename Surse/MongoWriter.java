import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.bson.Document;

public class MongoWriter {
    MongoClient client;
    MongoDatabase database;
    MongoCollection<Document> collection;
    
    List<DBObject> listMongo = new ArrayList<>();
    
    public MongoWriter(String collectionName) {
         
        Properties properties = getProperties();
        
        String databaseName = properties.getProperty("mongo.database");
        String host = properties.getProperty("mongo.host");
        String port = properties.getProperty("mongo.port");
        
        client = new MongoClient(host, Integer.parseInt(port));
        database = client.getDatabase(databaseName);

        if(database.getCollection(collectionName) == null)
            database.createCollection(collectionName);
        
        collection = database.getCollection(collectionName);
        
    }

    public Properties getProperties()
    {
        Properties returnedProperties = new Properties();
        
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String appConfigPath = rootPath + "mongo.properties";
        
        try {
            returnedProperties.load(new FileInputStream(appConfigPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return returnedProperties;
    }
}