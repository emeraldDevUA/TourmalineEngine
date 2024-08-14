package ResourceImpl;

import lombok.Getter;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;

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
    public static final int SHADOW_MAP_BINDING = 10;

    @Getter
    private final int program;

    public Shader(String vertexPath, String fragmentPath)
    {
        String vertexSource = "";
        String fragmentSource = "";

        try {
            vertexSource = new String(Files.readAllBytes(Paths.get(vertexPath)));
            fragmentSource = new String(Files.readAllBytes(Paths.get(fragmentPath)));
        } catch (IOException e) {
            System.err.println("Shader reading failed");
            System.err.println(e.getMessage());
        }

        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexSource);
        glCompileShader(vertexShader);
        if (!glGetShaderInfoLog(vertexShader).isEmpty())
            System.err.println(STR."\{vertexPath}: \{glGetShaderInfoLog(vertexShader)}");

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentSource);
        glCompileShader(fragmentShader);
        if (!glGetShaderInfoLog(fragmentShader).isEmpty())
            System.err.println(STR."\{fragmentPath}: \{glGetShaderInfoLog(fragmentShader)}");

        program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);

        glLinkProgram(program);
        if (!glGetProgramInfoLog(program).isEmpty())
            System.err.println(glGetProgramInfoLog(program));

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    public void use() {

        glUseProgram(program);
    }

    public void unbind(){
        glUseProgram(0);
    }
    @Override
    public void close() {

        glDeleteShader(program);
    }
}
