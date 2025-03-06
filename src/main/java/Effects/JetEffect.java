package Effects;

import ResourceImpl.Mesh;
import ResourceImpl.Shader;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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

@RequiredArgsConstructor

public class JetEffect  extends BaseEffect{

    @Setter
    private int steps = 100;
    private final int Z_MAX;
    private final float radius;

    private Mesh jetStream;
    public JetEffect(){
        Z_MAX = 20;
        radius = .08f;
    }

    public void setJetPosition(Vector3f position){
        this.jetStream.setPosition(position);
    }
    public void setScaleVector(Vector3f scaleVector) {
        super.setScaleVector(scaleVector);
        this.jetStream.setScale(scaleVector);
    }

    @Override
    public void draw(){
        jetStream.getShader().setUniform("effectType", 1);
        jetStream.getShader().setUniform("rocketPos", getMainPosition());
        jetStream.draw();

    }

    @Override
    public void compile() {



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
