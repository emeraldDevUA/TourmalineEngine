package ResourceImpl;

import Interfaces.Drawable;
import Interfaces.EnhancedLoadable;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.joml.Vector4d;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

class VBO implements Drawable {
    private ByteBuffer modelBuffer;
    public VBO(){

    }

    public void putVertex(Vector4d vector){

    }
    public void putNormal(Vector3d vector){

    }
    public void putTex(Vector2d vector){

    }


    @Override
    public void draw() {

    }

    @Override
    public void compile() {

    }

}

public class Mesh implements EnhancedLoadable {
    private final Map<String, VBO> map;

    public Mesh(){
        map = new HashMap<>();
    }

    @Override
    public void load(String path) throws FileNotFoundException {

    }

    @Override
    public void assemble() {
        for (VBO vbo : map.values()) {
            vbo.compile();
        }
    }

}
