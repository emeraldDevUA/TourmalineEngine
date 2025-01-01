package Effects;

import ResourceImpl.Mesh;
import ResourceImpl.Shader;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class JetEffect  extends BaseEffect{

    private Mesh jetStream;
    public JetEffect(){



    }

    @Override
    public void draw(){

        jetStream.draw();

    }

    @Override
    public void compile() {
        int Z_MAX = 20;
        int steps = 100;
        float radius = .08f;
        List<Vector3f> vertices = new ArrayList<>(Z_MAX * steps + 2);
        List<Integer> indices = new ArrayList<>();

// Precompute trigonometric values
        float[] cosValues = new float[steps];
        float[] sinValues = new float[steps];
        for (int j = 0; j < steps; j++) {
            cosValues[j] = (float) Math.cos(2 * Math.PI * j / steps);
            sinValues[j] = (float) Math.sin(2 * Math.PI * j / steps);
        }

// Generate vertices
        for (int i = 0; i <= Z_MAX+2; i++) {
            for (int j = 0; j < steps; j++) {
                float X = 0.2f*(float) i;
                float Y = radius * sinValues[j];
                float Z = radius * cosValues[j];
                vertices.add(new Vector3f(X, Y, Z));
            }
        }

// Add cap vertices
        vertices.add(new Vector3f(0, 0, 0));        // Bottom center
        vertices.add(new Vector3f(0, 0, Z_MAX));   // Top center

// Generate side indices
        for (int i = 0; i <= Z_MAX + 2; i++) {
            for (int j = 0; j < steps; j++) {
                int current = i * steps + j;
                int next = i * steps + (j + 1) % steps;
                int above = (i + 1) * steps + j;
                int aboveNext = (i + 1) * steps + (j + 1) % steps;

                indices.add(current);
                indices.add(above);
                indices.add(next);

                indices.add(next);
                indices.add(above);
                indices.add(aboveNext);
            }
        }

// Generate cap indices
//        int bottomCenter = vertices.size() - 2; // Bottom center vertex index
//        int topCenter = vertices.size() - 1;   // Top center vertex index
//
//// Bottom cap
//        for (int j = 0; j < steps; j++) {
//            int next = (j + 1) % steps;
//            indices.add(bottomCenter); // Center of the bottom cap
//            indices.add(j);            // Current vertex on the bottom ring
//            indices.add(next);         // Next vertex on the bottom ring
//        }
//
//// Top cap
//        for (int j = 0; j < steps; j++) {
//            int next = (j + 1) % steps;
//            indices.add(topCenter);                  // Center of the top cap
//            indices.add((Z_MAX - 1) * steps + next); // Next vertex on the top ring
//            indices.add((Z_MAX - 1) * steps + j);    // Current vertex on the top ring
//        }
        // Create Mesh
        Map<String, List<?>> map = new HashMap<>();
        map.put("Indices", indices);
        map.put("Normals", new ArrayList<>()); // Placeholder
        map.put("Vertices", vertices);
        map.put("UVs", new ArrayList<>()); // Placeholder

        jetStream = new Mesh("JetStream", map);
        jetStream.setPosition(getMainPosition());
        jetStream.setRotQuaternion(getMainRotation());
        jetStream.compile();
        jetStream.setNoCull(true);
    }

    public Mesh getMesh() {
        return jetStream;
    }
}
