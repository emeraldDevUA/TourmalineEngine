package Rendering.Lights;

import ResourceImpl.Shader;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL31;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;

public class LightingConfigurator {
    private static final int COMPUTED_SIZE = 2640;

    private static int bufferBase = -1;  // Assuming -1 means “not yet created”

    public static void printByteBuffer(ByteBuffer buffer, String label) {
        System.out.println("Contents of " + label + ":" + "Size: " + buffer.limit());
        buffer.rewind(); // Reset position to 0 for reading
        while (buffer.hasRemaining()) {
            System.out.print(buffer.getFloat() + " ");
        }
        System.out.println("\n");
        buffer.rewind(); // Reset position to 0 for further use
    }

    public static ByteBuffer putBufferInBuffer(ByteBuffer target, ByteBuffer source){
        source.rewind(); // Reset position to 0 for reading
        while (source.hasRemaining()) {
            target.putFloat(source.getFloat());
        }
        return target;
    }
    public static void setLights(List<AbstractLight> lights, Shader shaderProgram) {
        // Clear the ByteBuffer so that it can be filled from the beginning.

        float[] finalArray = new float[440];
        Float[] EmptyFloatArray = new Float[8];
        for(int i = 0; i < 8; i++){EmptyFloatArray[i] = 0f;}

        List<Float[]> list = new ArrayList<>();
        if (bufferBase == -1) {
            bufferBase = glGenBuffers();
            if(bufferBase == -1){
                throw  new RuntimeException("Lighting Buffer Allocation Failed!");
            }

            glBindBuffer(GL_UNIFORM_BUFFER, bufferBase);
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
            list.add(dirLights.get(i).getFloatArray());
        }
        // Fill remaining directional light slots with empty data.
        for (int i = boundDir; i < 5; i++) {
            // Use an appropriately named empty buffer if available.

            list.add(EmptyFloatArray);
        }

        // Fill point lights data.
        for (int i = 0; i < boundPoint; i++) {
            list.add(pointLights.get(i).getFloatArray());
//ww
        }
        // Fill remaining point light slots with empty data.
        for (int i = boundPoint; i < 50; i++) {
            list.add(EmptyFloatArray);
        }
      //  printByteBuffer(BufferedLights, "Final Buffer");
        // Prepare the ByteBuffer for reading.


        for(int i =0 ; i < list.size(); i ++){
            for(int j = 0; j < list.get(i).length; j++){
                finalArray[i*8+j] = list.get(i)[j];
            }
        }

        // Update the UBO with the new light data.
        glBindBuffer(GL_UNIFORM_BUFFER, bufferBase);
        glBufferData(GL_UNIFORM_BUFFER, finalArray, GL_STATIC_DRAW);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        // Bind the uniform block to binding point 4.
        shaderProgram.use();


        glBindBufferBase(GL_UNIFORM_BUFFER, Shader.LIGHT_BLOCK, bufferBase);

        shaderProgram.setUniform("number_pointLights", (int)(boundPoint));
        shaderProgram.setUniform("number_dirLights", (int)(2 + boundDir));

        shaderProgram.unbind();


    }



}
