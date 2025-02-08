package Rendering.Lights;

import ResourceImpl.Shader;
import org.lwjgl.opengl.GL31;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;

public class LightingConfigurator {
    private static final int COMPUTED_SIZE = 2640;
    private static final ByteBuffer BufferedLights = ByteBuffer.allocateDirect(COMPUTED_SIZE);
    private static int bufferBase = -1;  // Assuming -1 means “not yet created”

    public static void setLights(List<AbstractLight> lights, Shader shaderProgram) {
        // Clear the ByteBuffer so that it can be filled from the beginning.
        BufferedLights.clear();

        if (bufferBase == -1) {
            bufferBase = glGenBuffers();
            glBindBuffer(GL_UNIFORM_BUFFER, bufferBase);
            // Allocate the UBO once. Using GL_DYNAMIC_DRAW if updates are frequent.
            glBufferData(GL_UNIFORM_BUFFER, COMPUTED_SIZE, GL_STATIC_DRAW);
            glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        List<PointLight> pointLights = new ArrayList<>();
        List<DirectionalLight> dirLights = new ArrayList<>();

        // Separate the lights into directional and point lists.
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

        // Fill directional lights data.
        for (int i = 0; i < boundDir; i++) {
            BufferedLights.put(dirLights.get(i).formLight());
        }
        // Fill remaining directional light slots with empty data.
        for (int i = boundDir; i < 5; i++) {
            // Use an appropriately named empty buffer if available.
            BufferedLights.put(PointLight.getEmptyBuffer());
        }

        // Fill point lights data.
        for (int i = 0; i < boundPoint; i++) {
            BufferedLights.put(pointLights.get(i).formLight());
        }
        // Fill remaining point light slots with empty data.
        for (int i = boundPoint; i < 50; i++) {
            BufferedLights.put(PointLight.getEmptyBuffer());
        }

        // Prepare the ByteBuffer for reading.
        BufferedLights.flip();

        // Update the UBO with the new light data.
        glBindBuffer(GL_UNIFORM_BUFFER, bufferBase);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, BufferedLights);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        // Bind the uniform block to binding point 4.
        shaderProgram.use();
        int blockIndex = GL31.glGetUniformBlockIndex(shaderProgram.getProgram(), "LightBlock");
        GL31.glUniformBlockBinding(shaderProgram.getProgram(), blockIndex, 4);

        // Update additional uniforms.
        shaderProgram.setUniform("number_pointLights", boundPoint);
        shaderProgram.setUniform("number_dirLights", boundDir);
        shaderProgram.unbind();
    }



}
