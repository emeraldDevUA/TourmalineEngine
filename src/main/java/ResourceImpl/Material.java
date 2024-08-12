package ResourceImpl;

import org.joml.Vector3f;


import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Material implements Closeable {
    private Map<String, Double> physicalProperties;
    private Map<String, Texture> pbrMaps;
    private Map<String, Vector3f> colors;

    public  Material(){
        pbrMaps = new ConcurrentHashMap<>();
        physicalProperties = new ConcurrentHashMap<>();
        colors = new ConcurrentHashMap<>();
    }

    public void addProperty(String name, Double value){

        physicalProperties.put(name, value);
    }
    public void addMap(String name, Texture texture){

        pbrMaps.put(name, texture);
    }
    public void addColor(String name, Vector3f color){

        colors.put(name, color);
    }

    @Override
    public void close() {
        physicalProperties.clear();
        pbrMaps.clear();
        colors.clear();
    }

    public void use() {
    }
}
