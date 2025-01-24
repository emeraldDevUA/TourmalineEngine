package Rendering.Lights;

import ResourceImpl.Material;
import ResourceImpl.Mesh;
import lombok.Getter;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.joml.Math.cos;
import static org.joml.Math.sin;

public class PointLight extends AbstractLight {
    private Vector3f position;
    @Getter
    private Mesh lightMesh;
    @Getter
    private static  ByteBuffer emptyBuffer;

    private static final int posLightSize = 48 ;
    public PointLight(Vector3f position){
        super();
        this.position = position;
        this.LightBuffer = ByteBuffer.allocateDirect(posLightSize)
                .order(ByteOrder.nativeOrder());
        emptyBuffer =  ByteBuffer.allocateDirect(posLightSize)
                .order(ByteOrder.nativeOrder());
        generatePrimitive();
    }

    @Override
    public ByteBuffer formLight(){
        LightBuffer.clear();

        LightBuffer.putFloat(lightColor.x);
        LightBuffer.putFloat(lightColor.y);
        LightBuffer.putFloat(lightColor.z);

        LightBuffer.putFloat(position.x);
        LightBuffer.putFloat(position.y);
        LightBuffer.putFloat(position.z);

        LightBuffer.putFloat(lightIntensity);


        return LightBuffer;
    }


    private void generatePrimitive(){
        List<Vector3f> vertices  = new ArrayList<>();
        List<Integer>  indices   = new ArrayList<>();

        float po = this.lightIntensity;
        float steps = 20;
        for(int j = 0; j < steps; j ++){
        for(int i = 0; i < steps; i ++) {
            vertices.add(new Vector3f(
                    po * sin(((float) i) / steps * 6.28f),
                    po * cos(((float) i) / steps * 6.28f),
                    po * sin(((float) j) / steps * 6.28f)

            ));
            // Current vertex
            int current = j * (int) steps + i;

            // Next vertex in the longitude direction
            int nextI = (i + 1) % (int) steps; // Wrap around horizontally
            int nextJ = (j + 1) % (int) steps; // Wrap around vertically

            // Calculate indices
            int topRight    = j * (int) steps + nextI;
            int bottomLeft  = nextJ * (int) steps + i;
            int bottomRight = nextJ * (int) steps + nextI;

            // Triangle 1
            indices.add(current);
            indices.add(topRight);
            indices.add(bottomLeft);

            // Triangle 2
            indices.add(topRight);
            indices.add(bottomRight);
            indices.add(bottomLeft);

        }

        Map<String, List<?>> map = new HashMap<>();
        map.put("Vertices", vertices);
        map.put("Indices", indices);

        lightMesh = new Mesh(STR."GenericLight\{Math.random()}", map);
        lightMesh.compile();
        lightMesh.getMaterial().addColor(Material.ALBEDO_MAP, lightColor);
     }


    }
}
