package Rendering.Lights;

import ResourceImpl.Shader;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;

public class LightingConfigurator {
    private static int bufferBase = -1;
    private static int COMPUTED_SIZE = 624;
    private static ByteBuffer BufferedLights = ByteBuffer.allocateDirect(COMPUTED_SIZE);


    public static void setLights(List<AbstractLight> lights, Shader shaderProgram) {
        if (bufferBase == -1) {
            bufferBase = glGenBuffers();
            glBindBuffer(GL_UNIFORM_BUFFER, bufferBase);
            glBufferData(GL_UNIFORM_BUFFER, COMPUTED_SIZE, GL_STATIC_DRAW);
            glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        List<PointLight> pointLights = new ArrayList<>();
        List<DirectionalLight> dirLights = new ArrayList<>();

        lights.forEach(light -> {
            if (light instanceof PointLight) {
                pointLights.add((PointLight) light);
            } else if (light instanceof DirectionalLight) {
                dirLights.add((DirectionalLight) light);
            } else {
                System.err.println("Unsupported Light Type: " + light.getClass().getName());
            }
        });

        int boundDir = Math.min(5, dirLights.size());
        int boundPoint = Math.min(50, pointLights.size());

        for (int i = 0; i < boundDir; i++) {
            BufferedLights.put(dirLights.get(i).formLight());
        }
//        for (int i = boundDir; i < 5; i++) {
//            BufferedLights.put(DirectionalLight.getEmptyBuffer());
//        }

        for (int i = 0; i < boundPoint; i++) {
            BufferedLights.put(pointLights.get(i).formLight());
        }
        for (int i = boundPoint; i < 50; i++) {
            BufferedLights.put(PointLight.getEmptyBuffer());
        }

        BufferedLights.flip();

        shaderProgram.use();
        glBindBufferBase(GL_UNIFORM_BUFFER, 4, bufferBase);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, BufferedLights);
        shaderProgram.setUniform("number_pointLights", boundPoint);
        shaderProgram.setUniform("number_dirLights", boundDir);
        shaderProgram.unbind();
    }



}
