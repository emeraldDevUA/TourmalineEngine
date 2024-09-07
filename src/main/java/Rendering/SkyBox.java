package Rendering;

import Interfaces.Drawable;
import Interfaces.EnhancedLoadable;
import ResourceImpl.CubeMap;
import ResourceImpl.Shader;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

@NoArgsConstructor

public class SkyBox implements Drawable, EnhancedLoadable {

   private static final float[] skyboxVertices = {
            -1.0f, 1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,

            -1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,

            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,

            -1.0f, -1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,

            -1.0f, 1.0f, -1.0f,
            1.0f, 1.0f, -1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, -1.0f,

            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, 1.0f
    };
    private int skyboxVerticesBuffer, skyboxCubeArray;

    @Setter
    private CubeMap cubeMap;

    @Override
    public void draw() {
        cubeMap.use();
        glBindVertexArray(skyboxCubeArray);
        glDrawArrays(GL_TRIANGLES, 0, 36);
    }

    @Override
    public void compile() {
        skyboxCubeArray = glGenVertexArrays();
        glBindVertexArray(skyboxCubeArray);

        skyboxVerticesBuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, skyboxVerticesBuffer);
        glBufferData(GL_ARRAY_BUFFER, skyboxVertices, GL_STATIC_DRAW);

        glEnableVertexAttribArray(Shader.POSITION_LOCATION);
        glVertexAttribPointer(Shader.POSITION_LOCATION, 3, GL_FLOAT, false, 0, 0);

        glBindVertexArray(0);
    }

    @Override
    public void assemble() {
        compile();
    }

    @Override
    public void load(String path) throws FileNotFoundException, IOException {

    }
}
