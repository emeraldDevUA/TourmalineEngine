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
    private final Vector3f position;
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


    public void generatePrimitive(){
        List<Vector3f> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        float po = this.lightIntensity; // Radius of the sphere
        int steps = 50; // Number of subdivisions (both latitude and longitude)

        // Generate vertices
        for (int j = 0; j < steps; j++) {
            float theta = (float) ((float) j / steps * (float) 6*Math.PI); // Latitude angle
            for (int i = 0; i <= steps; i++) {
                float phi = (float) i / steps * 2.0f * (float) Math.PI; // Longitude angle

                // Optimize trigonometric calculations
                float sinTheta = (float) Math.sin(theta);
                float cosTheta = (float) Math.cos(theta);
                float sinPhi = (float) Math.sin(phi);
                float cosPhi = (float) Math.cos(phi);

                // Calculate vertex positions using spherical coordinates
                float x = po * sinTheta * cosPhi;
                float y = po * sinTheta * sinPhi;
                float z = po * cosTheta;

                vertices.add(new Vector3f(x, y, z));
            }
        }

        // Generate indices
        for (int j = 0; j < steps; j++) {
            for (int i = 0; i < steps; i++) {
                int current = j * (steps + 1) + i;
                int next = current + (steps + 1);

                // First triangle
                indices.add(current);
                indices.add(next);
                indices.add(current + 1);

                // Second triangle
                indices.add(current + 1);
                indices.add(next);
                indices.add(next + 1);
            }
        }

        Map<String, List<?>> map = new HashMap<>();
        map.put("Vertices", vertices);
        map.put("Indices", indices);

        lightMesh = new Mesh(STR."GenericLight\{Math.random()}", map);
        lightMesh.compile();
        lightMesh.getMaterial().addColor(Material.ALBEDO_MAP, lightColor);
        lightMesh.setNoCull(true);
    }

    @Override
    public void setLightColor(Vector3f lightColor) {
        super.setLightColor(lightColor);
        lightMesh.getMaterial().addColor(Material.ALBEDO_MAP, lightColor);

    }
}
