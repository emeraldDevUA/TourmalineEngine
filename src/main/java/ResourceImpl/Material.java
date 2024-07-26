package ResourceImpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Material{
    private Map<String, Double> physicalProperties;
    private Map<String, Texture> pbrMaps;

    public  Material(){
        pbrMaps = new ConcurrentHashMap<>();
        physicalProperties = new ConcurrentHashMap<>();
    }

    public void addProperty(String name, Double value){
        physicalProperties.put(name, value);
    }

    public void addMap(String name, Texture texture){
        pbrMaps.put(name, texture);
    }

}
