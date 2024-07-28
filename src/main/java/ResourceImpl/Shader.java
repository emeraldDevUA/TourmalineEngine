package ResourceImpl;

import java.io.Closeable;
import java.io.IOException;

import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glUseProgram;

@SuppressWarnings("unused")
public class Shader implements Closeable {
    public static final int  POSITION_LOCATION = 0;
    public static final int    NORMAL_LOCATION = 1;
    public static final int BITANGENT_LOCATION = 2;
    public static final int       UVS_LOCATION = 3;
    public static final int MODEL_BLOCK = 0;
    public static final int CAMERA_BLOCK = 1;
    public static final int MATERIAL_BLOCK = 2;
    public static final int ALBEDO_MAP_BINDING = 4;
    public static final int METALNESS_MAP_BINDING = 5;
    public static final int ROUGHNESS_MAP_BINDING = 6;
    public static final int NORMAL_MAP_BINDING = 7;
    public static final int EMISSION_MAP_BINDING = 8;
    public static final int AMBIENT_OCCLUSION_MAP_BINDING = 9;
    private int program;

    public Shader(){

    }

    public void use()
    {
        glUseProgram(program);
    }

    @Override
    public void close() {
        glDeleteShader(program);
    }
}
