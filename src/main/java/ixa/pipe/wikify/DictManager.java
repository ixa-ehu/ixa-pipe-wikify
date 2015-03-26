package ixa.pipe.wikify;

import java.io.File;
import java.util.Map;
import org.mapdb.DB;
import org.mapdb.DBMaker;


public class DictManager{
    private DB db;
    private Map<String, String> map;
    
    
    public DictManager(String dbName,String hashName){
	File file = new File(dbName);
	db = DBMaker.newFileDB(file).readOnly().closeOnJvmShutdown().make();
	map = db.getHashMap(hashName);
    }

    public String getValue(String id){
	return map.get(id);
    }
}