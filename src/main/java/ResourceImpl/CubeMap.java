package ResourceImpl;

import Interfaces.EnhancedLoadable;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.lwjgl.BufferUtils;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.opengl.GL30.GL_RGB16F;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;

@SuppressWarnings("SpellCheckingInspection")
public class CubeMap implements EnhancedLoadable{
    private static final String[] faceIndices = {"_posx", "_negx", "_negy", "_posy", "_posz", "_negz"};
    private int texture;
    private Map<String, hdrFace> faces;

    @Setter
    private boolean custom_mipmaps;

    @Setter
    private String extension;
    public CubeMap(final String path, final String extension, boolean customMips){
        custom_mipmaps = true;
        this.extension = ".hdr";
        texture = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, texture);
        setVerticalFlip(true);
        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);
        FloatBuffer textureData = null;

        for (int i = 0; i < 6; i++) {
            width.clear();
            height.clear();
            channels.clear();

            textureData = stbi_loadf(path + faceIndices[i] + extension,
                    width, height, channels, 3);
            if (textureData == null)
                System.err.println(STR."Cubemap \{path}\{faceIndices[i]}\{extension} is not found");
            else {
                glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i,
                        0, GL_RGB16F, width.get(0), height.get(0), 0, GL_RGB, GL_FLOAT, textureData);
                stbi_image_free(textureData);
            }
        }

        glGenerateMipmap(GL_TEXTURE_CUBE_MAP);

        // Loading 1-5 lvl mipmaps if they are found
        if (customMips) {
            for (int i = 0; i < 6; i++) {
                for (int j = 1; j < 6; j++) {
                    width.clear();
                    height.clear();
                    channels.clear();

                    textureData = stbi_loadf(STR."\{path}_mip_\{j}\{faceIndices[i]}\{extension}",
                            width, height, channels, 3);
                    if (textureData == null) {
                        System.err.println(STR."Cubemap mip \{path}_mip_\{j}\{faceIndices[i]}\{extension} is not found");
                        break;
                    }

                    glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i,
                            j, GL_RGB16F, width.get(0), height.get(0), 0, GL_RGB, GL_FLOAT, textureData);
                    stbi_image_free(textureData);
                }
            }
        }

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
    }


    @Override
    public void assemble() {

        Object[] array = faces.values().toArray();
        for(int i = 0; i < array.length; i++) {
            try {
                hdrFace face = (hdrFace) array[i];
                glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i,
                    0, GL_RGB16F, face.width.get(0), face.height.get(0), 0, GL_RGB, GL_FLOAT, face.texData);
                face.close();
            } catch (IOException|ClassCastException|NullPointerException e) {
                throw new RuntimeException(e);
            }

        }


        glGenerateMipmap(GL_TEXTURE_CUBE_MAP);

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);

    }

    @Override
    public void load(String path) throws FileNotFoundException, IOException {

        FloatBuffer textureData = null;
        faces = new HashMap<>();

        for (int i = 0; i < 6; i++) {
            IntBuffer width = BufferUtils.createIntBuffer(1);
            IntBuffer height = BufferUtils.createIntBuffer(1);
            IntBuffer channels = BufferUtils.createIntBuffer(1);

            width.clear();
            height.clear();
            channels.clear();

            textureData = stbi_loadf(path + faceIndices[i] + extension,
                    width, height, channels, 3);
            if (textureData == null)
                System.err.println(STR."Cubemap \{path}\{faceIndices[i]}\{extension} is not found");
            else {
                    faces.put(faceIndices[i], new hdrFace(width, height, textureData, channels));
            }

     }


    }

    public void use() {
        glBindTexture(GL_TEXTURE_CUBE_MAP, texture);


    }

    @AllArgsConstructor
    static class hdrFace implements Closeable {
        public IntBuffer width, height;
        public FloatBuffer texData;
        public  IntBuffer channels;
        @Override
        public void close() throws IOException {
            width.clear();
            height.clear();
            channels.clear();
            texData.clear();
            stbi_image_free(texData);
        }
    }

    public static void setVerticalFlip(boolean flip){
        stbi_set_flip_vertically_on_load(flip);
    }
}
