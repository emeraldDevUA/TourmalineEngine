package ResourceImpl;

import Interfaces.EnhancedLoadable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lwjgl.BufferUtils;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;


import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBImage.*;


@NoArgsConstructor
public class Texture implements EnhancedLoadable {
    private static final Map<String, Integer> loadedTextures = new HashMap<>();
    private static final Map<String, Integer> loadedInstances = new HashMap<>();
    @Getter
    private ByteBuffer textureData;
    @Getter
    private IntBuffer textureWidth = BufferUtils.createIntBuffer(1);
    @Getter
    private IntBuffer textureHeight = BufferUtils.createIntBuffer(1);
    private int texture;
    private String path;

    /**
     * Constructor
     *
     * @param path path to texture image file
     */
    public Texture(String path, int channels)
    {
        this.path = path;

        if(loadedTextures.containsKey(path))
        {
            texture = loadedTextures.get(path);
            loadedInstances.put(path, loadedInstances.get(path) + 1);
            return;
        }


        IntBuffer textureChannels = BufferUtils.createIntBuffer(1);


        try {
            textureData = stbi_load(path, textureWidth, textureHeight, textureChannels, channels);
        } catch (Exception e) {
            System.err.println("the STBI library failed to load the " + path + " texture");
        }

        texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR_MIPMAP_LINEAR);

        int format;
        int internal;
        switch (channels) {
            case 1:
                format = GL_R8;
                internal = GL_RED;
                break;
            case 2:
                format = GL_RG8;
                internal = GL_RG;
                break;
            case 3:
                format = GL_RGB8;
                internal = GL_RGB;
                break;
            case 4:
                format = GL_RGBA8;
                internal = GL_RGBA;
                break;
            default:
                throw new RuntimeException("Wrong texture channel count: "+channels);
        }

        glTexImage2D(GL_TEXTURE_2D, 0, format, textureWidth.get(0), textureHeight.get(0), 0,
                internal, GL_UNSIGNED_BYTE, textureData);
        glGenerateMipmap(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, 0);

        assert textureData != null;
        stbi_image_free(textureData);

        loadedTextures.put(path, texture);
        loadedInstances.put(path, 1);
    }

    public Texture(ByteBuffer buffer, int channels, int width, int height)
    {
        textureData = buffer;

        texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR_MIPMAP_LINEAR);

        int format;
        int internal = switch (channels) {
            case 1 -> {
                format = GL_R8;
                yield GL_RED;
            }
            case 2 -> {
                format = GL_RG8;
                yield GL_RG;
            }
            case 3 -> {
                format = GL_RGB8;
                yield GL_RGB;
            }
            case 4 -> {
                format = GL_RGBA8;
                yield GL_RGBA;
            }
            default -> throw new RuntimeException("Wrong texture channel count: "+channels);
        };

        glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0,
                internal, GL_UNSIGNED_BYTE, textureData);
        glGenerateMipmap(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, 0);

        assert textureData != null;
        stbi_image_free(textureData);

    }
    /**
     * Binds this texture to currently active texture slot
     */
    public void use()
    {
        glBindTexture(GL_TEXTURE_2D, texture);
    }

    /**
     * Deletes texture from GRAM
     */
    public void release()
    {
        loadedInstances.put(path, loadedInstances.get(path) - 1);
        if (loadedInstances.get(path) != 0)
            return;

        glDeleteTextures(texture);
    }

    public static void setVerticalFlip(boolean flip){
        stbi_set_flip_vertically_on_load(flip);
    }
    @Override
    public void load(String path) throws FileNotFoundException {
        this.path = path;
        int channels = 4;
        if(loadedTextures.containsKey(path))
        {
            texture = loadedTextures.get(path);
            loadedInstances.put(path, loadedInstances.get(path) + 1);
            return;
        }

         textureWidth = BufferUtils.createIntBuffer(1);
         textureHeight = BufferUtils.createIntBuffer(1);
         IntBuffer textureChannels = BufferUtils.createIntBuffer(1);

        textureData = stbi_load(path, textureWidth, textureHeight, textureChannels, channels);
        textureChannels.clear();
    }
    @Override
    public void assemble(){
        texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR_MIPMAP_LINEAR);

        int format;
        int internal;
        format = GL_RGBA16;
        internal = GL_RGBA;

        glTexImage2D(GL_TEXTURE_2D, 0, format, textureWidth.get(0), textureHeight.get(0), 0,
                internal, GL_UNSIGNED_BYTE, textureData);
        glGenerateMipmap(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, 0);

        assert textureData != null;
        try {
            stbi_image_free(textureData);
            loadedTextures.put(path, texture);
            loadedInstances.put(path, 1);
        }catch (NullPointerException e){
            System.err.println(e.getMessage());
        }
    }
}
